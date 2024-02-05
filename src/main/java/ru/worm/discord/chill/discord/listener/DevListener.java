package ru.worm.discord.chill.discord.listener;

import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.worm.discord.chill.discord.Commands;
import ru.worm.discord.chill.discord.GuildObserver;
import ru.worm.discord.chill.util.TextUtil;

@Service
public class DevListener extends MessageListener implements EventListener<MessageCreateEvent> {

    public DevListener() {
        this.command = Commands.DEV;
    }

    @Override
    public Class<MessageCreateEvent> getEventType() {
        return MessageCreateEvent.class;
    }

    public Mono<Void> execute(MessageCreateEvent event) {
        return Mono.just(event.getMessage())
                .filter(message -> message.getAuthor().map(user -> !user.isBot()).orElse(false))
                .flatMap(message -> {
                    String messageContent = message.getContent();
                    if (TextUtil.isEmpty(messageContent)) {
                        return Mono.empty();
                    }
                    String[] commandWords = messageContent.split(" ");
                    if (!commandWords[0].equalsIgnoreCase(commandName())) {
                        return Mono.empty();
                    } else {
                        return Mono.just(message);
                    }
                })
                .flatMap(Message::getChannel)
                .flatMap(channel -> {
                    String[] args = event.getMessage().getContent().split(" ");
                    event.getGuildId()
                            .map(Snowflake::asLong)
                            .ifPresentOrElse(guildId -> {
                                if (args.length == 1) {
                                    GuildObserver.setDevMode(guildId);
                                } else if (args[1].equalsIgnoreCase("disable")) {
                                    GuildObserver.discardDevMode(guildId);
                                }
                            }, () -> log.warn("couldn't retrieve guildId"));
                    return Mono.empty();
                })
                .then();
    }
}