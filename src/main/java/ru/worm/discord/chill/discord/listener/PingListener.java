package ru.worm.discord.chill.discord.listener;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.springframework.stereotype.Service;
import ru.worm.discord.chill.discord.Commands;

import javax.annotation.Nonnull;

@Service
public class PingListener extends MessageListener implements ITextCommand {
    public PingListener() {
        this.command = Commands.PING;
    }

    @Override
    public void onMessageReceived(@Nonnull MessageReceivedEvent event) {
        if (!filter(event)) {
            return;
        }
        answer(event, "suck a dick dumbshits");
    }
}