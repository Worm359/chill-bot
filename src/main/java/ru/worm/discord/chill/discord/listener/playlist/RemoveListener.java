package ru.worm.discord.chill.discord.listener.playlist;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.worm.discord.chill.discord.Commands;
import ru.worm.discord.chill.discord.listener.ITextCommand;
import ru.worm.discord.chill.discord.listener.MessageListener;
import ru.worm.discord.chill.logic.command.CliOption;
import ru.worm.discord.chill.logic.command.IOptionValidator;
import ru.worm.discord.chill.logic.command.validation.IdValidator;
import ru.worm.discord.chill.queue.TrackQueue;
import ru.worm.discord.chill.util.Pair;

import javax.annotation.Nonnull;

@Service
public class RemoveListener extends MessageListener implements ITextCommand {
    private final TrackQueue playlist;

    @Autowired
    public RemoveListener(TrackQueue playlist) {
        this.playlist = playlist;
        this.command = Commands.REMOVE;
    }

    @Override
    public void onMessageReceived(@Nonnull MessageReceivedEvent event) {
        CommandLine cli = filterWithOptions(event).orElse(null);
        if (cli == null) {
            return;
        }
        int id = Integer.parseInt(cli.getOptionValue(CliOption.optIdRequired));
        playlist.remove(id);
    }

    @Override
    public Pair<Options, IOptionValidator> options() {
        return new Pair<>(CliOption.id, IdValidator.INSTANCE);
    }
}