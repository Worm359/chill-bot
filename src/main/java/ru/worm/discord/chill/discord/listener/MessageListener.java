package ru.worm.discord.chill.discord.listener;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.GenericMessageEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.springframework.beans.factory.DisposableBean;
import ru.worm.discord.chill.discord.GuildObserver;
import ru.worm.discord.chill.discord.IWithPrefix;
import ru.worm.discord.chill.logic.PoolConfig;
import ru.worm.discord.chill.logic.command.CliOption;
import ru.worm.discord.chill.logic.command.IOptionValidator;
import ru.worm.discord.chill.util.Pair;
import ru.worm.discord.chill.util.TextUtil;

import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public abstract class MessageListener extends ListenerAdapter implements IWithPrefix, DisposableBean {
    private final static DefaultParser parser = new DefaultParser();
    protected String botPrefix;
    protected String command;


    protected boolean filter(MessageReceivedEvent event) {
        User author = event.getAuthor();
        if (author.isBot()) {
            return false;
        }
        net.dv8tion.jda.api.entities.Message message = event.getMessage();
        String messageContent;
        if (TextUtil.isEmpty(messageContent = message.getContentRaw())) {
            return false;
        }
        String[] commandWords = messageContent.split(" ");
        if (!commandWords[0].equalsIgnoreCase(commandName())) {
            return false;
        } else if (GuildObserver.modeMismatch(message.getGuildIdLong(), command)) {
            return false;
        } else if (Arrays.stream(commandWords).anyMatch(s -> s.equalsIgnoreCase("-h") || s.equalsIgnoreCase("--help"))) {
            answer(event, helpMessage());
            return false;
        } else {
            if (GuildObserver.isLockedToAnotherGuildId(message.getGuildIdLong(), command)) {
                answer(event, "sorry, bot is locked to another guildId");
                return false;
            }
            return true;
        }
    }

    protected Optional<CommandLine> filterWithOptions(MessageReceivedEvent event) {
        if (!filter(event)) {
            return Optional.empty();
        }
        User author = event.getAuthor();
        if (author.isBot()) {
            return Optional.empty();
        }
        Options opts = options().getFirst();
        IOptionValidator val = options().getSecond();
        if (opts == null) {
            throw new IllegalStateException("options filtering cannot be applied without implementing #options()");
        }
        net.dv8tion.jda.api.entities.Message message = event.getMessage();
        String messageContent;
        if (TextUtil.isEmpty(messageContent = message.getContentRaw())) {
            return Optional.empty();
        }
        String[] commandWords = messageContent.split(" ");
        if (Arrays.stream(commandWords).anyMatch(s -> s.equalsIgnoreCase("-h") || s.equalsIgnoreCase("--help"))) {
            answer(event, helpMessage());
//            event.getChannel().sendMessage(helpMessage()).queue();
            return Optional.empty();
        }
        try {
            CommandLine parse = parser.parse(opts,
                    Arrays.copyOfRange(commandWords, 1, commandWords.length));
            if (val != null) val.validate(parse);
            if (GuildObserver.isLockedToAnotherGuildId(message.getGuildIdLong(), command)) {
                answer(event, "sorry, bot is locked to another guildId");
                return Optional.empty();
            }
            if (GuildObserver.modeMismatch(message.getGuildIdLong(), command)) {
                return Optional.empty();
            }
            return Optional.of(parse);
        } catch (ParseException e) {
            event.getChannel().sendMessage(e.getLocalizedMessage()).queue();
            return Optional.empty();
        }
    }

    protected void answer(GenericMessageEvent event, String text) {
        String[] msgParts = TextUtil.splitMessage(text);
        Arrays.stream(msgParts)
            .forEach(msgPart -> event.getChannel()
                .sendMessage(msgPart)
                .queue());
    }

    protected <T> CompletableFuture<T> async(Supplier<T> supplier) {
        return CompletableFuture.supplyAsync(supplier, PoolConfig.forEvents);
    }

    @Override
    public void destroy() {
        PoolConfig.forEvents.shutdown();
    }

    @Override
    public void setBotPrefix(String botPrefix) {
        this.botPrefix = botPrefix;
    }

    public String commandName() {
        String botPrefix = this.botPrefix != null ? this.botPrefix : "!";
        return botPrefix + this.command;
    }

    public Pair<Options, IOptionValidator> options() {
        return new Pair<>(null, null);
    }

    protected String helpMessage() {
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