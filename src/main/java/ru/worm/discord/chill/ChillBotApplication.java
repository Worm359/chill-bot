package ru.worm.discord.chill;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ChillBotApplication {
    private static final Logger log = LoggerFactory.getLogger(ChillBotApplication.class);
    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(ChillBotApplication.class);
        app.run(args);
    }
}