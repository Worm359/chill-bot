package ru.worm.discord.chill.queue.event;

import ru.worm.discord.chill.queue.Track;

public interface ITrackQSubscriber {

    @SuppressWarnings("unchecked")
    default <T> void process(TrackEvent<T> event) {
        if (TrackEventType.CURRENT_TRACK.equals(event.getType())) {
            currentChanged((TrackEvent<Track>) event);
        } else if (TrackEventType.NEW_TRACK.equals(event.getType())) {
            newTrackAdded((TrackEvent<Track>) event);
        }
    }

    default void currentChanged(TrackEvent<Track> event) {
    }

    default void newTrackAdded(TrackEvent<Track> event) {
    }
}
