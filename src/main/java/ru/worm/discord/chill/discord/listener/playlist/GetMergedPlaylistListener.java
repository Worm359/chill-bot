package ru.worm.discord.chill.discord.listener.playlist;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.worm.discord.chill.discord.Commands;
import ru.worm.discord.chill.discord.listener.EventListener;
import ru.worm.discord.chill.discord.listener.MessageListener;
import ru.worm.discord.chill.queue.Track;
import ru.worm.discord.chill.queue.TrackQueue;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

/**
 * выводит id/title из плейлиста
 */
@Service
public class GetMergedPlaylistListener extends MessageListener implements EventListener {
    private final TrackQueue playlist;

    @Autowired
    public GetMergedPlaylistListener(TrackQueue playlist) {
        this.playlist = playlist;
        this.command = Commands.GET_MERGED_PLAYLIST;
    }

    @Override
    public void onMessageReceived(@Nonnull MessageReceivedEvent event) {
        if (!filter(event)) {
            return;
        }
        int playingIndex = 0;

        List<Track> history = playlist.getHistory();
        Collections.reverse(history);
        playingIndex = history.size();

        List<Track> playing = playlist.getPlaylist();

        List<Track> allTracks = Stream.concat(history.stream(), playing.stream()).toList();

        StringBuilder hstMsg = new StringBuilder("```\n");
        hstMsg.append("id\t\t\t\ttitle\n");
        for (int i = 0; i < allTracks.size(); i++) {
            Track track = allTracks.get(i);
            String title = track.getTitle();
            if (i == playingIndex) {
                hstMsg.append(">");
            } else {
                hstMsg.append("*");
            }
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