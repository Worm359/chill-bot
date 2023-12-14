package ru.worm.discord.chill.youtube.oembed;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import ru.worm.discord.chill.util.ExceptionUtils;
import ru.worm.discord.chill.util.TextUtil;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

/**
 * достает название ролика по url из youtube
 */
@Component
public class OEmbedService {
    private final Logger log = LoggerFactory.getLogger(getClass());
    private final RestTemplate restTemplate;
    @Autowired
    public OEmbedService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    Optional<String> title(String url) {
        try {
            String oembedEndpoint = "https://www.youtube.com/oembed?url=%s&format=json"
                    .formatted(URLEncoder.encode(url, StandardCharsets.UTF_8));
            URI uri = new URI(oembedEndpoint);
            ResponseEntity<YtbTitle> forEntity = restTemplate.getForEntity(uri, YtbTitle.class);
            if (forEntity.getStatusCodeValue() != 200) {
                log.error("coudln't get response: status {} for {}", forEntity.getStatusCodeValue(), url);
                return Optional.empty();
            }
            if (forEntity.getBody() == null || TextUtil.isEmpty(forEntity.getBody().getTitle())) {
                log.error("coudln't get response: status {} for {}, but body is empty", forEntity.getStatusCodeValue(), url);
                return Optional.empty();
            }
            return Optional.ofNullable(forEntity.getBody().getTitle());
        } catch (URISyntaxException | RestClientException e) {
            log.error("title loading for URL {} {}", url, ExceptionUtils.getStackTrace(e));
            return Optional.empty();
        }
    }
}
