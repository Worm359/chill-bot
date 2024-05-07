package ru.worm.discord.chill.discord.listener;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import net.dv8tion.jda.api.audio.SpeakingMode;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.managers.AudioManager;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import ru.worm.discord.chill.discord.Commands;
import ru.worm.discord.chill.lavaplayer.LavaPlayerAudioProvider;
import ru.worm.discord.chill.lavaplayer.StreamProvider;
import ru.worm.discord.chill.queue.Track;

import javax.annotation.Nonnull;
import java.time.Duration;

import static ru.worm.discord.chill.util.Consts.DEV_PROFILE;

@Profile(DEV_PROFILE)
@Service
public class PlayExampleListener extends MessageListener implements ITextCommand {
    private final AudioPlayer player;
    public PlayExampleListener(AudioPlayer player) {
        this.player = player;
        this.command = Commands.PLAY_FROM_FILE;
    }

    @Override
    public void onMessageReceived(@Nonnull MessageReceivedEvent event) {
        if (!filter(event)) {
            return;
        }
        Guild guild = event.getGuild();
        // This will get the first voice channel with the name "music"
        // matching by voiceChannel.getName().equalsIgnoreCase("music")
        VoiceChannel channel = guild.getVoiceChannelsByName("general", true).get(0);
        AudioManager manager = guild.getAudioManager();

        // MySendHandler should be your AudioSendHandler implementation
        manager.setSendingHandler(new LavaPlayerAudioProvider(player));


        Track track = new Track("asdfasdf", "title", Duration.ofMillis(60000));
        // Here we finally connect to the target voice channel
        // and it will automatically start pulling the audio from the MySendHandler instance
        manager.setSelfDeafened(false);
        manager.setSelfMuted(false);
        manager.setSpeakingMode(SpeakingMode.VOICE);
        manager.openAudioConnection(channel);
        player.playTrack(StreamProvider.getOggTrack(track));
        event
                .getChannel()
                .sendMessage("trying to play")
                .queue();

    }
}