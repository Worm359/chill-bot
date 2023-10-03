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

import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

/**
 * выводит id/title из плейлиста
 */
@Service
public class GetMergedPlaylistListener extends MessageListener implements EventListener<MessageCreateEvent> {
    private final TrackQueue playlist;
    private final TitleService titleService;

    @Autowired
    public GetMergedPlaylistListener(TrackQueue playlist, TitleService titleService) {
        this.playlist = playlist;
        this.titleService = titleService;
        this.command = Commands.GET_MERGED_PLAYLIST;
    }

    @Override
    public Class<MessageCreateEvent> getEventType() {
        return MessageCreateEvent.class;
    }

    public Mono<Void> execute(MessageCreateEvent event) {
        AtomicInteger currentIndex = new AtomicInteger();
        return filter(event.getMessage())
                .flatMap(m -> {
                    List<Track> history = playlist.getHistory();
                    Collections.reverse(history);
                    currentIndex.set(history.size());
                    List<Track> playing = playlist.getPlaylist();
                    List<Track> merged = Stream.concat(history.stream(), playing.stream()).toList();
                    return titleService.getTitles(merged);
                })
                .flatMap(m -> {
                    int playingIndex = currentIndex.get();
                    StringBuilder hstMsg = new StringBuilder("```\n");
                    hstMsg.append("id\t\t\t\ttitle\n");
                    for (int i = 0; i < m.size(); i++) {
                        Pair<Track, String> trackWithTitle = m.get(i);
                        Track track = trackWithTitle.getFirst();
                        String title = trackWithTitle.getSecond();
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
                    return event.getMessage()
                            .getChannel()
                            .flatMap(ch -> ch.createMessage(hstMsg.toString()));
                })
                .then();
    }
}