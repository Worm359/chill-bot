package ru.worm.discord.chill.discord.listener;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.springframework.stereotype.Service;
import ru.worm.discord.chill.discord.Commands;
import ru.worm.discord.chill.discord.GuildObserver;
import ru.worm.discord.chill.logic.command.CliOption;
import ru.worm.discord.chill.logic.command.IOptionValidator;
import ru.worm.discord.chill.logic.command.validation.OffAndPasswordValidator;
import ru.worm.discord.chill.util.Pair;

import javax.annotation.Nonnull;
import java.time.Duration;
import java.util.Objects;

@Service
public class BotLockListener extends MessageListener implements EventListener {
    private volatile String lastDurationChecked;

    public BotLockListener() {
        this.command = Commands.LOCK;
    }


    @Override
    public void onMessageReceived(@Nonnull MessageReceivedEvent event) {
        CommandLine cli = filterWithOptions(event).orElse(null);
        if (cli == null) {
            return;
        }
        String password = cli.getOptionValue(CliOption.password);
        boolean disable = cli.hasOption(CliOption.off);
        if (Objects.equals(lastDurationChecked, password)) {
            if (disable) {
                long guildId = event.getMessage().getGuildIdLong();
                if (guildId == 0) {
                    answer(event, "cannot retrieve guildId");
                } else {
                    GuildObserver.guildIdLock = guildId;
                    answer(event, "locked");
                }
            } else {
                GuildObserver.guildIdLock = null;
                answer(event, "unlocked");
            }
        } else {
            answer(event, "sorry, wrong password");
        }

    }

    @Override
    protected String helpMessage() {
        return "nope.";
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

    @Override
    protected Pair<Options, IOptionValidator> options() {
        return new Pair<>(CliOption.offAndPassword, OffAndPasswordValidator.INSTANCE);
    }
}