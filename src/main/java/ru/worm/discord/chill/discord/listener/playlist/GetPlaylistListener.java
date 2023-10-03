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
import ru.worm.discord.chill.util.Pair;
import ru.worm.discord.chill.youtube.oembed.TitleService;

/**
 * выводит id/title из плейлиста
 */
@Service
public class GetPlaylistListener extends MessageListener implements EventListener<MessageCreateEvent> {
    private final TrackQueue playlist;
    private final TitleService titleService;

    @Autowired
    public GetPlaylistListener(TrackQueue playlist, TitleService titleService) {
        this.playlist = playlist;
        this.titleService = titleService;
        this.command = Commands.GET_PLAYING;
    }

    @Override
    public Class<MessageCreateEvent> getEventType() {
        return MessageCreateEvent.class;
    }

    public Mono<Void> execute(MessageCreateEvent event) {
        return filter(event.getMessage())
                .flatMap(m -> titleService.getTitles(playlist.getPlaylist()))
                .flatMap(m -> {
                    StringBuilder hstMsg = new StringBuilder("```\n");
                    hstMsg.append("id\t\t\t\ttitle\n");
                    hstMsg.append(">");
                    for (int i = 0; i < m.size(); i++) {
                        if (i != 0) hstMsg.append("*");
                        Pair<Track, String> trackWithTitle = m.get(i);
                        Track track = trackWithTitle.getFirst();
                        String title = trackWithTitle.getSecond();
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