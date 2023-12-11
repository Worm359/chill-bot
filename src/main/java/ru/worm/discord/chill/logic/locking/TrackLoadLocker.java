package ru.worm.discord.chill.logic.locking;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ru.worm.discord.chill.util.ExceptionUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

import static ru.worm.discord.chill.logic.AudioFilePath.trackFilePart;
import static ru.worm.discord.chill.logic.AudioFilePath.trackFileWithExtension;

/**
 * Why this, instead of single queue for loading files:<br>
 * 1. Assume we want to load 3 next files beforehand.
 * Current track started playing, 3 others are loading.
 * I press shuffle. Now I'm gonna wait 4 times more before my random track is loaded.
 * I could also cancel loading, but it requires code, and I am probably gonna listen to those tracks anyway.
 * So, I don't have good reason to do cancellation.
 * Also, after shuffle next track could swap to the one that just started loading.
 * 2. Assume I don't load tracks beforehand. Could my track be in queue two times in a row? I press next...
 * 3. Other options: synchronize on a Track object?
 */
@Service
public class TrackLoadLocker {
    private final Logger log = LoggerFactory.getLogger(getClass());
    private long timeoutMs = Duration.ofMinutes(15).toMillis();
    private final ConcurrentMap<Integer, FileCashLock> locks = new ConcurrentHashMap<>();

    public TrackLoadLocker(Duration d) {
        this.timeoutMs = d.toMillis();
    }

    public TrackLoadLocker() {
    }

    public FileCashLock getLock(Integer id) {
        log.debug("acquiring lock id={}", id);
        FileCashLock lock = locks.get(id);
        if (lock == null) {
            FileCashLock newLock = new FileCashLock();
            lock = locks.putIfAbsent(id, newLock);
            if (lock == null) {
                lock = newLock;
            }
        }
        //time requested - the last time track.id was requested from the cash
        lock.setTimeRequested(Instant.now()); //volatile
        return lock;
    }

    public boolean checkFilePresent(Integer id) {
        FileCashLock lock = getLock(id);
        synchronized (lock) {
            try {
                lock.checkDeletedThrowIfYes();
                return lock.isReady();
            } catch (AudioCashLockException e) {
                return false;
            }
        }
    }

    @Scheduled(fixedDelay = 25, timeUnit = TimeUnit.MINUTES)
    void deleteOldLocks() {
        log.debug("cleaning old files cash");
        long nowMilli = Instant.now().toEpochMilli();
        Iterator<Map.Entry<Integer, FileCashLock>> iterator = locks.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Integer, FileCashLock> entry = iterator.next();
            FileCashLock lock = entry.getValue();
            synchronized (lock) {
                //timeRequested may be null, if it is newly created lock
                //which was put in map, but didn't synchronize before delete get called
                if (lock.getTimeRequested() != null && (nowMilli - lock.getTimeRequested().toEpochMilli()) > timeoutMs) {
                    tryDeleteFile(entry.getKey());
                    lock.deleted();
                    iterator.remove();
                }
            }
        }
    }

    protected void tryDeleteFile(Integer key) {
        try {
            Path file = Paths.get(trackFileWithExtension(key));
            Path filePart = Paths.get(trackFilePart(key));
            log.debug("deleting id={} {}/{}", key, file.toAbsolutePath(), filePart.toAbsolutePath());
            Files.deleteIfExists(file);
            Files.deleteIfExists(filePart);
        } catch (IOException e) {
            log.error("FATAL: file not deleted: {}", ExceptionUtils.getStackTrace(e));
        }
    }
}
