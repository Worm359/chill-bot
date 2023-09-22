package ru.worm.discord.chill.logic.locking;

public class AudioCashLockException extends Exception {
    private final TrackCashState lastState;

    public AudioCashLockException(TrackCashState lastState) {
        this.lastState = lastState;
    }

    public TrackCashState getLastState() {
        return lastState;
    }
}
