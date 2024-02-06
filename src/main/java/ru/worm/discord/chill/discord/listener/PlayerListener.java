package ru.worm.discord.chill.discord.listener;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.worm.discord.chill.discord.Commands;
import ru.worm.discord.chill.logic.command.CliOption;
import ru.worm.discord.chill.logic.command.IOptionValidator;
import ru.worm.discord.chill.util.Pair;

import javax.annotation.Nonnull;

/**
 * добавляет в playlist следующий youtube трек
 */
@Service
public class PlayerListener extends MessageListener implements EventListener {
    private final Logger log = LoggerFactory.getLogger(getClass());
    private final AudioPlayer player;

    @Autowired
    public PlayerListener(AudioPlayer player) {
        this.player = player;
        this.command = Commands.PLAYER;
    }

    @Override
    public void onMessageReceived(@Nonnull MessageReceivedEvent event) {
        CommandLine cli = filterWithOptions(event).orElse(null);
        if (cli == null) {
            return;
        }
        boolean stop = cli.hasOption(CliOption.optStop);
        boolean start = cli.hasOption(CliOption.optStart);
        if (stop) {
            player.setPaused(true);
        } else if (start) {
            player.setPaused(false);
        }
    }

    @Override
    protected Pair<Options, IOptionValidator> options() {
        return new Pair<>(CliOption.player, null);
    }
}