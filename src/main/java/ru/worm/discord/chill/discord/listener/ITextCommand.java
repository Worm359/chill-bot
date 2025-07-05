package ru.worm.discord.chill.discord.listener;

import org.apache.commons.cli.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.worm.discord.chill.logic.command.IOptionValidator;
import ru.worm.discord.chill.util.Pair;

public interface ITextCommand {

    Logger log = LoggerFactory.getLogger(ITextCommand.class);
    String commandName();
    Pair<Options, IOptionValidator> options();
}