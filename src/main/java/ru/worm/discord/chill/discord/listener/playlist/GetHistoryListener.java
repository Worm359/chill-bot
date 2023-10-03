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
 * выводит id/title из истории
 */
@Service
public class GetHistoryListener extends MessageListener implements EventListener<MessageCreateEvent> {
    private final TrackQueue playlist;
    private final TitleService titleService;

    @Autowired
    public GetHistoryListener(TrackQueue playlist, TitleService titleService) {
        this.playlist = playlist;
        this.titleService = titleService;
        this.command = Commands.GET_HISTORY;
    }

    @Override
    public Class<MessageCreateEvent> getEventType() {
        return MessageCreateEvent.class;
    }

    public Mono<Void> execute(MessageCreateEvent event) {
        return filter(event.getMessage())
                .flatMap(m -> titleService.getTitles(playlist.getHistory()))
                .flatMap(m -> {
                    StringBuilder hstMsg = new StringBuilder("```\n");
                    hstMsg.append("id\t\t\t\ttitle\n");
                    for (Pair<Track, String> trackWithTitle : m) {
                        Track track = trackWithTitle.getFirst();
                        String title = trackWithTitle.getSecond();
                        hstMsg.append(track.getId())
                                .append("\t\t\t\t")
                                .append(title)
                                .append("\t\t\t\t")
//                                .append(track.getUrl())
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