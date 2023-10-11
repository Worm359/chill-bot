package ru.worm.discord.chill.logic.command.validation;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.ParseException;
import ru.worm.discord.chill.logic.command.CliOption;
import ru.worm.discord.chill.logic.command.IOptionValidator;
import ru.worm.discord.chill.util.TextUtil;

import java.net.MalformedURLException;
import java.net.URL;

public class PlayNextValidator implements IOptionValidator {
    public static final PlayNextValidator INSTANCE = new PlayNextValidator();
    @Override
    public void validate(CommandLine opts) throws ParseException {
        String id = opts.getOptionValue(CliOption.playNextId);
        String url = opts.getOptionValue(CliOption.playNextUrl);
        if (!TextUtil.isEmpty(id)) {
            try {
                Integer idNum = Integer.valueOf(id);
            } catch (Throwable e) {
                throw new ParseException("couldn't pares 'id' value " + id);
            }
        }
        if (!TextUtil.isEmpty(url)) {
            try {
                URL uri = new URL(url);
            } catch (MalformedURLException e) {
                throw new ParseException("couldn't pares 'url' value " + url);
            }
        }
    }
}
