package ru.worm.discord.chill.logic;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import ru.worm.discord.chill.logic.locking.TrackLoadLocker;
import ru.worm.discord.chill.queue.Track;
import ru.worm.discord.chill.queue.TrackQueue;
import ru.worm.discord.chill.queue.event.ITrackQSubscriber;
import ru.worm.discord.chill.queue.event.TrackEvent;
import ru.worm.discord.chill.youtube.YtpDlpService;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static ru.worm.discord.chill.lavaplayer.StreamProvider.getOggTrack;

@Service
public class Orchestrator implements ITrackQSubscriber {
    private final Logger log = LoggerFactory.getLogger(getClass());
    private final YtpDlpService downloader;
    private final TrackLoadLocker storage;
    private final TrackQueue playlist;
    private final AudioPlayer player;
    private final ExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

    @Autowired
    public Orchestrator(YtpDlpService downloader, TrackLoadLocker storage,
                        @Lazy TrackQueue playlist, AudioPlayer player) {
        this.player = player;
        this.downloader = downloader;
        this.storage = storage;
        this.playlist = playlist;
    }

    @Override
    public void currentChanged(TrackEvent<Track> event) {
        Track track = event.getPayload();
        log.info("track changed to {}", track);
        executorService.submit(() -> {
            boolean present = storage.checkFilePresent(track.getId());

            CompletableFuture<Boolean> loaded;
            if (!present) {
                loaded = downloader.loadAudio(track);
            } else {
                loaded = new CompletableFuture<>();
                loaded.complete(true);
            }
            loaded.thenAccept(success -> {
                if (!success) {
                    log.error("skipping track {} because of error {}",
                        track.getId(),
                        "???ExceptionUtils.getStackTrace(thrw)???");
                    playlist.remove(track.getId());
                    playlist.next();
                } else {
                    player.playTrack(getOggTrack(track));
                }
            });
        });
    }
}
