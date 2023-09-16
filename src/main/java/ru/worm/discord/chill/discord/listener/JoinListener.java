package ru.worm.discord.chill.discord.listener;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.VoiceState;
import discord4j.core.object.entity.Member;
import discord4j.core.spec.VoiceChannelJoinSpec;
import discord4j.voice.AudioProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.worm.discord.chill.discord.Commands;

import java.time.Duration;

@Service
public class JoinListener extends MessageListener implements EventListener<MessageCreateEvent> {
    private final AudioProvider lavaAudioProvider;

    /**
     * реализовать AudioProvider самому не получилось, см. ru.worm.discord.chill.ffmpeg.FfmpegAudioProvider
     */
    @Autowired
    public JoinListener(@Qualifier("lavaAudioProvider") AudioProvider lavaAudioProvider) {
        this.command = Commands.JOIN;
        this.lavaAudioProvider = lavaAudioProvider;
    }

    @Override
    public Class<MessageCreateEvent> getEventType() {
        return MessageCreateEvent.class;
    }

    public Mono<Void> execute(MessageCreateEvent event) {
        return filter(event.getMessage())
                .map(m -> event)
                .flatMap(e -> Mono.justOrEmpty(e.getMember()))
                .flatMap(Member::getVoiceState)
                .flatMap(VoiceState::getChannel)
                // join returns a VoiceConnection which would be required if we were
                // adding disconnection features, but for now we are just ignoring it.
                // с такими таймаутами видно что падает из-за неудавшегося UDP соединения
                .flatMap(channel -> channel.join(VoiceChannelJoinSpec.builder()
                        .provider(lavaAudioProvider)
                        .selfDeaf(false)
                        .selfMute(false)
                        .timeout(Duration.ofMinutes(1L))
                        .ipDiscoveryTimeout(Duration.ofSeconds(10))
                        .build()))
                .then();
    }
}