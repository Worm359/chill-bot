package ru.worm.discord.chill.discord;

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

    public static boolean modeMismatch(long guildId, String command) {
        if (guildId == 0 || Commands.DEV.equalsIgnoreCase(command)) return false;
        boolean modeMismatch = serverIsInDevMode ^ isDevMode(guildId);
        if (modeMismatch) {
            log.debug("server is in dev mode: {}. mode mismatch for guildId={}",
                    serverIsInDevMode,
                    guildId);
        }
        return modeMismatch;
    }

    public static boolean isLockedToAnotherGuildId(Long guildId, String command) {
        if (command.equalsIgnoreCase(Commands.LOCK)
            || command.equalsIgnoreCase(Commands.STAT)
            || command.equalsIgnoreCase(Commands.DEV)) {
            return false;
        }
        Long tmpGuildLock;
        if ((tmpGuildLock = guildIdLock) != null) {
            log.debug("guildIdLock={} message.guildId={}", tmpGuildLock, guildId);
            return !Objects.equals(tmpGuildLock, guildId);

        }
        return false;
    }

    private static enum GuildMode {
        dev, prod
    }
}
