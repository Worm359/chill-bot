package ru.worm.discord.chill.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

class TextUtilTest {

    @Test
    void testSlice1() {
        TextUtil.DSCD_LIM = 10;
        String message = "012345678901234567890123456789";

        String[] strings = TextUtil.splitMessage(message);

        Assertions.assertEquals(3, strings.length);
        Arrays.stream(strings).forEach(s -> Assertions.assertEquals("0123456789", s));
    }

    @Test
    void testSlice2() {
        TextUtil.DSCD_LIM = 10;
        String message = "0123456789012345";

        String[] strings = TextUtil.splitMessage(message);

        Assertions.assertEquals(2, strings.length);
        Assertions.assertEquals(strings[0], "0123456789");
        Assertions.assertEquals(strings[1], "012345");
    }

    @Test
    void testSlice3() {
        TextUtil.DSCD_LIM = 10;
        String message = "";

        String[] strings = TextUtil.splitMessage(message);

        Assertions.assertEquals(1, strings.length);
        Assertions.assertEquals(strings[0], "");
    }

    @Test
    void testSlice4() {
        TextUtil.DSCD_LIM = 10;
        String[] strings = TextUtil.splitMessage(wrapped);
    }

    private static final String wrapped = """
        ```
        0123456789
        0123456789
        0123456789
        ```""";
}