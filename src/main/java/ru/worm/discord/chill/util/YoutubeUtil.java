package ru.worm.discord.chill.util;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class YoutubeUtil {
    private static final Pattern ytbLinkSimple = Pattern.compile(
            "youtu\\.be/([0-9a-zA-Z]+)$"
    );
    private static final Pattern ytbLinkSecondGuess = Pattern.compile(
            "(?<=watch\\?v=|/videos/|embed\\/)[^#\\&\\?]*"
    );


    public static Optional<String> stripVideoUrl(String url) {
        Matcher matcher = ytbLinkSimple.matcher(url);
        if (matcher.find()) {
            return Optional.ofNullable(matcher.group(1));
        }
        matcher = ytbLinkSecondGuess.matcher(url);
        if (matcher.find()) {
            return Optional.ofNullable(matcher.group());
        }
        return Optional.empty();
    }
}
