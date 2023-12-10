package ru.worm.discord.chill.youtube;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import ru.worm.discord.chill.config.settings.RootSettings;
import ru.worm.discord.chill.config.settings.YoutubeSetting;
import ru.worm.discord.chill.logic.locking.FileCashLock;
import ru.worm.discord.chill.logic.locking.LoadEventHandler;
import ru.worm.discord.chill.logic.locking.TrackCashState;
import ru.worm.discord.chill.logic.locking.TrackLoadLocker;
import ru.worm.discord.chill.queue.Track;
import ru.worm.discord.chill.util.YoutubeUtil;
import ru.worm.discord.chill.youtube.api.VideoMetadataService;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static ru.worm.discord.chill.logic.AudioFilePath.trackFile;

@Component
public class YtpDlpService {
    private final Logger log = LoggerFactory.getLogger(getClass());
    private final TrackLoadLocker locker;
    private final LoadEventHandler loadingEventMng;
    private VideoMetadataService durationService;
    private final YoutubeSetting settings;

    @Autowired
    public YtpDlpService(RootSettings settings,
                         TrackLoadLocker locker,
                         LoadEventHandler loadingEventMng) {
        this.locker = locker;
        this.loadingEventMng = loadingEventMng;
        this.settings = settings.getYoutube();
    }

    @Autowired(required = false)
    public void setDurationService(VideoMetadataService durationService) {
        this.durationService = durationService;
    }

    public Mono<Void> loadAudio(Track ytTrack) {
        return Mono.create(sink -> {
            FileCashLock trackLock = locker.getLock(ytTrack.getId());
            Consumer<String> processError = (msg) -> {
                synchronized (trackLock) {
                    trackLock.error();
                    loadingEventMng.failedToLoad(ytTrack.getId());
                }
                sink.error(new RuntimeException(msg));
            };
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
                //fixme HTTP operation inside create -> refactor?
                //video duration check
                if (durationService != null) {
                    Optional<String> err = checkDuration(ytTrack);
                    if (err.isPresent()) {
                        processError.accept(err.get());
                        return;
                    }
                }
                //process building
                ProcessBuilder pb = new ProcessBuilder("yt-dlp.exe",
                        "-x",
                        "-o", trackFile(ytTrack),
                        "--no-playlist",
                        ytTrack.getUrl());
                pb.inheritIO();
                Process ytpDlpProcess = pb.start();
                //process -> future
                CompletableFuture<Process> future = ytpDlpProcess.onExit();
                future.completeOnTimeout(ytpDlpProcess, 30, TimeUnit.SECONDS);
                //process callbacks
                future.whenComplete((process, throwable) -> {
                    synchronized (trackLock) {
                        if (process != null && !process.isAlive() && process.exitValue() == 0) {
                            //lock#timeRequested was just updated. no need to check deleted state
                            trackLock.ready();
                            loadingEventMng.downloaded(ytTrack.getId());
                            sink.success();
                        } else {
                            //lock#timeRequested was just updated. no need to check deleted state
                            processError.accept("couldn't load " + ytTrack.getUrl());
                        }
                    }
                });
            } catch (IOException e) {
                processError.accept(e.getMessage());
            }
        });
    }

    private Optional<String> checkDuration(Track ytTrack) {
        String videoId = YoutubeUtil.stripVideoUrl(ytTrack.getUrl()).orElse(null);
        if (videoId == null) {
            return Optional.of("failed to strip %d %s video id"
                    .formatted(ytTrack.getId(), ytTrack.getUrl()));
        }
        Optional<Long> loadedDuration = durationService.videoMinutesLength(videoId);
        if (loadedDuration.isEmpty()) {
            return Optional.of("failed to obtain %d %s video duration"
                    .formatted(ytTrack.getId(), ytTrack.getUrl()));
        }
        Long duration = loadedDuration.get();
        if (duration.compareTo(settings.getMaximumVideoLengthMinutes()) > 0) {
            return Optional.of("%d %s duration %d min. is more than %d min. skip downloading."
                    .formatted(ytTrack.getId(), ytTrack.getUrl(), duration, settings.getMaximumVideoLengthMinutes()));
        }
        log.debug("{} {} duration is {} min. OK", ytTrack.getId(), ytTrack.getUrl(), duration);
        return Optional.empty();
    }
}
