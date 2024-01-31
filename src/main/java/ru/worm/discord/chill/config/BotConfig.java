package ru.worm.discord.chill.config;

import discord4j.core.DiscordClientBuilder;
import discord4j.core.GatewayDiscordClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import ru.worm.discord.chill.config.settings.RootSettings;
import ru.worm.discord.chill.util.ExceptionUtils;

import java.time.Instant;

@Configuration
public class BotConfig {
    private final Logger log = LoggerFactory.getLogger(getClass());

    private final String token;

    @Autowired
    public BotConfig(RootSettings settings) {
        token = settings.getDiscord().getToken();
        assert (token != null);
    }

    @Bean
    @DependsOn({"lavaAudioProvider"})
    public GatewayDiscordClient gatewayDiscordClient() {
        GatewayDiscordClient discord = null;
        try {
            discord = DiscordClientBuilder.create(token).build().login().block();
        } catch (Exception e) {
            log.error("couldn't initialize GatewayDiscordClient {}", ExceptionUtils.getStackTrace(e));
            System.exit(-1);
        }
        if (discord == null) {
            log.error("couldn't initialize GatewayDiscordClient");
            System.exit(-1);
        }
        log.info("successfully connected to discord gateway");
        return discord;
    }

    @Bean("launchTimestamp")
    public Instant launchTimestamp() {
        return Instant.now();
    }
}
