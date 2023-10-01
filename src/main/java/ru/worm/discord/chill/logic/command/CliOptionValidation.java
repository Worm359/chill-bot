package ru.worm.discord.chill.logic.command;

import org.apache.commons.cli.ParseException;

public class CliOptionValidation {
    public static IOptionValidator youtubeLink = (args) -> {
        if (args.getArgList().size() != 1 || !args.getArgList().get(0).contains("youtu")) {
            throw new ParseException("single 'https://www.youtube.com/watch?...' argument expected");
        }
    };
}
