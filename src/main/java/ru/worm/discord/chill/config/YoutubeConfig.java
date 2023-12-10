package ru.worm.discord.chill.config;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.worm.discord.chill.config.settings.RootSettings;
import ru.worm.discord.chill.config.settings.YoutubeSetting;
import ru.worm.discord.chill.util.TextUtil;

import java.io.IOException;
import java.security.GeneralSecurityException;

@Configuration
public class YoutubeConfig {

    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

    private final YoutubeSetting settings;

    public YoutubeConfig(RootSettings settings) {
        this.settings = settings.getYoutube();
    }

    @Bean
    public YouTube youTube() throws GeneralSecurityException, IOException {
        if (settings.getDisabled()) {
            return null;
        }
        if (TextUtil.isEmpty(settings.getApiKey())) {
            throw new IllegalStateException("youtube integration is not disabled. no api key is provided though...");
        }
        final NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        return new YouTube.Builder(httpTransport, JSON_FACTORY, null)
                .setApplicationName("chill-bot")
                .setHttpRequestInitializer(request -> {
                    request.setConnectTimeout(4000);
                    request.setReadTimeout(4000);
                })
                .build();
    }

}
