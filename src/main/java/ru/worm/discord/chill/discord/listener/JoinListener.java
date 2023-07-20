package ru.worm.discord.chill.discord.listener;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.VoiceState;
import discord4j.core.object.entity.Member;
import discord4j.voice.AudioProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.worm.discord.chill.discord.Consts;

import java.time.Duration;

@Service
public class JoinListener extends MessageListener implements EventListener<MessageCreateEvent> {
    private final AudioProvider lavaAudioProvider;

    @Autowired
    public JoinListener(AudioProvider lavaAudioProvider) {
        this.command = Consts.JOIN;
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
                .flatMap(channel -> channel.join(spec -> spec.setProvider(lavaAudioProvider).setTimeout(Duration.ofMinutes(1L))))
                .then();
    }
}