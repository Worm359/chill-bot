package ru.worm.discord.chill.config;

import discord4j.core.event.domain.Event;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.worm.discord.chill.config.settings.RootSettings;
import ru.worm.discord.chill.discord.IWithPrefix;
import ru.worm.discord.chill.discord.listener.EventListener;

import java.util.List;

/**
 * для модификации слушателей перед их использованием
 */
@Component
public class ListenerModifier {

    @Autowired
    public <T extends Event> ListenerModifier(RootSettings settings, List<EventListener<T>> discordListeners) {
        modifyPrefix(settings.getDiscord().getPrefix(), discordListeners);
    }

    private <T extends Event> void modifyPrefix(String prefix, List<EventListener<T>> collectionOfBeans) {
        for (EventListener<T> listener : collectionOfBeans) {
            if (listener instanceof IWithPrefix) {
                ((IWithPrefix) listener).setBotPrefix(prefix);
            }
        }
    }
}