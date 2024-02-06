package ru.worm.discord.chill.lavaplayer;

import com.sedmelluq.discord.lavaplayer.format.StandardAudioDataFormats;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.playback.MutableAudioFrame;
import net.dv8tion.jda.api.audio.AudioSendHandler;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.nio.ByteBuffer;

@Component
public class LavaPlayerAudioProvider implements AudioSendHandler {
    private final AudioPlayer player;
    private final MutableAudioFrame frame = new MutableAudioFrame();

    private final ByteBuffer buffer;

    public LavaPlayerAudioProvider(AudioPlayer player) {
        this.player = player;
        this.buffer = ByteBuffer.allocate(
                StandardAudioDataFormats.DISCORD_OPUS.maximumChunkSize()
            );
        this.frame.setBuffer(buffer);
    }

    @Override
    public boolean canProvide() {
        // AudioPlayer writes audio data to its AudioFrame
        final boolean didProvide = player.provide(frame);
        // If audio was provided, flip from write-mode to read-mode
        if (didProvide) {
            provide20MsAudio().flip();
        }
        return didProvide;
    }


    @Nullable
    @Override
    public ByteBuffer provide20MsAudio() {
        return buffer;
    }

    @Override
    public boolean isOpus() {
        return true;
    }
}
