package ru.worm.discord.chill.discord.listener;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.springframework.stereotype.Service;
import ru.worm.discord.chill.discord.Commands;
import ru.worm.discord.chill.discord.GuildObserver;

import javax.annotation.Nonnull;

@Service
public class DevListener extends MessageListener implements EventListener {

    public DevListener() {
        this.command = Commands.DEV;
    }

    @Override
    public void onMessageReceived(@Nonnull MessageReceivedEvent event) {
        if (!filter(event)) {
            return;
        }
        long guildId = event.getMessage().getGuildIdLong();
        if (guildId == 0) {
            answer(event, "couldn't retrieve guildId");
            log.warn("couldn't retrieve guildId");
            return;
        }
        String[] args = event.getMessage().getContentRaw().split(" ");
        if (args.length == 1) {
            GuildObserver.setDevMode(guildId);
        } else if (args[1].equalsIgnoreCase("disable")) {
            GuildObserver.discardDevMode(guildId);
        }
    }
}