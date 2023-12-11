package ru.worm.discord.chill.youtube.api;

import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.PlaylistItemContentDetails;
import com.google.api.services.youtube.model.PlaylistItemListResponse;
import com.google.api.services.youtube.model.Video;
import com.google.api.services.youtube.model.VideoListResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;
import ru.worm.discord.chill.config.settings.RootSettings;
import ru.worm.discord.chill.util.ExceptionUtils;
import ru.worm.discord.chill.util.TextUtil;
import ru.worm.discord.chill.util.YoutubeUtil;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
@ConditionalOnBean(YouTube.class)
public class VideoMetadataService {
    private final Logger log = LoggerFactory.getLogger(getClass());
    private final YouTube youtube;
    private final String apiKey;

    @Autowired
    public VideoMetadataService(YouTube youtube, RootSettings settings) {
        this.youtube = youtube;
        this.apiKey = settings.getYoutube().getApiKey();
    }

    public List<String> getVideoUrls(String playlistId) {
        try {
            YouTube.PlaylistItems.List request = youtube.playlistItems()
                .list("contentDetails");
            PlaylistItemListResponse response = request.setKey(apiKey)
                    .setMaxResults(25L)
                    .setPlaylistId(playlistId)
                    .execute();
            if (response == null || response.getItems() == null || response.getItems().isEmpty()) {
                log.error("failed to load playlist {}", playlistId);
                return Collections.emptyList();
            }
            return response.getItems()
                    .stream()
                    .flatMap(item -> {
                        PlaylistItemContentDetails itemDetails = item.getContentDetails();
                        if (itemDetails == null) return Stream.empty();
                        if (TextUtil.isEmpty(itemDetails.getVideoId())) return Stream.empty();
                        return Stream.of("https://youtube.com/watch?v=%s".formatted(itemDetails.getVideoId()));
                    }).collect(Collectors.toCollection(ArrayList::new));
        } catch (IOException e) {
            log.error(ExceptionUtils.getStackTrace(e));
            return Collections.emptyList();
        }
    }

    public static void main(String[] args) {
        String videoId = YoutubeUtil.stripVideoUrl("https://youtu.be/-r679Hhs9Zs").orElse(null);
        System.out.println(videoId);
    }

    public Optional<Long> videoMinutesLength(String videoId) {
        YouTube.Videos.List request = null;
        try {
            request = youtube.videos().list("contentDetails"); //snippet
            VideoListResponse response = request.setKey(apiKey)
                    .setId(videoId)
                    .execute();

            if (response == null || response.getItems() == null || response.getItems().isEmpty()) {
                log.error("failed to load track {} info", videoId);
                return Optional.empty();
            }

            if (response.getItems().size() > 1) {
                log.error("cannot load track {} info - more than one video found", videoId);
                return Optional.empty();
            }

            Video video = response.getItems().get(0);

            if (video.getContentDetails() == null || TextUtil.isEmpty(video.getContentDetails().getDuration())) {
                log.error("track {} coudln't load content details - failed to extract duration.", videoId);
                return Optional.empty();
            }

            Duration duration = Duration.parse(video.getContentDetails().getDuration());
            return Optional.of(duration.toMinutes());
        } catch (IOException e) {
            log.error(ExceptionUtils.getStackTrace(e));
            return Optional.empty();
        }
    }
}
