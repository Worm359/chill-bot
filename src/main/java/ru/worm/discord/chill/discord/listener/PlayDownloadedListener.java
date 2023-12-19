package ru.worm.discord.chill.discord.listener;

import com.sedmelluq.discord.lavaplayer.container.ogg.OggAudioTrack;
import com.sedmelluq.discord.lavaplayer.tools.io.NonSeekableInputStream;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import discord4j.core.event.domain.message.MessageCreateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.worm.discord.chill.discord.Commands;
import ru.worm.discord.chill.lavaplayer.TrackScheduler;
import ru.worm.discord.chill.queue.Track;
import ru.worm.discord.chill.util.ExceptionUtils;
import ru.worm.discord.chill.youtube.YtpDlpService;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Arrays;

import static ru.worm.discord.chill.logic.AudioFilePath.trackFileWithExtension;
import static ru.worm.discord.chill.util.Consts.DEV_PROFILE;

/**
 * скачивает youtube аудио по ссылке (для каждого трека свой файл) и стримит в дискорд через LavaPlayer
 */
@Service
@Profile(DEV_PROFILE)
public class PlayDownloadedListener extends MessageListener implements EventListener<MessageCreateEvent> {
    private final Logger log = LoggerFactory.getLogger(getClass());
    private final TrackScheduler scheduler;
    private final YtpDlpService ytpDl;

    @Autowired
    public PlayDownloadedListener(TrackScheduler scheduler, YtpDlpService ytpDl) {
        this.scheduler = scheduler;
        this.ytpDl = ytpDl;
        this.command = Commands.DOWNLOAD_AND_PLAY;
    }

    @Override
    public Class<MessageCreateEvent> getEventType() {
        return MessageCreateEvent.class;
    }

    public Mono<Void> execute(MessageCreateEvent event) {
        return filter(event.getMessage())
                .flatMap(m -> Mono.justOrEmpty(m.getContent()))
                .map(content -> Arrays.asList(content.split(" ")))
                .flatMap(command -> {
                    Track track = new Track(command.get(1), "unknown - unknown", Duration.ofMinutes(2));
                    return ytpDl.loadAudio(track)
                            .doOnSuccess((v) -> scheduler.trackLoaded(getOggTrack(track)));
                })
                .doOnError(t -> log.error("while downloading file: {}", ExceptionUtils.getStackTrace(t)))
                .then();
    }


    private static Path path(Track track) {
        return Paths.get("").toAbsolutePath().resolve(trackFileWithExtension(track));
    }

    private static InputStream inputStreamFromFile(Track track) {
        Path file = path(track);
        //get input stream from file
        try {
            return Files.newInputStream(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static OggAudioTrack getOggTrack(Track track) {
        NonSeekableInputStream nonSeekableIS = new NonSeekableInputStream(inputStreamFromFile(track));
        AudioTrackInfo trackInfo = new AudioTrackInfo(
                "unknown",
                "unknown",
                144000,
                "identifier",
                false,
                path(track).toAbsolutePath().toString());
        return new OggAudioTrack(trackInfo, nonSeekableIS);
    }
}