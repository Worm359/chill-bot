package ru.worm.discord.chill.logic.locking;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.stereotype.Service;
import ru.worm.discord.chill.logic.PoolConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
public class LoadEventHandler implements DisposableBean {
    private final Map<Integer, List<ILoadingAwaiter>> callbacks = new ConcurrentHashMap<>();
    private final ScheduledExecutorService executor = PoolConfig.trackLoadWaiter;

    public void downloaded(Integer id) {
        List<ILoadingAwaiter> consumers = callbacks.remove(id);
        if (consumers != null && !consumers.isEmpty()) {
            consumers.forEach(c -> c.dispatchState(TrackCashState.ready));
        }
    }

    public void failedToLoad(Integer id) {
        List<ILoadingAwaiter> consumers = callbacks.remove(id);
        if (consumers != null && !consumers.isEmpty()) {
            consumers.forEach(c -> c.dispatchState(TrackCashState.error));
        }
    }

    //call inside synchronization on id
    public void registerCallback(Integer id, ILoadingAwaiter subscriber) {
        List<ILoadingAwaiter> subscribers = callbacks.computeIfAbsent(id, i -> new ArrayList<>());
        subscribers.add(subscriber);
        executor.schedule(() -> subscriber.dispatchState(TrackCashState.error), 32, TimeUnit.SECONDS);
    }

    public static ILoadingAwaiter awaiter(FileCashLock id, CompletableFuture<Boolean> r) {
        return new ILoadingAwaiter(id, r);
    }

    @Override
    public void destroy() throws Exception {
        executor.shutdown();
    }

    public static class ILoadingAwaiter {
        private volatile boolean set = false;
        private final FileCashLock lock;
        private CompletableFuture<Boolean> r;

        private ILoadingAwaiter(FileCashLock lock, CompletableFuture<Boolean> r) {
            this.lock = lock;
            this.r = r;
        }

        private void dispatchState(TrackCashState state) {
            synchronized (lock) {
                if (set) {
                    return;
                }
                set = true;
                if (state == TrackCashState.ready) {
                    r.complete(true);
                } else {
                    r.complete(false);
                }
            }
        }
    }
}
