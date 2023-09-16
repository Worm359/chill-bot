package ru.worm.discord.chill.util;

import ru.worm.discord.chill.queue.Track;

public class PathUtil {
    public static String trackFile(Track track) {
        return "%s/%d".formatted(Consts.OPUS_DOWNLOAD_FOLDER, track.getId());
    }
    public static String trackFileWithExtension(Track track) {
        return "%s.opus".formatted(trackFile(track));
    }
}
