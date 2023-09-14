package ru.worm.discord.chill.queue;

import java.util.concurrent.atomic.AtomicInteger;

public class Track {
    private static final AtomicInteger ids = new AtomicInteger(0);

    public Track(String url) {
        this.url = url;
        this.id = ids.getAndIncrement();
    }

    private String url;
    private Integer id;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
