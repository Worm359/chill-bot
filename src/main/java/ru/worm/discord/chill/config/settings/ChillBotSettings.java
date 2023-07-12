package ru.worm.discord.chill.config.settings;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

@Component
@PropertySource("file:${spring.config.location}/application.properties")
@ConfigurationProperties("chill-bot")
public class ChillBotSettings {

}
