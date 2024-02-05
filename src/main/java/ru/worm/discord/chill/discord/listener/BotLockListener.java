package ru.worm.discord.chill.discord.listener;

import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.worm.discord.chill.discord.Commands;
import ru.worm.discord.chill.discord.GuildObserver;
import ru.worm.discord.chill.util.TextUtil;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

@Service
public class BotLockListener extends MessageListener implements EventListener<MessageCreateEvent> {
    private volatile String lastDurationChecked;

    public BotLockListener() {
        this.command = Commands.LOCK;
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
                    if (!commandWords[0].equalsIgnoreCase(commandName()) && !commandWords[0].contains("unlock")) {
                        return Mono.empty();
                    } else {
                        return Mono.just(message);
                    }
                })
                .flatMap(Message::getChannel)
                .flatMap(channel -> {
                    String[] args = event.getMessage().getContent().split(" ");
                    boolean toLock = args[0].equalsIgnoreCase(commandName());
                    if (args.length < 2) {
                        return channel.createMessage("sorry, wrong password");
                    }
                    String password = args[1];
                    if (Objects.equals(lastDurationChecked, password)) {
                        if (toLock) {
                            Optional<Snowflake> guildId = event.getGuildId();
                            if (guildId.isEmpty()) {
                                return channel.createMessage("sorry, no guildId");
                            } else {
                                GuildObserver.guildIdLock = guildId.get().asLong();
                                return channel.createMessage("locked.");
                            }
                        } else {
                            GuildObserver.guildIdLock = null;
                            return channel.createMessage("unlocked.");
                        }
                    } else {
                        return channel.createMessage("sorry, wrong password");
                    }
                })
                .then();
    }

    public void setLastDurationChecked(Duration uptime) {
        this.lastDurationChecked = String.valueOf(getChar((int) uptime.toDays())) +
                getChar((int) uptime.toHours()) +
                getChar(uptime.toMinutesPart()) +
                getChar(uptime.toSecondsPart());
    }

    private static char getChar(int i) {
        return i < 0 || i > 25 ? 'z' : (char) ('a' + i);
    }

    public static void main(String[] args) throws InterruptedException {
        Instant now = Instant.now();
        Thread.sleep(2000);
        Duration uptime = Duration.between(now, Instant.now());
        System.out.println("%d days (%02d hr %02d min %02d sec)".formatted(
                uptime.toDays(),
                uptime.toHours(),
                uptime.toMinutesPart(),
                uptime.toSecondsPart()
        ));
        System.out.println(String.valueOf(getChar((int) uptime.toDays())) +
                getChar((int) uptime.toHours()) +
                getChar(uptime.toMinutesPart()) +
                getChar(uptime.toSecondsPart()));

    }
}