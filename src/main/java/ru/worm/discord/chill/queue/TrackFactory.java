package ru.worm.discord.chill.queue;

import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import ru.worm.discord.chill.config.settings.RootSettings;
import ru.worm.discord.chill.util.ExceptionUtils;
import ru.worm.discord.chill.util.TextUtil;
import ru.worm.discord.chill.util.YoutubeUtil;
import ru.worm.discord.chill.youtube.oembed.OEmbedService;

import java.io.IOException;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class TrackFactory {
    private static final String UNKNOWN = "unknown - unknown";
    private final Logger log = LoggerFactory.getLogger(getClass());
    private final YouTube youtube;
    private final String apiKey;
    private final TrackQueue trackQueue;
    private final OEmbedService oEmbed;

    public TrackFactory(YouTube youtube, RootSettings settings, TrackQueue trackQueue, OEmbedService oEmbed) {
        this.youtube = youtube;
        this.apiKey = settings.getYoutube().getApiKey();
        this.trackQueue = trackQueue;
        this.oEmbed = oEmbed;
    }

    public Mono<List<Track>> newTracks(String playlistId) {
        if (TextUtil.isEmpty(playlistId)) return Mono.empty();
        return Mono.fromCallable(() -> {
            try {
                List<PlaylistItem> playlistItems = playlist(playlistId);
                if (playlistItems == null || playlistItems.isEmpty()) {
                    log.error("failed to load playlist {}", playlistId);
                    return Collections.emptyList();
                }

                List<String> allVIds = playlistItems
                        .stream()
                        .flatMap(item -> {
                            PlaylistItemContentDetails itemDetails = item.getContentDetails();
                            if (itemDetails == null) return Stream.empty();
                            if (TextUtil.isEmpty(itemDetails.getVideoId())) return Stream.empty();
                            return Stream.of(itemDetails.getVideoId());
                        })
                        .toList();

                String allIds = allVIds
                        .stream()
                        .distinct()
                        .collect(Collectors.joining(","));

                List<Video> items = video("contentDetails,snippet", allIds);
                //videoId/title already present
                //looking for a duration
                List<Track> tracks = Collections.emptyList();
                if (items != null && !items.isEmpty()) {
                    Map<String, Track> knownTracks = trackQueue.getAllKnownTracks()
                            .stream()
                            .collect(Collectors.toMap(Track::getVideoId, t -> t, (t1, t2) -> t1));
                    tracks = items.stream()
                            .flatMap(v -> {
                                String duration = null;
                                String title = null;
                                String videoId = v.getId();
                                if (TextUtil.isEmpty(videoId)) {
                                    return Stream.empty();
                                }
                                Track oldTrack = knownTracks.get(videoId);
                                //fixme -> too old track will be in 'deleted' status???
                                if (oldTrack != null) {
                                    return Stream.of(oldTrack);
                                }
                                if (v.getContentDetails() != null) {
                                    duration = v.getContentDetails().getDuration();
                                }
                                if (v.getSnippet() != null) {
                                    title = v.getSnippet().getTitle();
                                }
                                //if title is empty, try load through oEmbed
                                if (TextUtil.isEmpty(title)) {
                                    String videoUrl = YoutubeUtil.urlForVideoId(videoId);
                                    title = oEmbed.title(videoUrl).orElse(UNKNOWN);
                                }
                                return Stream.of(new Track(videoId, title, parse(duration)));
                            })
                            .collect(Collectors.toCollection(ArrayList::new));
                }
                return tracks;
            } catch (IOException e) {
                log.error(ExceptionUtils.getStackTrace(e));
                return Collections.emptyList();
            }
        });
    }

    public Mono<Track> newTrack(String youtubeUrl) {
        String videoId = YoutubeUtil.stripVideoUrl(youtubeUrl).orElse(null);
        if (videoId == null) return Mono.empty();
        Optional<Track> trackAlreadyPresent = trackQueue.findTrackByVideoId(youtubeUrl);
        return trackAlreadyPresent
                .map(Mono::just)
                .orElseGet(() -> Mono.fromCallable(() -> {
                    try {
                        List<Video> items = video("contentDetails,snippet", videoId);
                        if (items == null || items.isEmpty()) {
                            log.error("failed to load track {} info", videoId);
                            return null;
                        }
                        if (items.size() > 1) {
                            log.error("cannot load track {} info - more than one video found", videoId);
                            return null;
                        }

                        Video video = items.get(0);
                        String title;
                        Duration duration = null;
                        if (video.getContentDetails() != null && !TextUtil.isEmpty(video.getContentDetails().getDuration())) {
                            duration = parse(video.getContentDetails().getDuration());
                        }

                        if (video.getSnippet() != null && !TextUtil.isEmpty(video.getSnippet().getTitle())) {
                            title = video.getSnippet().getTitle();
                        } else {
                            title = oEmbed.title(youtubeUrl).orElse(UNKNOWN);
                        }

                        if (duration == null) {
                            log.warn("track {} failed to load duration.", youtubeUrl);
                        }

                        if (title == null) {
                            log.warn("track {} failed to load title.", youtubeUrl);
                        }

                        return new Track(videoId, title, duration);
                    } catch (IOException e) {
                        log.error(ExceptionUtils.getStackTrace(e));
                        return null;
                    }
                }));
                //.subscribeOn(Schedulers.boundedElastic())); //blocking http operation -> need for elastic?
    }

    private List<Video> video(String part, String id) throws IOException {
        YouTube.Videos.List requestVideos;
        requestVideos = youtube.videos().list(part);
        VideoListResponse responseVideos = requestVideos.setKey(apiKey)
                .setId(id)
                .setMaxResults(100L)
                .execute();
        if (responseVideos == null) return Collections.emptyList();
        return responseVideos.getItems();
    }

    private List<PlaylistItem> playlist(String playlistId) throws IOException {
        YouTube.PlaylistItems.List request = youtube.playlistItems()
                .list("contentDetails,snippet");
        PlaylistItemListResponse response = request.setKey(apiKey)
                .setMaxResults(100L)
                .setPlaylistId(playlistId)
                .execute();
        if (response == null) return Collections.emptyList();
        return response.getItems();
    }

    private static Duration parse(String duration) {
        try {
            return Duration.parse(duration);
        } catch (Exception e) {
            return null;
        }
    }
}


