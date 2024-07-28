package ru.worm.discord.chill.discord

import net.dv8tion.jda.api.JDA
import org.slf4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import ru.worm.discord.chill.util.logger

@Service
class NotificationService @Autowired constructor(val jda: JDA) {
    val log: Logger = logger<NotificationService>()

    companion object {
        @Volatile
        var channelId: Long? = null
    }

    fun msg(text: String) {
        log.debug(text)
        val localChannelId = channelId
        if (localChannelId == null) {
            log.debug("cannot send notification; no channelId is known")
            return
        }
        val channel = jda.getTextChannelById(localChannelId)
        if (channel == null) {
            log.warn("couldn't obtain channel by channelId=$localChannelId")
            return
        }
        channel.sendMessage(text).queue()
    }
}