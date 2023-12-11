package ru.worm.discord.chill.discord.listener;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.worm.discord.chill.discord.Commands;

@Service
public class SwearListener extends MessageListener implements EventListener<MessageCreateEvent> {
    public SwearListener() {
        this.command = Commands.PING;
    }

    @Override
    public Class<MessageCreateEvent> getEventType() {
        return MessageCreateEvent.class;
    }

    public Mono<Void> execute(MessageCreateEvent event) {
        return filter(event.getMessage())
                .flatMap(Message::getChannel)
                .flatMap(channel -> channel.createMessage("suck a dick dumbshits"))
                .then();
    }
}