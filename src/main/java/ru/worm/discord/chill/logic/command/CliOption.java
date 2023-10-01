package ru.worm.discord.chill.logic.command;

import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;

import java.io.PrintWriter;
import java.io.StringWriter;

public class CliOption {
    public static final HelpFormatter helpFormatter;
    static {
        helpFormatter = new HelpFormatter();
    }

    public static String help(Options opts) {
        StringWriter out = new StringWriter();
        PrintWriter pw = new PrintWriter(out);
        helpFormatter.printHelp(pw, 80, null, null, opts,
                helpFormatter.getLeftPadding(),
                helpFormatter.getDescPadding(),
                null, false);
        pw.flush();
        return out.toString();
    }

    public static final Options addOption;
    static {
        addOption = new Options();
    }

    public static final Options emptyOptions = new Options();
}
