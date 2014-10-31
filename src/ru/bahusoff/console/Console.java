package ru.bahusoff.console;

import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.AnsiConsole;

/**
 * Works with console output (terminal)
 */
public class Console {

    private static String getColoredText(String text, Ansi.Color color, boolean bright) {
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

    public static void reset() {
        Console.eraseScreen();
        Console.setCursor(0, 0);
    }

    public static void newLine() {
        Console.print(Ansi.ansi().newline());
    }

    public static void eraseScreen() {
        Console.print(Ansi.ansi().eraseScreen());
    }

    public static void eraseScreen(Ansi.Erase kind) {
        Console.print(Ansi.ansi().eraseScreen(kind));
    }

    public static void setCursor(int x, int y) {
        Console.print(Ansi.ansi().cursor(y, x));
    }

    public static void print(Ansi ansi) {
        AnsiConsole.out.print(ansi.toString());
        AnsiConsole.out.flush();
    }

    public static void println(String text) {
        Console.println(Ansi.ansi().render(text).reset());
    }

    public static void println(Ansi ansi) {
        AnsiConsole.out.println(ansi.toString());
    }

    public static void printf(String text, Object ... args) {
        AnsiConsole.out.printf(Ansi.ansi().render(text).toString(), args);
        AnsiConsole.out.flush();
    }

    public static void printRed(String text, boolean bright) {
        AnsiConsole.out.println(
            getColoredText(text, Ansi.Color.RED, bright)
        );
    }

    public static void printRed(String text) {
        printRed(text, false);
    }

    public static void printGreen(String text, boolean bright) {
        AnsiConsole.out.println(
            getColoredText(text, Ansi.Color.GREEN, bright)
        );
    }

    public static void printGreen(String text) {
        printGreen(text, false);
    }

}
