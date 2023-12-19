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
import ru.worm.discord.chill.logic.command.CliOptionValidation;
import ru.worm.discord.chill.logic.command.IOptionValidator;
import ru.worm.discord.chill.queue.TrackFactory;
import ru.worm.discord.chill.queue.TrackQueue;
import ru.worm.discord.chill.util.Pair;

/**
 * добавляет в playlist следующий youtube трек
 */
@Service
public class AddListener extends MessageListener implements EventListener<MessageCreateEvent> {
    private final Logger log = LoggerFactory.getLogger(getClass());
    private final TrackQueue playlist;
    private final TrackFactory trackFactory;

    @Autowired
    public AddListener(TrackQueue playlist, TrackFactory trackFactory) {
        this.playlist = playlist;
        this.trackFactory = trackFactory;
        this.command = Commands.ADD;
    }

    @Override
    public Class<MessageCreateEvent> getEventType() {
        return MessageCreateEvent.class;
    }

    public Mono<Void> execute(MessageCreateEvent event) {
        return filterWithOptions(event.getMessage())
                .map(p -> p.getSecond().getArgList().get(0))
                .flatMap(trackFactory::newTrack)
                .doOnNext(playlist::add)
                .then();
    }

    @Override
    protected Pair<Options, IOptionValidator> options() {
        return new Pair<>(CliOption.emptyOptions, CliOptionValidation.youtubeLink);
    }
}