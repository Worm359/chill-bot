package ru.worm.discord.chill.logic.locking;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

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

    //todo example outer method
    //void somethingThatNeedsNamedLocks(Integer name) {
    //    LockOnDemand lock = getLock(name);
    //    synchronized (lock) {
    //        lock.checkDeleted();
    //        //load;
    //        lock.setLoaded();
    //    }
    //}

    public boolean checkFilePresent(Integer id) {
        FileCashLock lock = getLock(id);
        synchronized (lock) {
            try {
                lock.isDeleted();
                return lock.isReady();
            } catch (AudioCashLockException e) {
                return false;
            }
        }
    }

    //scheduled
    @SuppressWarnings("synchronized")
    void deleteOldLocks() {
        Iterator<Map.Entry<Integer, FileCashLock>> iterator = locks.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Integer, FileCashLock> entry = iterator.next();
            FileCashLock lock = entry.getValue();
            synchronized (lock) {
                //timeRequested may be null, if it is newly created lock
                //which was put in map, but didn't synchronize before delete get called
                if (lock.getTimeRequested() != null && (Instant.now().toEpochMilli() - lock.getTimeRequested().toEpochMilli()) > timeoutMs) {
                    //todo delete file
                    lock.deleted();
                    iterator.remove();
                }
            }
        }
    }
}
