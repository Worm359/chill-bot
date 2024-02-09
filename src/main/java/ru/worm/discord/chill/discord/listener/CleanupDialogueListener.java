package ru.worm.discord.chill.discord.listener;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.springframework.stereotype.Service;
import ru.worm.discord.chill.discord.Commands;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Objects;

@Service
public class CleanupDialogueListener extends MessageListener implements EventListener {

    public CleanupDialogueListener() {
        this.command = Commands.CLEANUP_DIALOGUE;
    }


    @Override
    public void onMessageReceived(@Nonnull MessageReceivedEvent event) {
        if (!filter(event)) {
            return;
        }
        long id = event.getJDA().getSelfUser().getIdLong(); //the bot ID
        String botPrefix = this.botPrefix != null ? this.botPrefix : "!";
        MessageChannelUnion channel = event.getMessage().getChannel();
        if (!channel.getType().equals(ChannelType.TEXT)) {
            answer(event, "sorry, cannot determine text channel");
            return;
        }
        channel.getHistory().retrievePast(50).flatMap(messages -> {
            List<Message> botMessages = messages.stream()
                .filter(m -> Objects.equals(m.getAuthor().getIdLong(), id) || m.getContentRaw().startsWith(botPrefix))
                .toList();
            return channel.asTextChannel().deleteMessages(botMessages);
        }).queue();
    }

    @Override
    protected String helpMessage() {
        return "deletes all all recent messages from bot or addressed to bot.";
    }
}