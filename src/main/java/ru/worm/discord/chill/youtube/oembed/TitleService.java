package ru.worm.discord.chill.youtube.oembed;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.worm.discord.chill.queue.Track;
import ru.worm.discord.chill.util.Pair;

import java.util.*;
import java.util.concurrent.*;

@Service
public class TitleService implements DisposableBean {
    private static final String UNKNOWN_TITLE = "unknown - unknown";
    private final ExecutorService executor;
    private final OEmbedService oEmbedApi;
    private final Map<Integer, String> titles = new ConcurrentHashMap<>();

    @Autowired
    public TitleService(OEmbedService oEmbedApi) {
        this.oEmbedApi = oEmbedApi;
        this.executor = Executors.newSingleThreadExecutor();
    }

    public Mono<List<Pair<Track, String>>> getTitles(Collection<Track> tracks) {
        CompletableFuture<List<Pair<Track, String>>> future = CompletableFuture.supplyAsync(() -> {
            List<Pair<Track, String>> tracksWithNames = new ArrayList<>();
            for (Track track : tracks) {
                String cashed = titles.get(track.getId());
                if (cashed != null) {
                    tracksWithNames.add(new Pair<>(track, cashed));
                    continue;
                }
                String loaded = oEmbedApi.title(track.getUrl()).orElse(UNKNOWN_TITLE);
                titles.put(track.getId(), loaded);
                tracksWithNames.add(new Pair<>(track, loaded));
            }
            return tracksWithNames;
        }, executor);
        future.orTimeout(5L * tracks.size(), TimeUnit.SECONDS);
        return Mono.fromFuture(future)
                .onErrorResume(TimeoutException.class, throwable -> {
                    List<Pair<Track, String>> res = tracks.stream().map(t -> {
                        String title = titles.get(t.getId());
                        return new Pair<>(t, Objects.requireNonNullElse(title, UNKNOWN_TITLE));
                    }).toList();
                    return Mono.just(res);
                });
    }

    @Scheduled(fixedDelay = 5, timeUnit = TimeUnit.HOURS)
    void clean() {
        this.titles.clear();
    }

    @Override
    public void destroy() {
        this.executor.shutdown();
    }
}
