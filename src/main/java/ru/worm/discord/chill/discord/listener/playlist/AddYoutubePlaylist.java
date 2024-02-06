package ru.worm.discord.chill.discord.listener.playlist;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.worm.discord.chill.discord.Commands;
import ru.worm.discord.chill.discord.listener.EventListener;
import ru.worm.discord.chill.discord.listener.MessageListener;
import ru.worm.discord.chill.logic.command.CliOption;
import ru.worm.discord.chill.logic.command.IOptionValidator;
import ru.worm.discord.chill.logic.command.validation.UrlValidator;
import ru.worm.discord.chill.queue.TrackFactory;
import ru.worm.discord.chill.queue.TrackQueue;
import ru.worm.discord.chill.util.AsyncUtil;
import ru.worm.discord.chill.util.Pair;
import ru.worm.discord.chill.util.YoutubeUtil;

import javax.annotation.Nonnull;
import java.util.Collections;

@Service
public class AddYoutubePlaylist extends MessageListener implements EventListener {
    private final TrackQueue playlist;
    private final TrackFactory trackFactory;

    @Autowired
    public AddYoutubePlaylist(TrackQueue playlist, TrackFactory trackFactory) {
        this.playlist = playlist;
        this.trackFactory = trackFactory;
        this.command = Commands.PLAYLIST;
    }

    @Override
    public void onMessageReceived(@Nonnull MessageReceivedEvent event) {
        CommandLine cli = filterWithOptions(event).orElse(null);
        if (cli == null) {
            return;
        }
        String url = cli.getOptionValue(CliOption.optUrlRequired);
        boolean shuffle = cli.hasOption(CliOption.shuffle);
        String playlistId = YoutubeUtil.stripPlaylistId(url).orElse(null);
        if (playlistId == null) {
            answer(event, "couldn't extract playlist id from " + url);
            return;
        }
        async(() -> trackFactory.obtainTracks(playlistId))
            .thenAccept(tracks -> {
                if (shuffle) {
                    Collections.shuffle(tracks);
                }
                tracks.forEach(playlist::add);
            })
            .exceptionally(AsyncUtil.logError());
    }

    @Override
    protected Pair<Options, IOptionValidator> options() {
        return new Pair<>(CliOption.urlAndShuffle, UrlValidator.INSTANCE);
    }
}