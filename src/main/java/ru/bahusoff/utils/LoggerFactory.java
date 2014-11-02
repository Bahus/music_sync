package ru.bahusoff.utils;

import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.ConsoleHandler;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.StreamHandler;

public class LoggerFactory {

    public static org.slf4j.Logger getLogger(Class clazz, Level level) {
        // check level for global logger
        Level global_level = Logger.getGlobal().getLevel();
        Level effective_level = Level.OFF;

        if (global_level != null && !global_level.equals(Level.OFF)) {
            effective_level = global_level.intValue() < level.intValue() ? global_level : level;
        }

        Logger logger = Logger.getLogger(clazz.getName());
        logger.setUseParentHandlers(false);

        Formatter formatter = new CustomFormatter();
        StreamHandler handler = new StdoutConsoleHandler();
        handler.setFormatter(formatter);
        handler.setLevel(effective_level);

        logger.addHandler(handler);
        logger.setLevel(effective_level);

        return org.slf4j.LoggerFactory.getLogger(clazz);
    }

    public static org.slf4j.Logger getLogger(Class clazz) {
        return getLogger(clazz, Level.INFO);
    }
}

class CustomFormatter extends Formatter {
    private static final DateFormat df = new SimpleDateFormat("dd.MM.yyyy hh:mm:ss.SSS");

    public String format(LogRecord record) {
        StringBuilder builder = new StringBuilder(1000);

        builder.append(df.format(new Date(record.getMillis()))).append(" ");
        builder.append(String.format("%30.30s", splitString(record.getLoggerName(), 30)));
        builder.append(" ");
        builder.append("[").append(String.format("%-8s", record.getLevel())).append("]");
        builder.append(" ");
        builder.append(formatMessage(record));
        builder.append("\n");

        return builder.toString();
    }

    private String splitString(String str, int maxLength) {
        int startIndex = str.length() > maxLength ? (str.length() - maxLength) : 0;
        return str.substring(startIndex);
    }
}

class StdoutConsoleHandler extends ConsoleHandler {
    protected void setOutputStream(OutputStream out) throws SecurityException {
        super.setOutputStream(System.out); // kitten killed here :-(
    }
}