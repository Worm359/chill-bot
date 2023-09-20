package ru.worm.discord.chill.lavaplayer;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import ru.worm.discord.chill.queue.TrackQueue;

@Component
public class LavaPlayerEventListener extends AudioEventAdapter {
    private final TrackQueue playlist;

    @Autowired
    public LavaPlayerEventListener(@Lazy TrackQueue playlist) {
        this.playlist = playlist;
    }

    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        if (AudioTrackEndReason.FINISHED.equals(endReason)) {
            playlist.next();
        }
    }
}
