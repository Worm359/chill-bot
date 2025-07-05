package ru.worm.discord.chill.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class CommandLineLoggerV2 {
    private static final Logger log = LoggerFactory.getLogger(CommandLineLoggerV2.class);
    private final InputStream io;
    private String identifier = "[unknown]";

    public CommandLineLoggerV2(InputStream io) {
        this.io = io;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public void log(Executor executor) {
        CompletableFuture.runAsync(() -> {
            try (InputStreamReader inputStreamReader = new InputStreamReader(io);
                 BufferedReader bufferedReader = new BufferedReader(inputStreamReader)) {
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    log.debug("{}: {}", identifier, line); // Replace with logger if needed
                }
            } catch (IOException e) {
                log.error("Error reading input stream: {}", ExceptionUtils.getStackTrace(e));
            }
        }, executor);
    }
}
