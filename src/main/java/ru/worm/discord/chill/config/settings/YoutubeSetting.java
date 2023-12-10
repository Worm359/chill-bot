package ru.worm.discord.chill.config.settings;

public class YoutubeSetting {
    private String apiKey;
    private Boolean disabled = false;
    private Long maximumVideoLengthMinutes = 25L;

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public Boolean getDisabled() {
        return disabled;
    }

    public void setDisabled(Boolean disabled) {
        this.disabled = disabled;
    }

    public Long getMaximumVideoLengthMinutes() {
        return maximumVideoLengthMinutes;
    }

    public void setMaximumVideoLengthMinutes(Long maximumVideoLengthMinutes) {
        this.maximumVideoLengthMinutes = maximumVideoLengthMinutes;
    }
}
