package ru.worm.discord.chill.logic.command;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.ParseException;

public interface IOptionValidator {
    void validate(CommandLine parse) throws ParseException;
}
