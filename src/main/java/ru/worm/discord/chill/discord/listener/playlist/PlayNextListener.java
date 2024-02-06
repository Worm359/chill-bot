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
import ru.worm.discord.chill.logic.command.validation.IdOrUrlValidator;
import ru.worm.discord.chill.queue.Track;
import ru.worm.discord.chill.queue.TrackFactory;
import ru.worm.discord.chill.queue.TrackQueue;
import ru.worm.discord.chill.util.Pair;

import javax.annotation.Nonnull;
import java.util.Optional;

/**
 * добавляет в playlist следующий youtube трек
 */
@Service
public class PlayNextListener extends MessageListener implements EventListener {
    private final TrackQueue playlist;
    private final TrackFactory trackFactory;

    @Autowired
    public PlayNextListener(TrackQueue playlist, TrackFactory trackFactory) {
        this.playlist = playlist;
        this.trackFactory = trackFactory;
        this.command = Commands.PLAY_NEXT;
    }

    @Override
    public void onMessageReceived(@Nonnull MessageReceivedEvent event) {
        CommandLine cli = filterWithOptions(event).orElse(null);
        if (cli == null) {
            return;
        }
        String url = cli.getOptionValue(CliOption.optUrl);
        String id = cli.getOptionValue(CliOption.optId);
        Optional<Track> track;
        if (url != null) {
            track = trackFactory.obtainTrack(url);
        } else {
            Integer trackId = Integer.valueOf(id);
            track = playlist.findTrackById(trackId);
            if (track.isPresent()) {
                playlist.remove(trackId);
            }
        }
        track.ifPresentOrElse(t -> playlist.addNext(t, true), () -> {
            String errIdentificator = url != null ? ("url=" + url) : ("id=" + id);
            answer(event, "track not found by %s".formatted(errIdentificator));
        });

    }

    @Override
    protected Pair<Options, IOptionValidator> options() {
        return new Pair<>(CliOption.idOrUrl, IdOrUrlValidator.INSTANCE);
    }
}