package ru.worm.discord.chill.util;

public class TextUtil {
    public static boolean isEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }

    /**
     * Discord message length limit. Slightly less than actual limit (2000)
     */
    public static int DSCD_LIM = 1950;
    public static String[] splitMessage(String msg) {
        int msgLength = msg.length();
        if (msgLength < DSCD_LIM) {
            return new String[] {msg};
        }
        boolean needsWrapping = msg.startsWith("```");
        int numberOfMessages = (msgLength / DSCD_LIM) + ((msgLength % DSCD_LIM) > 0 ? 1 : 0);
        String[] messages = new String[numberOfMessages];
        for (int i = 0; i < numberOfMessages; i++) {
            StringBuilder sliceBuilder = new StringBuilder();
            int start = i * DSCD_LIM;
            int end = Math.min(start + DSCD_LIM, msg.length());

            String slice = msg.substring(start, end);

            if (needsWrapping) {
                if (i != 0) {
                    sliceBuilder.append("```\n");
                }
                sliceBuilder.append(slice);
                if (i != numberOfMessages - 1) {
                    sliceBuilder.append("\n```");
                }
            } else {
                sliceBuilder.append(slice);
            }
            messages[i] = sliceBuilder.toString();
        }
        return messages;
    }
}
