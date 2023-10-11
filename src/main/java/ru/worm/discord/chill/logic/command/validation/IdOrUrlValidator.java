package ru.worm.discord.chill.logic.command.validation;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.ParseException;
import ru.worm.discord.chill.logic.command.CliOption;
import ru.worm.discord.chill.logic.command.IOptionValidator;
import ru.worm.discord.chill.util.TextUtil;

import java.net.MalformedURLException;
import java.net.URL;

public class IdOrUrlValidator implements IOptionValidator {
    public static final IdOrUrlValidator INSTANCE = new IdOrUrlValidator();
    @Override
    public void validate(CommandLine opts) throws ParseException {
        String id = opts.getOptionValue(CliOption.optId);
        String url = opts.getOptionValue(CliOption.optUrl);
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
