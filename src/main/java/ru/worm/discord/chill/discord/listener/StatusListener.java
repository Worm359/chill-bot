package ru.worm.discord.chill.discord.listener;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.worm.discord.chill.discord.Commands;

import javax.annotation.Nonnull;
import java.time.Duration;
import java.time.Instant;

@Service
public class StatusListener extends MessageListener implements EventListener {
    private final Instant launchTimestamp;
    private final BotLockListener lockListener;

    public StatusListener(@Qualifier("launchTimestamp") Instant launchTimestamp, BotLockListener lockListener) {
        this.lockListener = lockListener;
        this.command = Commands.STAT;
        this.launchTimestamp = launchTimestamp;
    }

    @Override
    public void onMessageReceived(@Nonnull MessageReceivedEvent event) {
        if (!filter(event)) {
            return;
        }
        String msg = """
            Version: ???
            Uptime: %s
            """.formatted(uptime());
        answer(event, msg);
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