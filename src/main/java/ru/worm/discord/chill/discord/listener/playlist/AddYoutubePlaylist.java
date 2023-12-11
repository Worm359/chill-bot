package ru.worm.discord.chill.discord.listener.playlist;

import discord4j.core.event.domain.message.MessageCreateEvent;
import org.apache.commons.cli.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.worm.discord.chill.discord.Commands;
import ru.worm.discord.chill.discord.listener.EventListener;
import ru.worm.discord.chill.discord.listener.MessageListener;
import ru.worm.discord.chill.logic.command.CliOption;
import ru.worm.discord.chill.logic.command.IOptionValidator;
import ru.worm.discord.chill.logic.command.validation.UrlValidator;
import ru.worm.discord.chill.queue.TrackQueue;
import ru.worm.discord.chill.util.Pair;
import ru.worm.discord.chill.util.YoutubeUtil;
import ru.worm.discord.chill.youtube.api.VideoMetadataService;

import java.util.Collections;
import java.util.List;

@Service
public class AddYoutubePlaylist extends MessageListener implements EventListener<MessageCreateEvent> {
    private final Logger log = LoggerFactory.getLogger(getClass());
    private final TrackQueue playlist;
    private final VideoMetadataService youtube;

    @Autowired
    public AddYoutubePlaylist(TrackQueue playlist, VideoMetadataService youtube) {
        this.playlist = playlist;
        this.youtube = youtube;
        this.command = Commands.PLAYLIST;
    }

    @Override
    public Class<MessageCreateEvent> getEventType() {
        return MessageCreateEvent.class;
    }

    public Mono<Void> execute(MessageCreateEvent event) {
        return filterWithOptions(event.getMessage())
                .flatMap(p -> {
                    String url = p.getSecond().getOptionValue(CliOption.optUrlRequired);
                    Boolean shuffle = p.getSecond().hasOption(CliOption.shuffle);
                    String playlistId = YoutubeUtil.stripPlaylistId(url).orElse(null);
                    if (playlistId == null) {
                        return Mono.error(new RuntimeException("couldn't extract playlist id from " + url));
                    } else {
                        return Mono.just(new Pair<>(playlistId, shuffle));
                    }
                })
                .doOnNext(p -> {
                    String playlistId = p.getFirst();
                    Boolean shuffle = p.getSecond();
                    List<String> videos = youtube.getVideoUrls(playlistId);
                    if (shuffle) {
                        Collections.shuffle(videos);
                    }
                    videos.forEach(v -> playlist.add(playlist.newTrack(v)));
                })
                .onErrorResume(throwable -> event.getMessage().getChannel()
                        .flatMap(channel -> channel.createMessage(throwable.getMessage()))
                        .flatMap(response -> Mono.empty()))
                .then();
    }

    @Override
    protected Pair<Options, IOptionValidator> options() {
        return new Pair<>(CliOption.urlAndShuffle, UrlValidator.INSTANCE);
    }
}