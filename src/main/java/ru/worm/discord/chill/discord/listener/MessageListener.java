package ru.worm.discord.chill.discord.listener;

import discord4j.core.object.entity.Message;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import reactor.core.publisher.Mono;
import ru.worm.discord.chill.discord.GuildObserver;
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
                .flatMap(message -> {
                    String messageContent = message.getContent();
                    if (TextUtil.isEmpty(messageContent)) {
                        return Mono.empty();
                    }
                    String[] commandWords = messageContent.split(" ");
                    if (!commandWords[0].equalsIgnoreCase(commandName())) {
                        return Mono.empty();
                    } else if (Arrays.stream(commandWords).anyMatch(s -> s.equalsIgnoreCase("-h") || s.equalsIgnoreCase("--help"))) {
                        return eventMessage.getChannel()
                                .flatMap(channel -> channel.createMessage(helpMessage()))
                                .flatMap(response -> Mono.empty());
                    } else {
                        if (GuildObserver.isLockedToAnotherGuildId(message, command)) {
                            return handleWrongGuildId(message).then(Mono.empty());
                        }
                        if (GuildObserver.modeMismatch(message)) {
                            return Mono.empty();
                        }
                        return Mono.just(message);
                    }
                });
    }

    protected Mono<Pair<Message, CommandLine>> filterWithOptions(Message eventMessage) {
        return filter(eventMessage)
                .flatMap(message -> {
                    Options opts = options().getFirst();
                    IOptionValidator val = options().getSecond();
                    if (opts == null) {
                        return Mono.error(new IllegalStateException("options filtering cannot be applied without implementing #options()"));
                    }
                    String messageContent = message.getContent();
                    String[] commandWords = messageContent.split(" ");
                    if (Arrays.stream(commandWords).anyMatch(s -> s.equalsIgnoreCase("-h") || s.equalsIgnoreCase("--help"))) {
                        return eventMessage.getChannel()
                                .flatMap(channel -> channel.createMessage(CliOption.help(commandName(), opts)))
                                .flatMap(response -> Mono.empty());
                    }
                    try {
                        CommandLine parse = parser.parse(opts,
                                Arrays.copyOfRange(commandWords, 1, commandWords.length));
                        if (val != null) val.validate(parse);
                        if (GuildObserver.isLockedToAnotherGuildId(message, command)) {
                            return handleWrongGuildId(message).then(Mono.empty());
                        }
                        if (GuildObserver.modeMismatch(message)) {
                            return Mono.empty();
                        }
                        return Mono.just(new Pair<>(message, parse));
                    } catch (ParseException e) {
                        return Mono.error(e);
                    }
                })
                .onErrorResume(throwable -> {
                    if (throwable instanceof ParseException) {
                        String err = "%s\n%s".formatted(throwable.getMessage(), CliOption.help(commandName(), options().getFirst()));
                        return eventMessage.getChannel()
                                .flatMap(channel -> channel.createMessage(err))
                                .flatMap(response -> Mono.empty());
                    } else {
                        return Mono.error(throwable); // Handle other errors as needed
                    }
                });
    }

    private Mono<Void> handleWrongGuildId(Message message) {
        return message.getChannel()
                .flatMap(channel -> channel.createMessage("sorry, bot is locked to another guildId"))
                .then(Mono.empty());
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

    private String helpMessage() {
        Options opts = options().getFirst();
        String msg;
        if (opts != null) {
            msg = CliOption.help(commandName(), opts);
        } else {
            msg = "'" + commandName() + "' does not have any help info. you're on your own...";
        }
        return msg;
    }
}