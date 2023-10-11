package ru.worm.discord.chill.logic.command;

import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;

import java.io.PrintWriter;
import java.io.StringWriter;

public class CliOption {
    //example for 'properties' like input: -EXMP firstKey=firstValue -EXMP secondKey=secondValue
    //CommandLine#getOptionProperties("EXMP");
    //options.addOption(Option.builder("EXMP")
    //        .hasArgs()
    //        .valueSeparator('=')
    //        .build());

    public static final HelpFormatter helpFormatter;
    static {
        helpFormatter = new HelpFormatter();
    }

    public static String help(String command, Options opts) {
        StringWriter out = new StringWriter();
        PrintWriter pw = new PrintWriter(out);
        helpFormatter.printHelp(pw, 80, command, null, opts,
                helpFormatter.getLeftPadding(),
                helpFormatter.getDescPadding(),
                null, true);
        pw.flush();
        return out.toString();
    }

    public static final Option helpOption = Option.builder("h")
            .longOpt("help")
            .hasArg(false)
            .build();

    //play next
    public static final Options playNext;
    public static final Option playNextId = new Option("id", true, "track.id from history/playlist");
    public static final Option playNextUrl = new Option("url", true, "url for youtube video");
    static {
        playNext = new Options();
        OptionGroup playNextGroup = new OptionGroup();
        playNextGroup.addOption(playNextId);//grp.addOption(new Option("id", true, "track.id from history/playlist"));
        playNextGroup.addOption(playNextUrl);//grp.addOption(new Option("url", true, "url for youtube video"));
        playNextGroup.setRequired(true);
        playNext.addOptionGroup(playNextGroup);
    }

    public static final Options emptyOptions = new Options();
}
