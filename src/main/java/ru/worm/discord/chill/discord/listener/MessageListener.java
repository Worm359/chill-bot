package ru.worm.discord.chill.discord.listener;

import discord4j.core.object.entity.Message;
import reactor.core.publisher.Mono;
import ru.worm.discord.chill.discord.IWithPrefix;

public abstract class MessageListener implements IWithPrefix {
    protected String botPrefix;
    protected String command;

//    public Mono<Void> processCommand(Message eventMessage) {
//        return Mono.just(eventMessage)
//          .filter(message -> message.getAuthor().map(user -> !user.isBot()).orElse(false))
//          .filter(message -> message.getContent().contains(command))
//          .flatMap(Message::getChannel)
//          .flatMap(channel -> channel.createMessage("suck a dick dumbshits"))
//          .then();
//    }
    public Mono<Message> filter(Message eventMessage) {
        return Mono.just(eventMessage)
          .filter(message -> message.getAuthor().map(user -> !user.isBot()).orElse(false))
          .filter(message -> message.getContent().contains(commandName()));
    }

    @Override
    public void setBotPrefix(String botPrefix) {
        this.botPrefix = botPrefix;
    }

    public String commandName() {
        String botPrefix = this.botPrefix != null ? this.botPrefix : "!";
        return botPrefix + this.command;
    }
}