package ru.worm.discord.chill.config;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.track.playback.NonAllocatingAudioFrameBuffer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.worm.discord.chill.config.settings.LavaPlayerSetting;
import ru.worm.discord.chill.config.settings.RootSettings;
import ru.worm.discord.chill.lavaplayer.LavaPlayerEventListener;

@Configuration
public class LavaPlayerConfig {

    private final LavaPlayerSetting settings;

    public LavaPlayerConfig(RootSettings settings) {
        this.settings = settings.getLavaPlayer();
    }

    /**
     * lavaplayer
     */
    @Bean
    public AudioPlayerManager playerManager() {
        final AudioPlayerManager playerManager = new DefaultAudioPlayerManager();

        // This is an optimization strategy that Discord library can utilize.
        // It is not important to understand
        playerManager.getConfiguration()
                .setFrameBufferFactory(NonAllocatingAudioFrameBuffer::new);
        //Allow playerManager to parse remote sources like YouTube links
        if (settings.getUseRemote()) {
            AudioSourceManagers.registerRemoteSources(playerManager);
        }
        AudioSourceManagers.registerLocalSource(playerManager);
        return playerManager;
    }

    /**
     * lavaplayer
     */
    @Autowired
    @Bean
    public AudioPlayer lavaPlayer(AudioPlayerManager playerManager, LavaPlayerEventListener lavaListener) {
        // Create an AudioPlayer so Discord can receive audio data
        AudioPlayer player = playerManager.createPlayer();
        player.addListener(lavaListener);
        return player;
    }
}
