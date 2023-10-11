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
import ru.worm.discord.chill.logic.command.validation.IdOrUrlValidator;
import ru.worm.discord.chill.queue.Track;
import ru.worm.discord.chill.queue.TrackQueue;
import ru.worm.discord.chill.util.Pair;

import java.util.Optional;

@Service
public class PlayNowListener extends MessageListener implements EventListener<MessageCreateEvent> {
    private final Logger log = LoggerFactory.getLogger(getClass());
    private final TrackQueue playlist;

    @Autowired
    public PlayNowListener(TrackQueue playlist) {
        this.playlist = playlist;
        this.command = Commands.PLAY_NOW;
    }

    @Override
    public Class<MessageCreateEvent> getEventType() {
        return MessageCreateEvent.class;
    }

    public Mono<Void> execute(MessageCreateEvent event) {
        return filterWithOptions(event.getMessage())
                .doOnNext(p -> {
                    String url = p.getSecond().getOptionValue(CliOption.optUrl);
                    String id = p.getSecond().getOptionValue(CliOption.optId);
                    if (url != null) {
                        playlist.playNow(playlist.newTrack(url));
                    } else {
                        Optional<Track> trackFromPlaylist = playlist.getTrackById(Integer.valueOf(id));
                        trackFromPlaylist.ifPresentOrElse(t -> {
                                    playlist.remove(t.getId());
                                    playlist.playNow(t);
                                },
                                () -> event.getMessage()
                                        .getChannel()
                                        .flatMap(ch -> ch.createMessage("id %s not found".formatted(id)))
                                        .subscribe());
                    }
                })
                .then();
    }

    @Override
    protected Pair<Options, IOptionValidator> options() {
        return new Pair<>(CliOption.idOrUrl, IdOrUrlValidator.INSTANCE);
    }
}