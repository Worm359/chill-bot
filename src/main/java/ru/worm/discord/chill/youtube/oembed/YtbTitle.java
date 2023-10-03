package ru.worm.discord.chill.youtube.oembed;

import com.fasterxml.jackson.annotation.JsonProperty;

public class YtbTitle {
    @JsonProperty
    private String title;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
