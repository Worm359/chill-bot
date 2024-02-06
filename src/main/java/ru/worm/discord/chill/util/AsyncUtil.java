package ru.worm.discord.chill.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Function;

public class AsyncUtil {
    private final static Logger log = LoggerFactory.getLogger(AsyncUtil.class);
    public static <T> Function<Throwable, T> logError() {
        return throwable -> {
            log.error("{}", ExceptionUtils.getStackTrace(throwable));
            return null;
        };
    }
}
