package ru.worm.discord.chill.queue;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

public class Track {
    private static final AtomicInteger ids = new AtomicInteger(0);

    public Track(String url, String title, Duration duration) {
        this.id = ids.incrementAndGet();
        this.videoId = url;
        this.title = title;
        this.duration = duration;
    }

    private String videoId;
    private String title;
    private Duration duration;
    private Integer id;

    public String getVideoId() {
        return videoId;
    }

    public String getTitle() {
        return title;
    }

    public Duration getDuration() {
        return duration;
    }

    void setTitle(String title) {
        this.title = title;
    }

    public Integer getId() {
        return id;
    }

    @Override
    public String toString() {
        return "Track{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", videoId='" + videoId + '\'' +
                ", duration=" + (duration != null ? duration.toMinutes() : null) + " min." +
                '}';
    }
}
