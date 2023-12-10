package ru.worm.discord.chill.youtube.api;

import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.Video;
import com.google.api.services.youtube.model.VideoListResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;
import ru.worm.discord.chill.config.settings.RootSettings;
import ru.worm.discord.chill.config.settings.YoutubeSetting;
import ru.worm.discord.chill.util.ExceptionUtils;
import ru.worm.discord.chill.util.TextUtil;

import java.io.IOException;
import java.time.Duration;
import java.util.Optional;

@Component
@ConditionalOnBean(YouTube.class)
public class VideoMetadataService {
    private final Logger log = LoggerFactory.getLogger(getClass());
    private final YouTube youtube;
    private final YoutubeSetting settings;

    @Autowired
    public VideoMetadataService(YouTube youtube, RootSettings settings) {
        this.youtube = youtube;
        this.settings = settings.getYoutube();
    }

    public Optional<Long> videoMinutesLength(String videoId) {
        YouTube.Videos.List request = null;
        try {
            request = youtube.videos().list("contentDetails"); //snippet
            VideoListResponse response = request.setKey(settings.getApiKey())
                    .setId(videoId)
                    .execute();

            if (response == null || response.getItems() == null) {
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
