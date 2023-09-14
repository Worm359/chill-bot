package ru.worm.discord.chill.discord.listener;

import discord4j.core.object.entity.Message;
import reactor.core.publisher.Mono;
import ru.worm.discord.chill.discord.IWithPrefix;
import ru.worm.discord.chill.util.TextUtil;

public abstract class MessageListener implements IWithPrefix {
    protected String botPrefix;
    protected String command;

    public Mono<Message> filter(Message eventMessage) {
        return Mono.just(eventMessage)
          .filter(message -> message.getAuthor().map(user -> !user.isBot()).orElse(false))
          .filter(message -> {
              String messageContent = message.getContent();
              if (TextUtil.isEmpty(messageContent)) {
                  return false;
              }
              String[] commandWords = messageContent.split(" ");
              return commandWords[0].equalsIgnoreCase(commandName());
          });
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