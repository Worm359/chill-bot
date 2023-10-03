package ru.worm.discord.chill.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@Configuration
public class RestTemplateConfig {
    private static final Duration timeout = Duration.ofSeconds(4);

    @Autowired
    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder standardBuilder) {
        return standardBuilder
                .setConnectTimeout(timeout)
                .setReadTimeout(timeout)
                .build();
    }
}
