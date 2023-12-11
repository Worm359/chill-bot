package ru.worm.discord.chill.discord.listener.playlist;

import discord4j.core.event.domain.message.MessageCreateEvent;
import org.apache.commons.cli.Options;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.worm.discord.chill.discord.Commands;
import ru.worm.discord.chill.discord.listener.EventListener;
import ru.worm.discord.chill.discord.listener.MessageListener;
import ru.worm.discord.chill.logic.command.CliOption;
import ru.worm.discord.chill.logic.command.IOptionValidator;
import ru.worm.discord.chill.logic.command.validation.IdValidator;
import ru.worm.discord.chill.queue.TrackQueue;
import ru.worm.discord.chill.util.Pair;

@Service
public class SkipToListener extends MessageListener implements EventListener<MessageCreateEvent> {
    private final TrackQueue playlist;

    @Autowired
    public SkipToListener(TrackQueue playlist) {
        this.playlist = playlist;
        this.command = Commands.SKIP_TO;
    }

    @Override
    public Class<MessageCreateEvent> getEventType() {
        return MessageCreateEvent.class;
    }

    public Mono<Void> execute(MessageCreateEvent event) {
        return filterWithOptions(event.getMessage())
                .doOnNext(p -> {
                    int id = Integer.parseInt(p.getSecond().getOptionValue(CliOption.optIdRequired));
                    playlist.skipTo(id);
                })
                .then();
    }

    @Override
    protected Pair<Options, IOptionValidator> options() {
        return new Pair<>(CliOption.id, IdValidator.INSTANCE);
    }
}