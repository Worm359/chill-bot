package ru.worm.discord.chill.discord.listener.playlist;

import discord4j.core.event.domain.message.MessageCreateEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.worm.discord.chill.discord.Commands;
import ru.worm.discord.chill.discord.listener.EventListener;
import ru.worm.discord.chill.discord.listener.MessageListener;
import ru.worm.discord.chill.queue.Track;
import ru.worm.discord.chill.queue.TrackQueue;

/**
 * выводит id/title из плейлиста
 */
@Service
public class GetPlaylistListener extends MessageListener implements EventListener<MessageCreateEvent> {
    private final TrackQueue playlist;

    @Autowired
    public GetPlaylistListener(TrackQueue playlist) {
        this.playlist = playlist;
        this.command = Commands.GET_PLAYING;
    }

    @Override
    public Class<MessageCreateEvent> getEventType() {
        return MessageCreateEvent.class;
    }

    public Mono<Void> execute(MessageCreateEvent event) {
        return filter(event.getMessage())
                .map(m -> playlist.getPlaylist())
                .flatMap(tracks -> {
                    StringBuilder hstMsg = new StringBuilder("```\n");
                    hstMsg.append("id\t\t\t\ttitle\n");
                    hstMsg.append(">");
                    for (int i = 0; i < tracks.size(); i++) {
                        if (i != 0) hstMsg.append("*");
                        Track track = tracks.get(i);
                        String title = track.getTitle();
                        hstMsg.append(track.getId())
                                .append("\t\t\t\t")
                                .append(title)
                                .append("\t\t\t\t")
                                .append("\n");
                    }
                    hstMsg.append("```");
                    return event.getMessage()
                            .getChannel()
                            .flatMap(ch -> ch.createMessage(hstMsg.toString()));
                })
                .then();
    }
}