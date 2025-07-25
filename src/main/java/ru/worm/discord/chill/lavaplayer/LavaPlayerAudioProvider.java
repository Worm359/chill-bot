package ru.worm.discord.chill.lavaplayer;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.playback.MutableAudioFrame;
import net.dv8tion.jda.api.audio.AudioSendHandler;

import javax.annotation.Nullable;
import java.nio.Buffer;
import java.nio.ByteBuffer;


/**
 * from lavaplayer examples
 */
//@Component
public class LavaPlayerAudioProvider implements AudioSendHandler {
    private final AudioPlayer audioPlayer;
    private final ByteBuffer buffer;
    private final MutableAudioFrame frame;

    public LavaPlayerAudioProvider(AudioPlayer player) {
        this.audioPlayer = player;
        this.buffer = ByteBuffer.allocate(1024);
        this.frame = new MutableAudioFrame();
        this.frame.setBuffer(buffer);
    }

    @Override
    public boolean canProvide() {
        return audioPlayer.provide(frame);
    }


    @Nullable
    @Override
    public ByteBuffer provide20MsAudio() {
        ((Buffer) buffer).flip();
        return buffer;
    }

    @Override
    public boolean isOpus() {
        return true;
    }
}
