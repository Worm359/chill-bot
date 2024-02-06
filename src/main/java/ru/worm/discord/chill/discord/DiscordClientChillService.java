package ru.worm.discord.chill.discord;

import net.dv8tion.jda.api.JDA;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.worm.discord.chill.discord.listener.EventListener;

import java.util.List;

@Service
public class DiscordClientChillService implements InitializingBean {
    private final Logger log = LoggerFactory.getLogger(getClass());
    private final JDA discord;
    private final List<EventListener> eventListeners;

    @Autowired
    public DiscordClientChillService(JDA discord, List<EventListener> eventListeners) {
        this.discord = discord;
        this.eventListeners = eventListeners;
    }

    @Override
    public void afterPropertiesSet() {
        for (EventListener listener : eventListeners) {
            log.info("subscribing to command '{}'...", listener.commandName());
            subscribeListener(listener);
        }
    }

    private void subscribeListener(EventListener listener) {
        //проверка на то что команда подходит слушателю сейчас внутри слушателя, можно перенести сюда (вынеся слушателей в мапу)
        discord.addEventListener(listener);
    }
}
