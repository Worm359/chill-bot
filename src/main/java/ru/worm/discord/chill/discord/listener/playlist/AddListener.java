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
import ru.worm.discord.chill.logic.command.CliOptionValidation;
import ru.worm.discord.chill.logic.command.IOptionValidator;
import ru.worm.discord.chill.queue.TrackFactory;
import ru.worm.discord.chill.queue.TrackQueue;
import ru.worm.discord.chill.util.AsyncUtil;
import ru.worm.discord.chill.util.Pair;

import javax.annotation.Nonnull;

/**
 * добавляет в playlist следующий youtube трек
 */
@Service
public class AddListener extends MessageListener implements ITextCommand {
    private final TrackQueue playlist;
    private final TrackFactory trackFactory;

    @Autowired
    public AddListener(TrackQueue playlist, TrackFactory trackFactory) {
        this.playlist = playlist;
        this.trackFactory = trackFactory;
        this.command = Commands.ADD;
    }

    @Override
    public void onMessageReceived(@Nonnull MessageReceivedEvent event) {
        CommandLine command = filterWithOptions(event).orElse(null);
        if (command == null) return;
        String youtubeUrl = command.getArgList().get(0);

        async(() -> trackFactory.obtainTrack(youtubeUrl))
            .thenAccept(track ->
                track.ifPresentOrElse(playlist::add, () -> {
                    String err = "had trouble loading the %s".formatted(youtubeUrl);
                    answer(event, err);
                }))
            .exceptionally(AsyncUtil.logError())
        ;
    }

    @Override
    public Pair<Options, IOptionValidator> options() {
        return new Pair<>(CliOption.emptyOptions, CliOptionValidation.youtubeLink);
    }
}