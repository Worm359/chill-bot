package ru.worm.discord.chill.logic;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.worm.discord.chill.queue.Track;
import ru.worm.discord.chill.queue.event.ITrackQSubscriber;
import ru.worm.discord.chill.queue.event.TrackEvent;
import ru.worm.discord.chill.youtube.oembed.TitleService;

import java.util.Collections;

@Service
public class TitleListener implements ITrackQSubscriber {
    private final Logger log = LoggerFactory.getLogger(getClass());
    private final TitleService titleService;

    @Autowired
    public TitleListener(TitleService titleService) {
        this.titleService = titleService;
    }

    @Override
    public void newTrackAdded(TrackEvent<Track> event) {
        Track track = event.getPayload();
        log.debug("loading title for new track id={} url={}", track.getId(), track.getUrl());
        titleService
                .getTitles(Collections.singletonList(track))
                .subscribe();
    }
}
