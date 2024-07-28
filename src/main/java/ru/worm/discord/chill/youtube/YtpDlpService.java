package ru.worm.discord.chill.youtube;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.worm.discord.chill.config.settings.RootSettings;
import ru.worm.discord.chill.config.settings.YoutubeSetting;
import ru.worm.discord.chill.logic.locking.FileCashLock;
import ru.worm.discord.chill.logic.locking.LoadEventHandler;
import ru.worm.discord.chill.logic.locking.TrackCashState;
import ru.worm.discord.chill.logic.locking.TrackLoadLocker;
import ru.worm.discord.chill.queue.Track;
import ru.worm.discord.chill.util.YoutubeUtil;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static ru.worm.discord.chill.logic.AudioFilePath.trackFile;
import static ru.worm.discord.chill.youtube.LoadResult.err;
import static ru.worm.discord.chill.youtube.LoadResult.success;

@Component
public class YtpDlpService {
    private final Logger log = LoggerFactory.getLogger(getClass());
    private final TrackLoadLocker locker;
    private final LoadEventHandler loadingEventMng;
    private final YoutubeSetting settings;

    @Autowired
    public YtpDlpService(RootSettings settings,
                         TrackLoadLocker locker,
                         LoadEventHandler loadingEventMng) {
        this.locker = locker;
        this.loadingEventMng = loadingEventMng;
        this.settings = settings.getYoutube();
    }

    public CompletableFuture<LoadResult> loadAudio(Track ytTrack) {
        CompletableFuture<LoadResult> result = new CompletableFuture<>();
        FileCashLock trackLock = locker.getLock(ytTrack.getId());
        Consumer<String> processError = (msg) -> {
            synchronized (trackLock) {
                trackLock.error();
                loadingEventMng.failedToLoad(ytTrack.getId(), msg);
            }
        };
        try {
            synchronized (trackLock) {
                //why: later, tracks will be loaded before the play event occurs.
                //so, currentTrack -> load can happen when the track is already loading.
                //to not lose the player.play callback on a loaded track, we should await it, if loading.
                TrackCashState state = trackLock.getState();
                if (state == TrackCashState.ready) {
                    return CompletableFuture.completedFuture(new LoadResult());
                } else if (state == TrackCashState.loading) {
                    LoadEventHandler.ILoadingAwaiter callback = LoadEventHandler.awaiter(trackLock, result);
                    loadingEventMng.registerCallback(ytTrack.getId(), callback);
                    return result;
                } else if (state == TrackCashState.error) {
                    return CompletableFuture.completedFuture(err(
                        "previously encountered an error for this track, not gonna make the same mistake."
                    ));
                } else if (state == TrackCashState.deleted) {
                    return CompletableFuture.completedFuture(err(
                        "somehow have managed to get 'deleted' track lock. could try again next time."
                    ));
                } else {
                    if (state == TrackCashState.idle) {
                        trackLock.loading();
                    }
                }
            }

            //video duration check
            {
                Optional<String> err = checkDuration(ytTrack);
                if (err.isPresent()) {
                    processError.accept(err.get());
                    return CompletableFuture.completedFuture(err(
                        err.get()
                    ));
                }
            }

            //process building
            ProcessBuilder pb = new ProcessBuilder(settings.getYtpDlpBin(),
                "-x",
                "-o", trackFile(ytTrack),
                "--audio-format", "opus",
                "--no-playlist",
                YoutubeUtil.urlForVideoId(ytTrack.getVideoId()));
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
                        result.complete(success());
                    } else {
                        //lock#timeRequested was just updated. no need to check deleted state
                        String err = "couldn't load " + ytTrack;
                        processError.accept(err);
                        result.complete(err(err));
                    }
                }
            });
            return result;
        } catch (IOException e) {
            processError.accept(e.getMessage());
            return CompletableFuture.completedFuture(err(
                e.getMessage() != null ? e.getMessage() : "[no error description]"
            ));
        }
    }

    private Optional<String> checkDuration(Track ytTrack) {
        if (ytTrack.getDuration() == null) {
            log.warn("couldn't check {} duration (was not loaded for track).", ytTrack);
            return Optional.of("%s. duration is null".formatted(ytTrack.toString()));
        } else {
            Long duration = ytTrack.getDuration().toMinutes();
            if (duration.compareTo(settings.getMaximumVideoLengthMinutes()) > 0) {
                return Optional.of("%s. duration is more than %d min. skip downloading.".formatted(
                        ytTrack.toString(), settings.getMaximumVideoLengthMinutes())
                );
            }
            log.debug("{} duration check OK.", ytTrack);
            return Optional.empty();
        }
    }
}
