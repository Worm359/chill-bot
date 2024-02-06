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

    public static final Options urlAndShuffle;
    public static final Option optUrlRequired;
    public static final Option shuffle;
    static {
        urlAndShuffle = new Options();
        optUrlRequired = new Option("url", true, "url with youtube playlist");
        optUrlRequired.setRequired(true);
        urlAndShuffle.addOption(optUrlRequired);
        shuffle = new Option("s", "shuffle", false, "will shuffle youtube playlist, before adding adding tracks to queue");
        urlAndShuffle.addOption(shuffle);
    }

    public static final Options player;
    public static final Option optStop = new Option("s", "stop",false, "stop playing");
    public static final Option optStart = new Option("p", "play",false, "start playing");


    public static final Options offAndPassword;
    public static final Option off;
    public static final Option password;

    static {
        offAndPassword = new Options();
        off = new Option("off", false, "to disable locking");
        off.setRequired(false);
        offAndPassword.addOption(off);
        password = new Option("p", "password", true, "password");
        password.setRequired(true);
        offAndPassword.addOption(password);
    }


    static {
        player = new Options();
        OptionGroup grp = new OptionGroup();
        grp.addOption(optStart);
        grp.addOption(optStop);
        grp.setRequired(true);
        player.addOptionGroup(grp);
    }

    public static final Options emptyOptions = new Options();
}
