package ru.worm.discord.chill.youtube;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import ru.worm.discord.chill.logic.locking.AudioCashLockException;
import ru.worm.discord.chill.logic.locking.FileCashLock;
import ru.worm.discord.chill.logic.locking.TrackCashState;
import ru.worm.discord.chill.logic.locking.TrackLoadLocker;
import ru.worm.discord.chill.queue.Track;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static ru.worm.discord.chill.logic.AudioInfoStorage.trackFile;

@Component
public class YtpDlpService {
    private final Logger log = LoggerFactory.getLogger(getClass());

    private final TrackLoadLocker locker;

    @Autowired
    public YtpDlpService(TrackLoadLocker locker) {
        this.locker = locker;
    }

    public static void main(String[] args) throws InterruptedException {
//        YtpDlpService service = new YtpDlpService();
//        System.out.println("calling mono");
//        service.loadAudio("")
//                .doOnSuccess(b -> System.out.println("on success - all is ok"))
//                .doOnError(t -> System.out.println("ERROR: something gone wrong"))
//                .subscribe();
//        System.out.println("called Mono. Should end soon.");
//        Thread.sleep(40000);
//        System.out.println("END.");
    }

    public Mono<Void> loadAudio(Track ytTrack) {
        return Mono.create(sink -> {
            ProcessBuilder pb = new ProcessBuilder("yt-dlp.exe",
                    "-x",
                    "-o", trackFile(ytTrack),
                    "--no-playlist",
                    ytTrack.getUrl());
            pb.inheritIO();
            try {
                FileCashLock trackLock = locker.getLock(ytTrack.getId());
                synchronized (trackLock) {
                    trackLock.isIdle();
                    trackLock.loading();
                }
                Process ytpDlp;
                System.out.println("inside mono create before process start...");
                ytpDlp = pb.start();
                System.out.println("inside mono create after process start...");
                CompletableFuture<Process> future = ytpDlp.onExit();
                future.completeOnTimeout(ytpDlp, 30, TimeUnit.SECONDS);
                future.whenComplete((process, throwable) -> {
                    if (process != null && process.exitValue() == 0) {
                        //lock#timeRequested was just updated. no need to check deleted state
                        trackLock.ready();
                        sink.success();
                    } else {
                        //lock#timeRequested was just updated. no need to check deleted state
                        trackLock.error();
                        sink.error(new RuntimeException("couldn't load " + ytTrack.getUrl()));
                    }
                });
            } catch (IOException e) {
                sink.error(e);
            } catch (AudioCashLockException e) {
                log.error("was trying to load track.id={} '{}' but the lock obtained by other thread",
                        ytTrack.getId(), ytTrack.getUrl());
                if (TrackCashState.ready.equals(e.getLastState())) sink.success();
                else sink.error(e);
            }
        });
    }
}
