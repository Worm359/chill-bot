package ru.worm.discord.chill.logic;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class PoolConfig {
    // for long running operations inside event receiving
    public static final ExecutorService forEvents = Executors.newFixedThreadPool(2);
    //for orchestrator: checking track file, initializing load process
    public static final ExecutorService orchestratorExecutor = Executors.newSingleThreadScheduledExecutor();
    //for tracks, waiting to be loaded/discarded
    public static final ScheduledExecutorService trackLoadWaiter = Executors.newSingleThreadScheduledExecutor();
    //for process output logging
    public static final ExecutorService processOutputLogger = Executors.newSingleThreadExecutor();
}
