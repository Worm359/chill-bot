package ru.worm.discord.chill.discord.listener.playlist

import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import org.springframework.stereotype.Service
import ru.worm.discord.chill.discord.Commands
import ru.worm.discord.chill.discord.listener.ITextCommand
import ru.worm.discord.chill.discord.listener.MessageListener
import ru.worm.discord.chill.queue.TrackQueue
import java.util.*
import java.util.stream.Stream

@Service
class GetMergedPlaylistListener(val playlist: TrackQueue) : MessageListener(),
    ITextCommand {

    init {
        command = Commands.GET_MERGED_PLAYLIST
    }

    override fun onMessageReceived(event: MessageReceivedEvent) {
        if (!filter(event)) {
            return
        }
        val history = playlist.history
        val playing = playlist.playlist
        val playingIndex: Int = history.size
        Collections.reverse(history)
        val allTracks = Stream.concat(history.stream(), playing.stream()).toList()
        val hstMsg = buildString {
            append("```\nid\t\t\t\ttitle\n")
            allTracks.forEachIndexed { index, track ->
                if (index == playingIndex) {
                    append(">${track.id}=======${track.title}=======\n")
                } else {
                    append("*${track.id}\t\t${track.title}\n")
                }
            }
            append("```")
        }
        answer(event, hstMsg)
    }
}