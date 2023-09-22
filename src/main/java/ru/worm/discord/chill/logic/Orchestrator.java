package ru.worm.discord.chill.logic;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.worm.discord.chill.lavaplayer.TrackScheduler;
import ru.worm.discord.chill.logic.locking.TrackLoadLocker;
import ru.worm.discord.chill.queue.Track;
import ru.worm.discord.chill.queue.event.ITrackQSubscriber;
import ru.worm.discord.chill.queue.event.TrackEvent;
import ru.worm.discord.chill.youtube.YtpDlpService;

import static ru.worm.discord.chill.lavaplayer.StreamProvider.getOggTrack;

@Service
public class Orchestrator implements ITrackQSubscriber {
    private final Logger log = LoggerFactory.getLogger(getClass());
    private final TrackScheduler scheduler;
    private final YtpDlpService downloader;
    private final TrackLoadLocker storage;

    @Autowired
    public Orchestrator(TrackScheduler scheduler, YtpDlpService downloader, TrackLoadLocker storage) {
        this.scheduler = scheduler;
        this.downloader = downloader;
        this.storage = storage;
    }

    @Override
    public void currentChanged(TrackEvent<Track> event) {
        Track track = event.getPayload();
        log.info("track changed to id={} url={}", track.getId(), track.getUrl());
        Mono.just(storage.checkFilePresent(track.getId()))
                .flatMap(present -> present ? Mono.empty() : downloader.loadAudio(track))
                .doOnSuccess((v) -> scheduler.trackLoaded(getOggTrack(track)))
                .subscribe();
    }
}
