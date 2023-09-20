package ru.worm.discord.chill.queue.event;

public class TrackEvent<T> {
    private TrackEventType type;
    private T payload;

    public TrackEventType getType() {
        return type;
    }

    public void setType(TrackEventType type) {
        this.type = type;
    }

    public T getPayload() {
        return payload;
    }

    public void setPayload(T payload) {
        this.payload = payload;
    }
}
