package ru.worm.discord.chill.logic.command.validation;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.ParseException;
import ru.worm.discord.chill.logic.command.CliOption;
import ru.worm.discord.chill.logic.command.IOptionValidator;
import ru.worm.discord.chill.util.TextUtil;

public class OffAndPasswordValidator implements IOptionValidator {
    public static final OffAndPasswordValidator INSTANCE = new OffAndPasswordValidator();
    @Override
    public void validate(CommandLine parse) throws ParseException {
        String password = parse.getOptionValue(CliOption.password);
        if (TextUtil.isEmpty(password)) {
            throw new ParseException("password must not be empty");
        }
    }
}
