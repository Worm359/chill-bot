package ru.worm.discord.chill.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.worm.discord.chill.config.settings.RootSettings;
import ru.worm.discord.chill.discord.IWithPrefix;
import ru.worm.discord.chill.discord.listener.ITextCommand;

import java.util.List;

/**
 * для модификации слушателей перед их использованием
 */
@Component
public class ListenerModifier {

    @Autowired
    public ListenerModifier(RootSettings settings, List<ITextCommand> discordListeners) {
        modifyPrefix(settings.getDiscord().getPrefix(), discordListeners);
    }

    private void modifyPrefix(String prefix, List<ITextCommand> collectionOfBeans) {
        for (ITextCommand listener : collectionOfBeans) {
            if (listener instanceof IWithPrefix) {
                ((IWithPrefix) listener).setBotPrefix(prefix);
            }
        }
    }
}