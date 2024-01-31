package ru.worm.discord.chill.discord.listener;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.worm.discord.chill.discord.Commands;

import java.time.Duration;
import java.time.Instant;

@Service
public class StatusListener extends MessageListener implements EventListener<MessageCreateEvent> {
    private final Instant launchTimestamp;
    private final BotLockListener lockListener;

    public StatusListener(@Qualifier("launchTimestamp") Instant launchTimestamp, BotLockListener lockListener) {
        this.lockListener = lockListener;
        this.command = Commands.STAT;
        this.launchTimestamp = launchTimestamp;
    }

    @Override
    public Class<MessageCreateEvent> getEventType() {
        return MessageCreateEvent.class;
    }

    public Mono<Void> execute(MessageCreateEvent event) {
        return filter(event.getMessage())
                .flatMap(Message::getChannel)
                .flatMap(channel -> {
                    String msg = """
                            Version: ???
                            Uptime: %s
                            """.formatted(uptime());
                    return channel.createMessage(msg);
                })
                .then();
    }


    private String uptime() {
        Duration uptime = Duration.between(launchTimestamp, Instant.now());
        lockListener.setLastDurationChecked(uptime);
        return "%d days (%02d hr %02d min %02d sec)".formatted(
                uptime.toDays(),
                uptime.toHours(),
                uptime.toMinutesPart(),
                uptime.toSecondsPart()
        );
    }
}