package ru.worm.discord.chill.util;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class YoutubeUtil {
    private static final Pattern ytbLinkSimple = Pattern.compile(
            "youtu\\.be/([\\-0-9a-zA-Z_]+)$"
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

    private static final Pattern playlistIdPattern = Pattern.compile("(?:\\?|&)list=([a-zA-Z0-9_-]+)(?:&|$)");

    public static void main(String[] args) {
        {
            String url = "https://www.somesite.com/playlist?list=PLu5udog-280Pf1_LE9j83m0INJi3ZkWu8";
            Matcher matcher = playlistIdPattern.matcher(url);
            System.out.println(matcher.find());
            System.out.println(matcher.group());
            System.out.println(matcher.group(1));
        }
        {
            String url = "https://www.somesite.com/watch?v=Mu9G2zm1PhE&list=PLu5udog-280Pf1_LE9j83m0INJi3ZkWu8&index=1";
            Matcher matcher = playlistIdPattern.matcher(url);
            System.out.println(matcher.find());
            System.out.println(matcher.group());
            System.out.println(matcher.group(1));
        }

    }

    public static Optional<String> stripPlaylistId(String url) {
        Matcher matcher = playlistIdPattern.matcher(url);
        if (matcher.find()) {
            return Optional.ofNullable(matcher.group(1));
        }
        return Optional.empty();
    }

    public static String urlForVideoId(String videoId) {
        assert !TextUtil.isEmpty(videoId);
        return "https://youtube.com/watch?v=%s".formatted(videoId);
    }
}
