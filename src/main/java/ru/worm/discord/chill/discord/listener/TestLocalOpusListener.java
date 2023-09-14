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
 * пример проигрывания файла с диска (hardcoded osip.opus в корне проекта)
 */
@Service
public class TestLocalOpusListener extends MessageListener implements EventListener<MessageCreateEvent> {
    private final TrackScheduler scheduler;

    @Autowired
    public TestLocalOpusListener(TrackScheduler scheduler) {
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
                .doOnNext(command -> scheduler.trackLoaded(getOggTrack()))
                .then();
    }

    private static Path path() {
        return Paths.get("").toAbsolutePath().getParent().resolve("osip.opus");
    }

    private static OggAudioTrack getOggTrack() {
        NonSeekableInputStream nonSeekableIS = new NonSeekableInputStream(inputStreamFromFile());
        AudioTrackInfo trackInfo = new AudioTrackInfo(
                "unknown",
                "unknown",
                144000,
                "identifier",
                false,
                path().toAbsolutePath().toString());
        return new OggAudioTrack(trackInfo, nonSeekableIS);
    }

    private static Mp3AudioTrack getMp3Track() {
        AudioTrackInfo trackInfo = new AudioTrackInfo("unknown",
                "unknown",
                (long) 5,
                "identifier",
                false,
                path().toUri().toString());
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