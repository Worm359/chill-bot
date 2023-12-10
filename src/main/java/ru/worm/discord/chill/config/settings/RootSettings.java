package ru.worm.discord.chill.config.settings;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

@Component
@PropertySource("file:${spring.config.location}/application.properties")
@ConfigurationProperties("chill-bot")
public class RootSettings {
    DiscordSetting discord;
    YoutubeSetting youtube = new YoutubeSetting();

    public DiscordSetting getDiscord() {
        return discord;
    }

    public void setDiscord(DiscordSetting discord) {
        this.discord = discord;
    }

    public YoutubeSetting getYoutube() {
        return youtube;
    }

    public void setYoutube(YoutubeSetting youtube) {
        this.youtube = youtube;
    }
}
