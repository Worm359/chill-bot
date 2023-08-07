package ru.worm.discord.chill.discord.listener;

import com.sedmelluq.discord.lavaplayer.container.mp3.Mp3AudioTrack;
import com.sedmelluq.discord.lavaplayer.container.ogg.OggAudioTrack;
import com.sedmelluq.discord.lavaplayer.tools.io.NonSeekableInputStream;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import discord4j.core.event.domain.message.MessageCreateEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.worm.discord.chill.discord.Consts;
import ru.worm.discord.chill.lavaplayer.TrackScheduler;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

/**
 * пример проигрывания файла с диска
 */
@Service
public class PlayFileListener extends MessageListener implements EventListener<MessageCreateEvent> {
    private final TrackScheduler scheduler;

    @Autowired
    public PlayFileListener(TrackScheduler scheduler) {
        this.scheduler = scheduler;
        this.command = Consts.PLAY_FROM_FILE;
    }

    @Override
    public Class<MessageCreateEvent> getEventType() {
        return MessageCreateEvent.class;
    }

    public Mono<Void> execute(MessageCreateEvent event) {
        return filter(event.getMessage())
                .map(m -> event)
                .flatMap(e -> Mono.justOrEmpty(e.getMessage().getContent()))
                .map(content -> Arrays.asList(content.split(" ")))
                .doOnNext(command -> scheduler.trackLoaded(getMp3Track()))
                .then();
    }

    private static Path path() {
        return Paths.get("").toAbsolutePath().getParent().resolve("osip.mp3");
    }

    private static OggAudioTrack getOggTrack() {
        String pathToFileAsUri = path().toUri().toString();
        AudioTrackInfo trackInfo = new AudioTrackInfo("unknown", "unknown", (long) 5, "identifier", false, pathToFileAsUri);
        NonSeekableInputStream nonSeekableIS = new NonSeekableInputStream(inputStreamFromFile());
        return new OggAudioTrack(trackInfo, nonSeekableIS);
    }

    private static Mp3AudioTrack getMp3Track() {
        String pathToFileAsUri = path().toUri().toString();
        AudioTrackInfo trackInfo = new AudioTrackInfo("unknown", "unknown", (long) 5, "identifier", false, pathToFileAsUri);
        NonSeekableInputStream nonSeekableIS = new NonSeekableInputStream(inputStreamFromFile());
        return new Mp3AudioTrack(trackInfo, nonSeekableIS);
    }

    private static InputStream inputStreamFromFile() {
        Path file = path();
        //get input stream from file
        try {
            return Files.newInputStream(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}