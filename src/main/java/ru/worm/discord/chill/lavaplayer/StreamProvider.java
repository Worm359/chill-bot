package ru.worm.discord.chill.lavaplayer;

import com.sedmelluq.discord.lavaplayer.container.ogg.OggAudioTrack;
import com.sedmelluq.discord.lavaplayer.tools.io.NonSeekableInputStream;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import ru.worm.discord.chill.queue.Track;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static ru.worm.discord.chill.logic.AudioInfoStorage.trackFileWithExtension;

public class StreamProvider {
    private static Path path(Track track) {
        return Paths.get("").toAbsolutePath().resolve(trackFileWithExtension(track));
    }

    private static InputStream inputStreamFromFile(Track track) {
        Path file = path(track);
        //get input stream from file
        try {
            return Files.newInputStream(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static OggAudioTrack getOggTrack(Track track) {
        NonSeekableInputStream nonSeekableIS = new NonSeekableInputStream(inputStreamFromFile(track));
        AudioTrackInfo trackInfo = new AudioTrackInfo(
                "unknown",
                "unknown",
                144000,
                "identifier",
                false,
                path(track).toAbsolutePath().toString());
        return new OggAudioTrack(trackInfo, nonSeekableIS);
    }
}
