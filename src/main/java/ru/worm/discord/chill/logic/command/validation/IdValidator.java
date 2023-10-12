package ru.worm.discord.chill.logic.command.validation;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.ParseException;
import ru.worm.discord.chill.logic.command.CliOption;
import ru.worm.discord.chill.logic.command.IOptionValidator;
import ru.worm.discord.chill.util.TextUtil;

public class IdValidator implements IOptionValidator {
    public static final IdValidator INSTANCE = new IdValidator();

    @Override
    public void validate(CommandLine opts) throws ParseException {
        String id = opts.getOptionValue(CliOption.optIdRequired);
        if (!TextUtil.isEmpty(id)) {
            throw new ParseException("id cannot be empty");
        }
        try {
            Integer idCasted = Integer.valueOf(id);
        } catch (Throwable e) {
            throw new ParseException("couldn't parse id " + id);
        }
    }
}
