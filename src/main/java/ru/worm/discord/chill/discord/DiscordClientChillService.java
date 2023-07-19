package ru.worm.discord.chill.discord;

import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.Event;
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
    private final GatewayDiscordClient discord;
    private final List<EventListener<? extends Event>> eventListeners;

    @Autowired
    public DiscordClientChillService(GatewayDiscordClient discord, List<EventListener<? extends Event> > eventListeners) {
        this.discord = discord;
        this.eventListeners = eventListeners;
    }

    @Override
    public void afterPropertiesSet() {
        for (EventListener<?> listener : eventListeners) {
            log.info("subscribing to command '{}'...", listener.commandName());
            subscribeListener(listener);
        }
    }

    private <T extends Event> void subscribeListener(EventListener<T> listener) {
        //проверка на то что команда подходит слушателю сейчас внутри слушателя, можно перенести сюда (вынеся слушателей в мапу)
        discord.on(listener.getEventType())
                    .flatMap(listener::execute)
                    .onErrorResume(listener::handleError)
                    .subscribe();
    }
}
