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
    public static final Options idOrUrl;
    public static final Option optId = new Option("id", true, "track.id from history/playlist");
    public static final Option optUrl = new Option("url", true, "url for youtube video");
    static {
        idOrUrl = new Options();
        OptionGroup playNextGroup = new OptionGroup();
        playNextGroup.addOption(optId);//grp.addOption(new Option("id", true, "track.id from history/playlist"));
        playNextGroup.addOption(optUrl);//grp.addOption(new Option("url", true, "url for youtube video"));
        playNextGroup.setRequired(true);
        idOrUrl.addOptionGroup(playNextGroup);
    }

    public static final Options id;
    public static final Option optIdRequired;
    static {
        id = new Options();
        optIdRequired = new Option("id", true, "track.id from history/playlist");
        optIdRequired.setRequired(true);
        id.addOption(optIdRequired);
    }

    public static final Options emptyOptions = new Options();
}
