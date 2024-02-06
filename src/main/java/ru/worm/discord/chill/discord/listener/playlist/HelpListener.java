package ru.worm.discord.chill.discord.listener.playlist;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.worm.discord.chill.discord.Commands;
import ru.worm.discord.chill.discord.listener.EventListener;
import ru.worm.discord.chill.discord.listener.MessageListener;

import javax.annotation.Nonnull;
import java.util.List;

@Service
public class HelpListener extends MessageListener implements EventListener {
    private final List<EventListener> eventListeners;

    @Autowired
    public HelpListener(List<EventListener> eventListeners) {
        this.eventListeners = eventListeners;
        this.command = Commands.HELP;
    }

    @Override
    public void onMessageReceived(@Nonnull MessageReceivedEvent event) {
        if (!filter(event)) {
            return;
        }
        StringBuilder helpInfo = new StringBuilder();
        eventListeners.forEach(l -> helpInfo.append(l.commandName()).append("\n"));
        helpInfo.append("for more info type '!command -h'");
        answer(event, helpInfo.toString());
    }
}