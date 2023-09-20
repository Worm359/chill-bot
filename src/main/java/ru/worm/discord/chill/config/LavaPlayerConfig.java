package ru.worm.discord.chill.config;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.track.playback.NonAllocatingAudioFrameBuffer;
import discord4j.voice.AudioProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.worm.discord.chill.lavaplayer.LavaPlayerAudioProvider;
import ru.worm.discord.chill.lavaplayer.LavaPlayerEventListener;

@Configuration
public class LavaPlayerConfig {

    /**
     * lavaplayer
     */
    @Bean
    public AudioPlayerManager playerManager() {
        final AudioPlayerManager playerManager = new DefaultAudioPlayerManager();

        // This is an optimization strategy that Discord4J can utilize.
        // It is not important to understand
        playerManager.getConfiguration()
                .setFrameBufferFactory(NonAllocatingAudioFrameBuffer::new);

        // Allow playerManager to parse remote sources like YouTube links
        AudioSourceManagers.registerRemoteSources(playerManager);
        AudioSourceManagers.registerLocalSource(playerManager);
        return playerManager;
    }

    /**
     * lavaplayer
     */
    @Autowired
    @Bean
    public AudioPlayer lavaPlayer(AudioPlayerManager playerManager, LavaPlayerEventListener lavaListener) {
        // Create an AudioPlayer so Discord4J can receive audio data
        AudioPlayer player = playerManager.createPlayer();
        player.addListener(lavaListener);
        return player;
    }

    /**
     * our type - link between discord4j and lavaplayer
     */
    @Autowired
    @Bean("lavaAudioProvider")
    public AudioProvider audioProvider(AudioPlayer lavaPlayer) {
        return new LavaPlayerAudioProvider(lavaPlayer);
    }
}
