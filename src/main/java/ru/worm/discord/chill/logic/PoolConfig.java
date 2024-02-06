package ru.worm.discord.chill.logic;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PoolConfig {
    // for long running operations inside event receiving
    public static final ExecutorService forEvents = Executors.newFixedThreadPool(2);
}
