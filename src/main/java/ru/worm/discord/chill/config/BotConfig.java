package ru.worm.discord.chill.config;

import discord4j.core.DiscordClientBuilder;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.worm.discord.chill.config.settings.ChillBotSettings;
import ru.worm.discord.chill.discord.EventListener;

import java.util.List;

@Configuration
public class BotConfig {
    private final Logger log = LoggerFactory.getLogger(getClass());

    private final String token;

    @Autowired
    public BotConfig(ChillBotSettings settings) {
        token = settings.getDiscord().getToken();
        assert (token != null);
    }

    @Bean
    public <T extends Event> GatewayDiscordClient gatewayDiscordClient(List<EventListener<T>> eventListeners) {
        GatewayDiscordClient client = DiscordClientBuilder.create(token)
                .build()
                .login()
                .block();

        if (client == null) throw new IllegalStateException("couldn't initialize GatewayDiscordClient");

        for (EventListener<T> listener : eventListeners) {
            client.on(listener.getEventType())
                    .flatMap(listener::execute)
                    .onErrorResume(listener::handleError)
                    .subscribe();
        }
        log.info("initialized discord client");
        return client;
    }
}
