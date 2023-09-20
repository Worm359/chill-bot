package ru.worm.discord.chill.queue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import ru.worm.discord.chill.queue.event.ITrackQSubscriber;
import ru.worm.discord.chill.queue.event.TrackEvent;

import java.util.List;

@Component
public class TrackEventManager {
    private final Logger log = LoggerFactory.getLogger(getClass());
    private final List<ITrackQSubscriber> subscribers;

    public TrackEventManager(List<ITrackQSubscriber> subscribers) {
        this.subscribers = subscribers;
    }

    <T> void dispatchEvent(TrackEvent<T> event) {
        log.debug("TrackEvent {}", event.getType().name());
        subscribers.forEach(s -> s.process(event));
    }
}
