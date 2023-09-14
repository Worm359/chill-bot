package ru.worm.discord.chill.discord.listener;

import com.sedmelluq.discord.lavaplayer.container.ogg.OggAudioTrack;
import com.sedmelluq.discord.lavaplayer.tools.io.NonSeekableInputStream;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import discord4j.core.event.domain.message.MessageCreateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.worm.discord.chill.discord.Consts;
import ru.worm.discord.chill.lavaplayer.TrackScheduler;
import ru.worm.discord.chill.util.ExceptionUtils;
import ru.worm.discord.chill.youtube.YtpDlpService;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

/**
 * скачивает youtube аудио по ссылке и стримит в дискорд через LavaPlayer
 */
@Service
public class PlayDownloadedListener extends MessageListener implements EventListener<MessageCreateEvent> {
    private final Logger log = LoggerFactory.getLogger(getClass());
    private final TrackScheduler scheduler;
    private final YtpDlpService ytpDl;

    @Autowired
    public PlayDownloadedListener(TrackScheduler scheduler, YtpDlpService ytpDl) {
        this.scheduler = scheduler;
        this.ytpDl = ytpDl;
        this.command = Consts.DOWNLOAD_PLAY;
    }

    @Override
    public Class<MessageCreateEvent> getEventType() {
        return MessageCreateEvent.class;
    }

    public Mono<Void> execute(MessageCreateEvent event) {
        return filter(event.getMessage())
                .flatMap(m -> Mono.justOrEmpty(m.getContent()))
                .map(content -> Arrays.asList(content.split(" ")))
                .flatMap(command -> ytpDl
                        .loadAudio(command.get(1))
                        .doOnSuccess((v) -> scheduler.trackLoaded(getOggTrack())))
                        .doOnError(t -> log.error("while downloading file: {}", ExceptionUtils.getStackTrace(t)))
                .then();
    }

    private static Path path() {
        return Paths.get("").toAbsolutePath().resolve("output.opus");
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
}