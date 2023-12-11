package ru.worm.discord.chill.logic.command.validation;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.ParseException;
import ru.worm.discord.chill.logic.command.CliOption;
import ru.worm.discord.chill.logic.command.IOptionValidator;
import ru.worm.discord.chill.util.TextUtil;

import java.net.MalformedURLException;
import java.net.URL;

public class UrlValidator implements IOptionValidator {
    public static final UrlValidator INSTANCE = new UrlValidator();

    @Override
    public void validate(CommandLine opts) throws ParseException {
        String url = opts.getOptionValue(CliOption.optUrlRequired);
        if (TextUtil.isEmpty(url)) {
            throw new ParseException("url cannot be empty");
        }
        try {
            URL uri = new URL(url);
        } catch (MalformedURLException e) {
            throw new ParseException("couldn't pares 'url' value " + url);
        }
    }
}
