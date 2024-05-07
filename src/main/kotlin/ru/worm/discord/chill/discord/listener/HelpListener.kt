package ru.worm.discord.chill.discord.listener

import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.InitializingBean
import org.springframework.stereotype.Service
import ru.worm.discord.chill.discord.Commands
import ru.worm.discord.chill.discord.listener.playlist.*
import ru.worm.discord.chill.logic.command.CliOption
import java.io.PrintWriter
import java.io.StringWriter

@Service
class HelpListener(val eventListeners: List<ITextCommand>) : MessageListener(), ITextCommand, InitializingBean {

    val groups = Groups()
    val AFTER_COMMAND_TAB = 12
    val NEXT_LINE_TAB_STOP = 15 //AFTER_COMMAND_TAB + ' - '.length :)
    val DESCRIPTION_WIDTH = 40
    val categorized = HashSet<String>()
    val log: Logger = LoggerFactory.getLogger(HelpListener::class.java)

    init {
        command = Commands.HELP
    }

    override fun afterPropertiesSet() {
        groups.apply {
            group("ADDING TRACKS") {
                add<AddListener>("add youtube video to the end of track queue")
                add<AddYoutubePlaylist>("add youtube playlist to the end of track queue")
                add<PlayNextListener>("play specified track next (by track ID or youtube URL). Use ID for referencing existing track in the queue/history.")
                add<PlayNowListener>("skip current, play specified track now (by track ID or youtube URL). Use ID for referencing existing track in the queue/history.")
            }

            group("QUEUE CONTROLS") {
                add<NextListener>("play next track from queue")
                add<PreviousListener>("play the previous track")
                add<RemoveListener>("deletes the track from queue")
                add<SkipToListener>("skip the queue to specific ID")
                add<GetPlaylistListener>("prints all tracks from track queue")
                add<GetHistoryListener>("prints all tracks from recent history")
                add<GetCurrentListener>("prints currently playing song")
                add<GetMergedPlaylistListener>("prints all tracks from history & track queue")
            }

            group("BOT CONTROL") {
                add<JoinListener>("invite bot to the voice channel you currently in")
                add<PlayerListener>("play/pause")
                add<CleanupDialogueListener>("remove recent bot related messages from text channel")
                add<StatusListener>("show bot status/version")
                add<PingListener>("ping the bot")
                add<BotLockListener>("lock bot to current discord server")
                add<DevListener>("to notify the bots that 'dev' environment is used. add ' disable' to return to normal")
            }

            eventListeners
                .filter {!categorized.contains(it.commandName())}
                .takeIf { it.isNotEmpty() }
                ?.let { uknowns ->
                    group("OTHER") {
                        uknowns.forEach { addUnknown(it) }
                    }
            }

        }
    }

    override fun onMessageReceived(event: MessageReceivedEvent) {
        if (!filter(event)) {
            return
        }
        val helpInfo = StringBuilder("```")
        groups.groups.forEach { group ->
            helpInfo.append("======= ${group.name} =======\n\n")
            group.commands.forEach {
                val command = it.first.commandName().padEnd(AFTER_COMMAND_TAB)
                val description = wrapWithTabulation(it.second ?: "no description")
                helpInfo.append("$command - $description\n")
            }
        }
        helpInfo.append("=====================\n")
        helpInfo.append("for more info type '!command -h'\n")
        helpInfo.append("```")
        answer(event, helpInfo.toString())
    }

    // Method to find a specific type of ITextCommand
    private inline fun <reified T : ITextCommand> findCommand(): T? {
        return eventListeners.find { it is T } as T?
    }

    fun wrapWithTabulation(text: String): String {
        val out = StringWriter()
        val pw = PrintWriter(out)
        CliOption.helpFormatter.printWrapped(pw, DESCRIPTION_WIDTH, NEXT_LINE_TAB_STOP, text)
        pw.flush()
        return out.toString()
    }

    class Groups {
        val groups = mutableListOf<Group>()
        fun group(name: String, init: Group.() -> Unit) {
            val g = Group(name).apply(init)
            groups.add(g)
        }
    }

    class Group(val name: String) {
        val commands = mutableListOf<Pair<ITextCommand, String?>>()
    }

    private inline fun <reified T : ITextCommand> Group.add(description: String? = null) {
        val listener = findCommand<T>()
        if (listener != null) {
            commands.add(Pair(listener, description))
            categorized.add(listener.commandName())
        } else {
            log.info("no such command as ${T::class.java.simpleName} is known.")
        }
    }

    private fun Group.addUnknown(listener: ITextCommand) {
        commands.add(Pair(listener, null))
    }
}