package ru.worm.discord.chill.discord.listener.playlist;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.worm.discord.chill.discord.Commands;
import ru.worm.discord.chill.discord.listener.ITextCommand;
import ru.worm.discord.chill.discord.listener.MessageListener;
import ru.worm.discord.chill.queue.TrackQueue;

import javax.annotation.Nonnull;

/**
 * мотает на следующий трек
 */
@Service
public class NextListener extends MessageListener implements ITextCommand {
    private final TrackQueue playlist;

    @Autowired
    public NextListener(TrackQueue playlist) {
        this.playlist = playlist;
        this.command = Commands.NEXT;
    }

    @Override
    public void onMessageReceived(@Nonnull MessageReceivedEvent event) {
        if (!filter(event)) {
            return;
        }
        playlist.next();
    }
}