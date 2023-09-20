package ru.worm.discord.chill.logic;

import org.springframework.stereotype.Service;
import ru.worm.discord.chill.queue.Track;
import ru.worm.discord.chill.util.Consts;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

@Service
public class AudioInfoStorage {
    private final Path workingDirectory = Paths.get("").toAbsolutePath();
    private final Duration deleteAfter;
    private final Set<FileCash> loadedFiles;

    public AudioInfoStorage() {
        deleteAfter = Duration.of(20, ChronoUnit.MINUTES);
        loadedFiles = new HashSet<>();
    }

    /**
     * загружен ли трек
     */
    public synchronized boolean request(Track track) {
        Optional<FileCash> fileInfo = loadedFiles.stream()
                .filter(info -> info.trackId.equals(track.getId()))
                .findFirst();
        fileInfo.ifPresent(info -> info.requested = Instant.now());
        return fileInfo.isPresent();
    }

    /**
     * добавить трек
     */
    public synchronized void add(Track track) {
        FileCash f = new FileCash();
        f.trackId = track.getId();
        f.requested = Instant.now();
        loadedFiles.add(f);
    }

    //todo syncronized scheduled delete if requested < 20 min

    public static String trackFile(Track track) {
        return "%s/%d".formatted(Consts.OPUS_DOWNLOAD_FOLDER, track.getId());
    }
    public static String trackFileWithExtension(Track track) {
        return "%s.opus".formatted(trackFile(track));
    }

    private static class FileCash {
        private Instant requested;
        private Integer trackId;

        @Override
        public boolean equals(Object o) {
            return this == o;
            //if (this == o) return true;
            //if (o == null || getClass() != o.getClass()) return false;
            //FileCash fileCash = (FileCash) o;
            //return Objects.equals(requested, fileCash.requested) && Objects.equals(trackId, fileCash.trackId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(trackId);
        }
    }
}
