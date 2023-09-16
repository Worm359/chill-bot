package ru.worm.discord.chill.discord.listener;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import discord4j.core.event.domain.message.MessageCreateEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.worm.discord.chill.discord.Commands;
import ru.worm.discord.chill.lavaplayer.TrackScheduler;

import java.util.Arrays;

/**
 * пример проигрывание youtube через LavaPlayer по ссылке
 */
@Service
public class PlayRemoteYtbLavaPlayerListener extends MessageListener implements EventListener<MessageCreateEvent> {
    private final AudioPlayerManager playerManager;
    private final TrackScheduler scheduler;

    @Autowired
    public PlayRemoteYtbLavaPlayerListener(AudioPlayerManager playerManager, TrackScheduler scheduler) {
        this.playerManager = playerManager;
        this.scheduler = scheduler;
        this.command = Commands.PLAY;
    }

    @Override
    public Class<MessageCreateEvent> getEventType() {
        return MessageCreateEvent.class;
    }

    public Mono<Void> execute(MessageCreateEvent event) {
        return filter(event.getMessage())
                .map(m -> event)
                .flatMap(e -> Mono.justOrEmpty(e.getMessage().getContent()))
                .map(content -> Arrays.asList(content.split(" ")))
                .doOnNext(command -> playerManager.loadItem(command.get(1), scheduler))
                .then();
    }
}