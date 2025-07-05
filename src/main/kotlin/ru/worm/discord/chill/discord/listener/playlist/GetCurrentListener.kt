package ru.worm.discord.chill.discord.listener.playlist

import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import org.springframework.stereotype.Service
import ru.worm.discord.chill.discord.Commands
import ru.worm.discord.chill.discord.listener.ITextCommand
import ru.worm.discord.chill.discord.listener.MessageListener
import ru.worm.discord.chill.queue.TrackQueue

@Service
class GetCurrentListener(val playlist: TrackQueue) : MessageListener(),
    ITextCommand {

    init {
        command = Commands.GET_CURRENT
    }

    override fun onMessageReceived(event: MessageReceivedEvent) {
        if (!filter(event)) return
        val playing = playlist.playlist
        val response = if (playing.isNotEmpty()) {
            val track = playing.first()
            "`${track.title} (" +
                "${track.duration.toHoursPart()}h. " +
                "${track.duration.toMinutesPart()}m. " +
                "${track.duration.toSeconds()}s.)`"
        } else {
            "no track is currently playing"
        }
        answer(event, response)
    }
}