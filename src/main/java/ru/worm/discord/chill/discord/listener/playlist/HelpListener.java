package ru.worm.discord.chill.discord.listener.playlist;

import discord4j.core.event.domain.Event;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.worm.discord.chill.discord.Commands;
import ru.worm.discord.chill.discord.listener.EventListener;
import ru.worm.discord.chill.discord.listener.MessageListener;

import java.util.List;

@Service
public class HelpListener extends MessageListener implements EventListener<MessageCreateEvent> {
    private final List<EventListener<? extends Event>> eventListeners;

    @Autowired
    public HelpListener(List<EventListener<? extends Event>> eventListeners) {
        this.eventListeners = eventListeners;
        this.command = Commands.HELP;
    }

    @Override
    public Class<MessageCreateEvent> getEventType() {
        return MessageCreateEvent.class;
    }

    public Mono<Void> execute(MessageCreateEvent event) {
        return filter(event.getMessage())
                .flatMap(Message::getChannel)
                .flatMap(channel -> {
                    StringBuilder str = new StringBuilder();
                    eventListeners.forEach(l -> str.append(l.commandName()).append("\n"));
                    str.append("for more info type '!command -h'");
                    return channel.createMessage(str.toString());
                })
                .then();
    }
}