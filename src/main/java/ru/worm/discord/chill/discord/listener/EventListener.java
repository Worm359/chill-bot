package ru.worm.discord.chill.discord.listener;

import net.dv8tion.jda.api.events.GenericEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface EventListener extends net.dv8tion.jda.api.hooks.EventListener {

    Logger log = LoggerFactory.getLogger(EventListener.class);
    void onEvent(GenericEvent event);
    String commandName();
}