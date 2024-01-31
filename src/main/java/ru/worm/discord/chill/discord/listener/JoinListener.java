package ru.worm.discord.chill.discord.listener;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.VoiceState;
import discord4j.core.object.entity.Member;
import discord4j.core.spec.VoiceChannelJoinSpec;
import discord4j.voice.AudioProvider;
import discord4j.voice.VoiceConnection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.worm.discord.chill.discord.Commands;

import java.time.Duration;

@Service
public class JoinListener extends MessageListener implements EventListener<MessageCreateEvent> {
    private final AudioProvider lavaAudioProvider;
    private final AudioPlayer audioPlayer;
    private final Locker locker = new Locker();
    private VoiceConnection currentVoiceConnection = null;

    /**
     * реализовать AudioProvider самому не получилось, см. ru.worm.discord.chill.ffmpeg.FfmpegAudioProvider
     */
    @Autowired
    public JoinListener(@Qualifier("lavaAudioProvider") AudioProvider lavaAudioProvider, AudioPlayer audioPlayer) {
        this.audioPlayer = audioPlayer;
        this.command = Commands.JOIN;
        this.lavaAudioProvider = lavaAudioProvider;
    }

    @Override
    public Class<MessageCreateEvent> getEventType() {
        return MessageCreateEvent.class;
    }

    private static class Locker {
        private boolean isLoading = false;
    }

    public Mono<Void> execute(MessageCreateEvent event) {
        return filter(event.getMessage())
                .map(m -> event)
                .flatMap(e -> Mono.justOrEmpty(e.getMember()))
                .flatMap(Member::getVoiceState)
                .flatMap(VoiceState::getChannel)
                .flatMap(channel -> {
                    Mono<Void> disconnect = Mono.empty();
                    synchronized (locker) {
                        if (locker.isLoading) {
                            return event.getMessage()
                                    .getChannel()
                                    .map(ch -> ch.createMessage("sorry. already processing another join"));
                        }
                        locker.isLoading = true;
                        if (currentVoiceConnection != null) {
                            audioPlayer.setPaused(true);
                            disconnect = currentVoiceConnection.disconnect();
                            this.currentVoiceConnection = null; //?
                        }
                    }
                    return disconnect.then(
                                    channel.join(VoiceChannelJoinSpec.builder()
                                                    .provider(lavaAudioProvider)
                                                    .selfDeaf(false)
                                                    .selfMute(false)
                                                    .timeout(Duration.ofMinutes(1L))
                                                    .ipDiscoveryTimeout(Duration.ofSeconds(10))
                                                    .build())
                                            .doOnNext(vc -> this.currentVoiceConnection = vc))
                            .doFinally(sig -> {
                                synchronized (locker) {
                                    locker.isLoading = false;
                                    audioPlayer.setPaused(false); //fixme slow start...?
                                }
                            });
                })
                .then();
    }
}