package ru.worm.discord.chill.discord.listener;

import discord4j.core.event.domain.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;
import ru.worm.discord.chill.util.ExceptionUtils;

public interface EventListener<T extends Event> {

    Logger log = LoggerFactory.getLogger(EventListener.class);
    
    Class<T> getEventType();
    Mono<Void> execute(T event);
    String commandName();
    
    default Mono<Void> handleError(Throwable error) {
        log.error("Unable to process {} stacktrace {}", getEventType().getSimpleName(), ExceptionUtils.getStackTrace(error));
        return Mono.empty();
    }
}