package ru.worm.discord.chill.youtube;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import ru.worm.discord.chill.logic.locking.FileCashLock;
import ru.worm.discord.chill.logic.locking.LoadEventHandler;
import ru.worm.discord.chill.logic.locking.TrackCashState;
import ru.worm.discord.chill.logic.locking.TrackLoadLocker;
import ru.worm.discord.chill.queue.Track;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static ru.worm.discord.chill.logic.AudioFilePath.trackFile;

@Component
public class YtpDlpService {
    private final Logger log = LoggerFactory.getLogger(getClass());
    private final TrackLoadLocker locker;
    private final LoadEventHandler loadingEventMng;

    @Autowired
    public YtpDlpService(TrackLoadLocker locker, LoadEventHandler loadingEventMng) {
        this.locker = locker;
        this.loadingEventMng = loadingEventMng;
    }

    public Mono<Void> loadAudio(Track ytTrack) {
        return Mono.create(sink -> {
            FileCashLock trackLock = locker.getLock(ytTrack.getId());
            try {
                synchronized (trackLock) {
                    //why: later, tracks will be loaded before the play event occurs.
                    //so, currentTrack -> load can happen when the track is already loading.
                    //to not lose the player.play callback on a loaded track, we should await it, if loading.
                    TrackCashState state = trackLock.getState();
                    if (state == TrackCashState.ready) {
                        sink.success();
                        return;
                    } else if (state == TrackCashState.loading) {
                        LoadEventHandler.ILoadingAwaiter callback = LoadEventHandler.awaiter(trackLock, sink);
                        loadingEventMng.registerCallback(ytTrack.getId(), callback);
                        return;
                    } else if (state == TrackCashState.error || state == TrackCashState.deleted) {
                        sink.error(new RuntimeException("couldn't load " + ytTrack.getUrl()));
                        return;
                    } else if (state == TrackCashState.idle) {
                        trackLock.loading();
                    }
                }
                ProcessBuilder pb = new ProcessBuilder("yt-dlp.exe",
                        "-x",
                        "-o", trackFile(ytTrack),
                        "--no-playlist",
                        ytTrack.getUrl());
                pb.inheritIO();
                Process ytpDlpProcess = pb.start();
                CompletableFuture<Process> future = ytpDlpProcess.onExit();
                future.completeOnTimeout(ytpDlpProcess, 30, TimeUnit.SECONDS);
                future.whenComplete((process, throwable) -> {
                    synchronized (trackLock) {
                        if (process != null && !process.isAlive() && process.exitValue() == 0) {
                            //lock#timeRequested was just updated. no need to check deleted state
                            trackLock.ready();
                            loadingEventMng.downloaded(ytTrack.getId());
                            sink.success();
                        } else {
                            //lock#timeRequested was just updated. no need to check deleted state
                            trackLock.error();
                            loadingEventMng.failedToLoad(ytTrack.getId());
                            sink.error(new RuntimeException("couldn't load " + ytTrack.getUrl()));
                        }
                    }
                });
            } catch (IOException e) {
                synchronized (trackLock) {
                    trackLock.error();
                    loadingEventMng.failedToLoad(ytTrack.getId());
                }
                sink.error(e);
            }
        });
    }
}
