package ru.worm.discord.chill.discord.listener.playlist;

import discord4j.core.event.domain.message.MessageCreateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.worm.discord.chill.discord.Commands;
import ru.worm.discord.chill.discord.listener.EventListener;
import ru.worm.discord.chill.discord.listener.MessageListener;
import ru.worm.discord.chill.queue.TrackQueue;

/**
 * добавляет в playlist следующий youtube трек
 */
@Service
public class AddListener extends MessageListener implements EventListener<MessageCreateEvent> {
    private final Logger log = LoggerFactory.getLogger(getClass());
    private final TrackQueue playlist;

    @Autowired
    public AddListener(TrackQueue playlist) {
        this.playlist = playlist;
        this.command = Commands.ADD;
    }

    @Override
    public Class<MessageCreateEvent> getEventType() {
        return MessageCreateEvent.class;
    }

    public Mono<Void> execute(MessageCreateEvent event) {
        return filter(event.getMessage())
                .flatMap(m -> Mono.justOrEmpty(m.getContent()))
                .map(content -> content.split(" "))
                .flatMap(args -> {
                    if (args.length < 2) return Mono.empty();
                    return Mono.just(args[1]);
                })
                .map(playlist::newTrack)
                .doOnNext(playlist::add)
                .then();
    }
}