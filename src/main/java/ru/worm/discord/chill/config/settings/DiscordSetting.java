package ru.worm.discord.chill.config.settings;

public class DiscordSetting {
    private String token;
    private String prefix;
    private boolean useJdaNas;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public boolean isUseJdaNas() {
        return useJdaNas;
    }

    public void setUseJdaNas(boolean useJdaNas) {
        this.useJdaNas = useJdaNas;
    }
}
