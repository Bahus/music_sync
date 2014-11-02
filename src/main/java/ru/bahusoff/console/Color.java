package ru.bahusoff.console;

import org.fusesource.jansi.Ansi;

/**
 *
 */
public class Color {
    private Color() {}

    public static String getColoredText(String text, Ansi.Color color, boolean bright) {
        Ansi string = new Ansi();

        if (bright) {
            string.fgBright(color);
        } else {
            string.fg(color);
        }

        string.a(text);
        string.reset();
        return string.toString();
    }

    public static String red(String text, boolean bright) {
        return getColoredText(text, Ansi.Color.RED, bright);
    }

    public static String red(String text) {
        return red(text, false);
    }

    public static String green(String text, boolean bright) {
        return getColoredText(text, Ansi.Color.GREEN, bright);
    }

    public static String green(String text) {
        return green(text, false);
    }
}
