package ru.worm.discord.chill.queue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.worm.discord.chill.queue.event.TrackEventCreator;

import java.util.LinkedList;
import java.util.Optional;

@Component
public class TrackQueue {
    private final TrackEventManager trackMng;
    private final Logger log = LoggerFactory.getLogger(getClass());
    private final LinkedList<Track> queue = new LinkedList<>();
    private final LinkedList<Track> history = new LinkedList<>();
    private Track current;

    @Autowired
    public TrackQueue(TrackEventManager trackMng) {
        this.trackMng = trackMng;
    }

    public synchronized Optional<Track> next() { //boolean skipCurrent
        log.debug("next() event");
        if (!queue.isEmpty() && current != null) { //skipCurrent &&
            Track lastPlayed = queue.remove();
            log.debug("current track is {}, skipping it", lastPlayed.getUrl());
            history.addFirst(lastPlayed);
        }
        if (!queue.isEmpty()) {
            Track next = queue.getFirst();
            current = next;
            log.debug("current track {}", next.getUrl());
            trackMng.dispatchEvent(TrackEventCreator.currentPlayingIs(current));
            return Optional.of(next);
        } else {
            log.debug("no tracks left in queue");
            current = null;
            return Optional.empty();
        }
    }

    private void kick() {
        if (current == null) next();
    }

    public synchronized void playNow(Track track) {
        addNext(track, false);
        next();
    }

    public synchronized void add(Track track) {
        queue.add(track);
        kick();
    }

    public synchronized void addNext(Track track, boolean casual) {
        int position;
        if (queue.isEmpty()) {
            position = 0;
        } else {
            position = 1;
        }
        queue.add(position, track);
        if (casual) kick();
    }

    public synchronized void previous() {

    }

    public synchronized void getHistory() {

    }

    public synchronized void getQueue() {

    }

}
