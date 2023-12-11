package ru.worm.discord.chill.queue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.worm.discord.chill.queue.event.TrackEventCreator;

import java.util.*;
import java.util.stream.Stream;

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

    public synchronized Track newTrack(String url) {
        return Stream.concat(history.stream(), queue.stream())
                .filter(t -> t.getUrl().equals(url))
                .findFirst()
                .orElseGet(() -> new Track(url));
    }

    public synchronized Optional<Track> getTrackById(Integer id) {
        return Stream.concat(history.stream(), queue.stream())
                .filter(t -> t.getId().equals(id))
                .findFirst();
    }

    public synchronized List<Track> getHistory() {
        return new ArrayList<>(history);
    }

    public synchronized List<Track> getPlaylist() {
        return new ArrayList<>(queue);
    }

    public synchronized void next() { //boolean skipCurrent
        log.debug("next() event");
        if (!queue.isEmpty() && current != null) { //skipCurrent &&
            Track lastPlayed = queue.remove();
            log.debug("current track is {}, skipping it", lastPlayed.getUrl());
            addToHistory(lastPlayed);
        }
        if (!queue.isEmpty()) {
            Track next = queue.getFirst();
            current = next;
            log.debug("current track {}", next.getUrl());
            trackMng.dispatchEvent(TrackEventCreator.currentPlayingIs(current));
        } else {
            log.debug("no tracks left in queue");
            current = null;
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
        trackMng.dispatchEvent(TrackEventCreator.newTrackAdded(track));
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
        trackMng.dispatchEvent(TrackEventCreator.newTrackAdded(track));
        if (casual) kick();
    }

    public synchronized void remove(Integer id) {
        log.debug("removing {}", id);
        int i = 0;
        for (Iterator<Track> iterator = queue.iterator(); iterator.hasNext(); ) {
            Track track = iterator.next();
            if (!track.getId().equals(id) || i == 0) {
                i++;
                continue;
            }
            iterator.remove();
            log.debug("removed {} successfully", id);
            break;
        }
    }

    public synchronized void skipTo(int id) {
        log.debug("removing to {}", id);
        int indexOf = -1;
        for (int i = 0; i < queue.size(); i++) {
            Track track = queue.get(i);
            if (track.getId().equals(id)) {
                indexOf = i;
            }
        }
        if (indexOf == -1 || indexOf == 0) {
            return;
        }
        Iterator<Track> iterator = queue.iterator();
        int i = 0;
        while (iterator.hasNext() ) {
            Track track = iterator.next();
            if (i == (indexOf - 1)) {
                break;
            }
            addToHistory(track);
            iterator.remove();
            i++;
        }
        next();
    }

    public synchronized void previous() {
        if (!history.isEmpty()) {
            Track track = history.removeFirst();
            queue.addFirst(track);
            current = track;
            trackMng.dispatchEvent(TrackEventCreator.currentPlayingIs(current));
        } else if (current != null) {
            trackMng.dispatchEvent(TrackEventCreator.currentPlayingIs(current));
        }
    }

    private void addToHistory(Track track) {
        history.addFirst(track);
        if (history.size() > 20) history.removeLast();
    }
}
