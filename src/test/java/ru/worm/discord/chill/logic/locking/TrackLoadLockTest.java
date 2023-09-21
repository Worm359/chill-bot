package ru.worm.discord.chill.logic.locking;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.worm.discord.chill.util.ExceptionUtils;

import java.time.Duration;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;

class TrackLoadLockTest {
    private static final Logger log = LoggerFactory.getLogger(TrackLoadLockTest.class);

    private static ExecutorService executorService;

    @Test
    public void testLockDeletedAfterAcquiring() throws InterruptedException, ExecutionException, TimeoutException {
        executorService = Executors.newFixedThreadPool(1);

        TrackLoadLocker locker = new TrackLoadLocker(Duration.ofMillis(1));
        log.debug("acquiring lock 1 for the first time");
        locker.getLock(1L);
        AtomicReference<Boolean> recognisedDeletion = new AtomicReference<>(false);

        Future<?> anotherThreadLocking = executorService.submit(() -> {
            log.debug("acquiring lock 1 in another thread");
            FileCashLock lock = locker.getLock(1L);
            try {
                //wait, so the lock gets deleted
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                log.error(ExceptionUtils.getStackTrace(e));
            }
            log.debug("ready to synchronize on the lock");
            //sync on a lock that's been deleted from a map
            synchronized (lock) {
                try {
                    lock.checkDeleted();
                } catch (AudioCashLockException e) {
                    log.debug("lock was deleted!");
                    recognisedDeletion.set(true);
                }
            }
        });
        Thread.sleep(300);
        locker.deleteOldLocks();

        anotherThreadLocking.get(10, TimeUnit.SECONDS);
        Assertions.assertTrue(recognisedDeletion.get(), "deletion was not recognized by another thread");
    }


    @Test
    public void deleteIntervalRefreshed() throws InterruptedException, ExecutionException, TimeoutException {
        executorService = Executors.newFixedThreadPool(1);

        TrackLoadLocker locker = new TrackLoadLocker(Duration.ofMillis(1500));
        log.debug("acquiring lock 1 for the first time");
        locker.getLock(1L);
        AtomicReference<Boolean> recognisedDeletion = new AtomicReference<>(false);
        Thread.sleep(1000);

        Future<?> anotherThreadLockingWithoutDeletion = executorService.submit(() -> {
            log.debug("acquiring lock 1 in another thread");
            FileCashLock lock = locker.getLock(1L);
            log.debug("ready to synchronize on the lock");
            synchronized (lock) {
                try {
                    lock.checkDeleted(); //all is good
                    log.debug("lock has not been deleted");
                } catch (AudioCashLockException e) {
                    log.debug("lock was deleted!");
                    recognisedDeletion.set(true);
                }
            }
        });
        locker.deleteOldLocks();
        anotherThreadLockingWithoutDeletion.get(10, TimeUnit.SECONDS);
        Assertions.assertFalse(recognisedDeletion.get(), "deletion was launched :(");
    }

    @AfterAll
    public static void destroy() {
        try {
            executorService.shutdown();
        } catch (Exception e) {
            log.error(ExceptionUtils.getStackTrace(e));
        }
    }
  
}