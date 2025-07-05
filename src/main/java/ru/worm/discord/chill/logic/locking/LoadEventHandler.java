package ru.worm.discord.chill.logic.locking;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.stereotype.Service;
import ru.worm.discord.chill.logic.PoolConfig;
import ru.worm.discord.chill.youtube.LoadResult;

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
            consumers.forEach(c -> c.dispatchState(TrackCashState.ready, null));
        }
    }

    public void failedToLoad(Integer id, String msg) {
        List<ILoadingAwaiter> consumers = callbacks.remove(id);
        if (consumers != null && !consumers.isEmpty()) {
            consumers.forEach(c -> c.dispatchState(TrackCashState.error, msg));
        }
    }

    //call inside synchronization on id
    public void registerCallback(Integer id, ILoadingAwaiter subscriber) {
        List<ILoadingAwaiter> subscribers = callbacks.computeIfAbsent(id, i -> new ArrayList<>());
        subscribers.add(subscriber);
        executor.schedule(() -> subscriber.dispatchState(TrackCashState.error, "timeout!"), 32, TimeUnit.SECONDS);
    }

    public static ILoadingAwaiter awaiter(FileCashLock id, CompletableFuture<LoadResult> r) {
        return new ILoadingAwaiter(id, r);
    }

    @Override
    public void destroy() {
        executor.shutdown();
    }

    public static class ILoadingAwaiter {
        private volatile boolean set = false;
        private final FileCashLock lock;
        private CompletableFuture<LoadResult> r;

        private ILoadingAwaiter(FileCashLock lock, CompletableFuture<LoadResult> r) {
            this.lock = lock;
            this.r = r;
        }

        private void dispatchState(TrackCashState state, String msg) {
            synchronized (lock) {
                if (set) {
                    return;
                }
                set = true;
                if (state == TrackCashState.ready) {
                    r.complete(LoadResult.success());
                } else {
                    r.complete(LoadResult.err("was waiting for previously initiated loading and got: '" + msg + "'"));
                }
            }
        }
    }
}
