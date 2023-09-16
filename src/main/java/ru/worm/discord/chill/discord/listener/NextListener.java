package ru.worm.discord.chill.discord.listener;

import discord4j.core.event.domain.message.MessageCreateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.worm.discord.chill.discord.Commands;
import ru.worm.discord.chill.queue.TrackQueue;

/**
 * мотает на следующий трек
 */
@Service
public class NextListener extends MessageListener implements EventListener<MessageCreateEvent> {
    private final Logger log = LoggerFactory.getLogger(getClass());
    private final TrackQueue playlist;

    @Autowired
    public NextListener(TrackQueue playlist) {
        this.playlist = playlist;
        this.command = Commands.NEXT;
    }

    @Override
    public Class<MessageCreateEvent> getEventType() {
        return MessageCreateEvent.class;
    }

    public Mono<Void> execute(MessageCreateEvent event) {
        return filter(event.getMessage())
                .doOnNext(m -> playlist.nextFromPlaylist(true))
                .then();
    }
}