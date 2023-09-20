package ru.worm.discord.chill.logic;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.worm.discord.chill.lavaplayer.TrackScheduler;
import ru.worm.discord.chill.queue.Track;
import ru.worm.discord.chill.queue.event.ITrackQSubscriber;
import ru.worm.discord.chill.queue.event.TrackEvent;
import ru.worm.discord.chill.youtube.YtpDlpService;

import static ru.worm.discord.chill.lavaplayer.StreamProvider.getOggTrack;

@Service
public class Orchestrator implements ITrackQSubscriber {
    private final TrackScheduler scheduler;
    private final YtpDlpService downloader;
    private final AudioInfoStorage storage;

    @Autowired
    public Orchestrator(TrackScheduler scheduler, YtpDlpService downloader, AudioInfoStorage storage) {
        this.scheduler = scheduler;
        this.downloader = downloader;
        this.storage = storage;
    }

    @Override
    public void currentChanged(TrackEvent<Track> event) {
        Track track = event.getPayload();
        Mono.just(storage.request(track))
                .flatMap(present -> present ? Mono.empty() : downloader.loadAudio(track))
                .doOnSuccess((v) -> scheduler.trackLoaded(getOggTrack(track)))
                .subscribe();
    }
}
