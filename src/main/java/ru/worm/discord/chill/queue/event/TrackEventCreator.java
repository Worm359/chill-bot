package ru.worm.discord.chill.queue.event;

import ru.worm.discord.chill.queue.Track;

public class TrackEventCreator {
    public static TrackEvent<Track> currentPlayingIs(Track t) {
        TrackEvent<Track> event = new TrackEvent<>();
        event.setType(TrackEventType.CURRENT_TRACK);
        event.setPayload(t);
        return event;
    }
}
