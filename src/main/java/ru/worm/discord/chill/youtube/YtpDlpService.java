package ru.worm.discord.chill.youtube;

import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import ru.worm.discord.chill.queue.Track;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static ru.worm.discord.chill.logic.AudioInfoStorage.trackFile;

@Component
public class YtpDlpService {

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
                Process ytpDlp;
                System.out.println("inside mono create before process start...");
                ytpDlp = pb.start();
                System.out.println("inside mono create after process start...");
                CompletableFuture<Process> future = ytpDlp.onExit();
                future.completeOnTimeout(ytpDlp, 30, TimeUnit.SECONDS);
                future.whenComplete((process, throwable) -> {
                    if (process != null && process.exitValue() == 0) {
                        sink.success();
                    } else {
                        sink.error(new RuntimeException("couldn't load " + ytTrack.getUrl()));
                    }
                });
            } catch (IOException e) {
                sink.error(e);
            }
        });
    }
}
