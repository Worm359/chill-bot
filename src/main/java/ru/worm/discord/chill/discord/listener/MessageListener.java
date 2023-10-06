package ru.worm.discord.chill.discord.listener;

import discord4j.core.object.entity.Message;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import reactor.core.publisher.Mono;
import ru.worm.discord.chill.discord.IWithPrefix;
import ru.worm.discord.chill.logic.command.CliOption;
import ru.worm.discord.chill.logic.command.IOptionValidator;
import ru.worm.discord.chill.util.Pair;
import ru.worm.discord.chill.util.TextUtil;

import java.util.Arrays;

public abstract class MessageListener implements IWithPrefix {
    private final static DefaultParser parser = new DefaultParser();
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

    protected Mono<Pair<Message, CommandLine>> filterWithOptions(Message eventMessage) {
        return filter(eventMessage)
                .flatMap(message -> {
                    Options opts = options().getFirst();
                    IOptionValidator val = options().getSecond();
                    if (opts == null) {
                        return Mono.error(new IllegalStateException("options filtering cannot be applied without imlementing #options()"));
                    }
                    String messageContent = message.getContent();
                    String[] commandWords = messageContent.split(" ");
                    try {
                        CommandLine parse = parser.parse(opts,
                                Arrays.copyOfRange(commandWords, 1, commandWords.length));
                        if (val != null) val.validate(parse);
                        return Mono.just(new Pair<>(message, parse));
                    } catch (ParseException e) {
                        return Mono.error(e);
                    }
                })
                .onErrorResume(throwable -> {
                    if (throwable instanceof ParseException) {
                        String err = "%s\n%s".formatted(throwable.getMessage(), CliOption.help(options().getFirst()));
                        return eventMessage.getChannel()
                                .flatMap(channel -> channel.createMessage(err))
                                .flatMap(response -> Mono.empty());
                    } else {
                        return Mono.error(throwable); // Handle other errors as needed
                    }
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

    protected Pair<Options, IOptionValidator> options() {
        return new Pair<>(null, null);
    }
}