package ru.worm.discord.chill.discord.listener;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import discord4j.core.event.domain.message.MessageCreateEvent;
import org.apache.commons.cli.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.worm.discord.chill.discord.Commands;
import ru.worm.discord.chill.logic.command.CliOption;
import ru.worm.discord.chill.logic.command.IOptionValidator;
import ru.worm.discord.chill.util.Pair;

/**
 * добавляет в playlist следующий youtube трек
 */
@Service
public class PlayerListener extends MessageListener implements EventListener<MessageCreateEvent> {
    private final Logger log = LoggerFactory.getLogger(getClass());
    private final AudioPlayer player;

    @Autowired
    public PlayerListener(AudioPlayer player) {
        this.player = player;
        this.command = Commands.PLAYER;
    }

    @Override
    public Class<MessageCreateEvent> getEventType() {
        return MessageCreateEvent.class;
    }

    public Mono<Void> execute(MessageCreateEvent event) {
        return filterWithOptions(event.getMessage())
                .doOnNext(p -> {
                    boolean stop = p.getSecond().hasOption(CliOption.optStop);
                    boolean start = p.getSecond().hasOption(CliOption.optStart);
                    if (stop) {
                        player.setPaused(true);
                    } else if (start) {
                        player.setPaused(false);
                    }
                })
                .then();
    }

    @Override
    protected Pair<Options, IOptionValidator> options() {
        return new Pair<>(CliOption.player, null);
    }
}