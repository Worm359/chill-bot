package ru.worm.discord.chill.discord.listener;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import net.dv8tion.jda.api.audio.SpeakingMode;
import net.dv8tion.jda.api.audio.hooks.ConnectionListener;
import net.dv8tion.jda.api.audio.hooks.ConnectionStatus;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.managers.AudioManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.worm.discord.chill.discord.Commands;
import ru.worm.discord.chill.lavaplayer.LavaPlayerAudioProvider;
import ru.worm.discord.chill.queue.TrackQueue;
import ru.worm.discord.chill.util.ExceptionUtils;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.Optional;

@Service
public class JoinListener extends MessageListener implements EventListener {
    private final AudioPlayer audioPlayer;
    private final TrackQueue trackQueue;
    private final Locker locker = new Locker();
    private AudioManager currentManager;
    private final LavaPlayerAudioProvider mySendHandler;

    /**
     * реализовать AudioProvider самому не получилось, см. ru.worm.discord.chill.ffmpeg.FfmpegAudioProvider
     */
    @Autowired
    public JoinListener(AudioPlayer audioPlayer, TrackQueue trackQueue, LavaPlayerAudioProvider mySendHandler) {
        this.audioPlayer = audioPlayer;
        this.trackQueue = trackQueue;
        this.mySendHandler = mySendHandler;
        this.command = Commands.JOIN;
    }


    private static class Locker {
        private boolean isLoading = false;
    }

    @Override
    public void onMessageReceived(@Nonnull MessageReceivedEvent event) {
        if (!filter(event)) {
            return;
        }
        Optional<String> voiceValidError = validateVoiceState(event);
        if (voiceValidError.isPresent()) {
            log.error(voiceValidError.get());
            answer(event, voiceValidError.get());
            return;
        }
        @SuppressWarnings("ConstantConditions")
        VoiceChannel voiceChannel = event.getMember()
            .getVoiceState()
            .getChannel()
            .asVoiceChannel();
        AudioManager requestManager = voiceChannel.getGuild().getAudioManager();
        synchronized (locker) {
            if (locker.isLoading) {
                answer(event, "sorry. already processing another join");
                return;
            }
            locker.isLoading = true;
            if (currentManager != null) {
                log.info("pausing and closing previous connection");
                audioPlayer.setPaused(true);
                if (!Objects.equals(requestManager.getGuild().getIdLong(), currentManager.getGuild().getIdLong())) {
                    currentManager.closeAudioConnection();
                }
            }
        }
        try {
            currentManager = requestManager;
            currentManager.setSendingHandler(mySendHandler);
            currentManager.setSelfDeafened(false);
            currentManager.setSelfMuted(false);
            currentManager.setSpeakingMode(SpeakingMode.VOICE);
            currentManager.setConnectionListener(resumeListener);
            log.info("opening new connection");
            currentManager.openAudioConnection(voiceChannel);
        } catch (Throwable e) {
          log.error("{}", ExceptionUtils.getStackTrace(e));
        } finally {
            synchronized (locker) {
                locker.isLoading = false;
            }
        }
    }

    private final ConnectionListener resumeListener = new ConnectionListener() {
        @Override
        public void onPing(long ping) {

        }

        @Override
        public void onStatusChange(@Nonnull ConnectionStatus status) {
            log.info("connected event {}", status);
            if (ConnectionStatus.CONNECTED.equals(status)) {
                if (audioPlayer.getPlayingTrack() != null) {
                    audioPlayer.setPaused(false);
                } else {
                    trackQueue.kickConnected();
                }
            } else if (ConnectionStatus.DISCONNECTED_KICKED_FROM_CHANNEL.equals(status)) {
                audioPlayer.stopTrack();
            }
        }
    };

    private Optional<String> validateVoiceState(MessageReceivedEvent event) {
        Member member = event.getMember();
        if (member == null) {
            return Optional.of("sorry, cannot determine user");
        }

        GuildVoiceState voiceState = member.getVoiceState();
        if (voiceState == null) {
            return Optional.of("sorry, %s is not connected to voice".formatted(member.getNickname()));
        }

        if (voiceState.getChannel() == null || !ChannelType.VOICE.equals(voiceState.getChannel().getType())) {
            return Optional.of("sorry, %s is not connected to voice".formatted(member.getNickname()));
        }

        return Optional.empty();
    }
}