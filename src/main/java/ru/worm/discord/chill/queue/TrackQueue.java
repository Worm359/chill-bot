package ru.worm.discord.chill.queue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.Optional;

@Component
public class TrackQueue {
    private final Logger log = LoggerFactory.getLogger(getClass());
    private final LinkedList<Track> queue = new LinkedList<>();
    private final LinkedList<Track> history = new LinkedList<>();
    private Track current;

    public synchronized Optional<Track> nextFromPlaylist(boolean skipCurrent) {
        log.debug("next() event");
        if (skipCurrent && !queue.isEmpty() && current != null) {
            Track lastPlayed = queue.remove();
            log.debug("current track is {}, skipping it", lastPlayed.getUrl());
            history.addFirst(lastPlayed);
        }
        if (!queue.isEmpty()) {
            Track next = queue.getFirst();
            current = next;
            log.debug("current track {}", next.getUrl());
            return Optional.of(next);
        } else {
            log.debug("no tracks left in queue");
            current = null;
            return Optional.empty();
        }
    }

    public synchronized void add(Track track) {
        queue.add(track);
    }

    public synchronized void previous() {

    }

    public synchronized void getHistory() {

    }

    public synchronized void getQueue() {

    }

}
