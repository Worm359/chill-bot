package ru.worm.discord.chill.discord.listener.playlist;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import ru.worm.discord.chill.discord.Commands;
import ru.worm.discord.chill.discord.listener.EventListener;
import ru.worm.discord.chill.discord.listener.MessageListener;
import ru.worm.discord.chill.queue.Track;
import ru.worm.discord.chill.queue.TrackQueue;
import ru.worm.discord.chill.util.Consts;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * выводит id/title из истории
 */
@Profile(Consts.DEV_PROFILE)
@Service
public class GetHistoryListener extends MessageListener implements EventListener {
    private final TrackQueue playlist;

    @Autowired
    public GetHistoryListener(TrackQueue playlist) {
        this.playlist = playlist;
        this.command = Commands.GET_HISTORY;
    }

    @Override
    public void onMessageReceived(@Nonnull MessageReceivedEvent event) {
        if (!filter(event)) {
            return;
        }
        List<Track> tracks = playlist.getHistory();
        StringBuilder hstMsg = new StringBuilder("```\n");
        hstMsg.append("id\t\t\t\ttitle\n");
        for (Track track : tracks) {
            String title = track.getTitle();
            hstMsg.append(track.getId())
                .append("\t\t\t\t")
                .append(title)
                .append("\t\t\t\t")
                .append("\n");
        }
        hstMsg.append("```");
        answer(event, hstMsg.toString());
    }
}