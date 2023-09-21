package ru.worm.discord.chill.logic.locking;

import java.time.Instant;

public class FileCashLock {
    private volatile Instant timeRequested;
    private boolean loaded = false;
    private boolean deleted = false;

    public synchronized void checkDeleted() throws AudioCashLockException {
        if (deleted) throw new AudioCashLockException();
    }

    //synchronized added just in case
    public synchronized void setLoaded() {
        this.loaded = true;
    }

    //synchronized added just in case
    public synchronized boolean isLoaded() {
        return this.loaded;
    }

    synchronized void setDeleted() {
        this.deleted = true;
    }

    public void setTimeRequested(Instant timeRequested) {
        this.timeRequested = timeRequested;
    }

    public Instant getTimeRequested() {
        return timeRequested;
    }
}
