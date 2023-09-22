package ru.worm.discord.chill.logic;

import ru.worm.discord.chill.queue.Track;
import ru.worm.discord.chill.util.Consts;

public class AudioFilePath {
    public static String trackFile(Track track) {
        return trackFile(track.getId());
    }

    public static String trackFile(Integer id) {
        return "%s/%d".formatted(Consts.OPUS_DOWNLOAD_FOLDER, id);
    }

    public static String trackFileWithExtension(Track track) {
        return trackFileWithExtension(track.getId());
    }

    public static String trackFileWithExtension(Integer id) {
        return "%s.opus".formatted(trackFile(id));
    }
}
