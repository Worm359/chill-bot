package ru.worm.discord.chill.youtube;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import ru.worm.discord.chill.logic.locking.AudioCashLockException;
import ru.worm.discord.chill.logic.locking.FileCashLock;
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

    @Autowired
    public YtpDlpService(TrackLoadLocker locker) {
        this.locker = locker;
    }

    public Mono<Void> loadAudio(Track ytTrack) {
        return Mono.create(sink -> {
            try {
                FileCashLock trackLock = locker.getLock(ytTrack.getId());
                synchronized (trackLock) {
                    //todo if deleted | error throw AutoLockException
                    //todo if loading, create await future and generate a mono
                    //todo if idle already implemented
                    //why: later, tracks will be loaded before the play event occurs.
                    //so, currentTrack -> load can happen when the track is already loading.
                    //to not lose the player.play callback on a loaded track, we should await it, if loading.
                    trackLock.checkIdleIfNotThrow();
                    trackLock.loading();
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
                    if (process != null && !process.isAlive() && process.exitValue() == 0) {
                        //lock#timeRequested was just updated. no need to check deleted state
                        trackLock.ready();
                        sink.success();
                    } else {
                        //lock#timeRequested was just updated. no need to check deleted state
                        trackLock.error();
                        sink.error(new RuntimeException("couldn't load " + ytTrack.getUrl()));
                    }
                });
            } catch (IOException e) {
                sink.error(e);
            } catch (AudioCashLockException e) {
                log.error("was trying to load track.id={} '{}' but the lock obtained by other thread",
                        ytTrack.getId(), ytTrack.getUrl());
                if (TrackCashState.ready.equals(e.getLastState())) sink.success();
                else sink.error(e);
            }
        });
    }
}
