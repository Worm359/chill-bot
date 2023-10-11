package ru.worm.discord.chill.logic.locking;

import java.time.Instant;

public class FileCashLock {
    private volatile Instant timeRequested;
    private TrackCashState state = TrackCashState.idle;

    /**
     * Lock can be deleted upon acquiring (if the timing is unlucky).
     * if the method does not throw an exception, and the lock was just acquired
     * #timeRequested has been successfully updated and will not be picked for a deletion
     */
    public synchronized void checkDeletedThrowIfYes() throws AudioCashLockException {
        if (TrackCashState.deleted.equals(state)) throw new AudioCashLockException(state);
    }
    public synchronized void checkIdleIfNotThrow() throws AudioCashLockException {
        if (!TrackCashState.idle.equals(state)) throw new AudioCashLockException(state);
    }
    public synchronized boolean isReady() {
        return this.state.equals(TrackCashState.ready);
    }
    public synchronized TrackCashState getState() {
        return this.state;
    }

    public synchronized void ready() {
        this.state = TrackCashState.ready;
    }

    public synchronized void error() {
        this.state = TrackCashState.error;
    }

    public synchronized void loading() {
        this.state = TrackCashState.loading;
    }
    //synchronized added just in case

    synchronized void deleted() {
        this.state = TrackCashState.deleted;
    }

    void setTimeRequested(Instant timeRequested) {
        this.timeRequested = timeRequested;
    }

    Instant getTimeRequested() {
        return timeRequested;
    }
}
