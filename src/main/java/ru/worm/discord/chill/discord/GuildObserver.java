package ru.worm.discord.chill.discord;

import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class GuildObserver {
    private static final Logger log = LoggerFactory.getLogger(GuildObserver.class);
    private static boolean serverIsInDevMode = false;
    public static volatile Long guildIdLock;
    private static final Map<Long, GuildMode> guildModes = new ConcurrentHashMap<>();

    @Autowired
    public GuildObserver(Environment environment) {
        String[] activeProfiles = environment.getActiveProfiles();
        serverIsInDevMode = Arrays
                .stream(activeProfiles)
                .anyMatch("dev"::equalsIgnoreCase);
    }

    public static void setDevMode(long guildId) {
        guildModes.put(guildId, GuildMode.dev);
    }

    public static void discardDevMode(long guildId) {
        guildModes.put(guildId, GuildMode.prod);
    }

    private static boolean isDevMode(long guildId) {
        GuildMode guildMode = guildModes.get(guildId);
        return guildMode != null && guildMode.equals(GuildMode.dev);
    }

    public static boolean modeMismatch(Message message) {
        Long msgGuildId = message.getGuildId()
                    .map(Snowflake::asLong)
                    .orElse(null);
        if (msgGuildId == null) return false;
        boolean modeMismatch = serverIsInDevMode ^ isDevMode(msgGuildId);
        if (modeMismatch) {
            log.debug("server is in dev mode: {}. mode mismatch for msgGuildId={}",
                    serverIsInDevMode,
                    msgGuildId);
        }
        return modeMismatch;
    }

    public static boolean isLockedToAnotherGuildId(Message message, String command) {
        if (command.equalsIgnoreCase(Commands.LOCK) || command.equalsIgnoreCase(Commands.STAT)) {
            return false;
        }
        Long tmpGuildLock;
        if ((tmpGuildLock = guildIdLock) != null) {
            Long msgGuildId = message.getGuildId()
                    .map(Snowflake::asLong)
                    .orElse(null);
            log.debug("guildIdLock={} message.guildId={}", tmpGuildLock, msgGuildId);
            return !Objects.equals(tmpGuildLock, msgGuildId);

        }
        return false;
    }

    private static enum GuildMode {
        dev, prod
    }
}
