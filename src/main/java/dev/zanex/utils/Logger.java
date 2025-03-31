package dev.zanex.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Logger {
    // ANSI color codes
    public static final String RESET = "\u001B[0m";
    public static final String RED = "\u001B[31m";
    public static final String GREEN = "\u001B[32m";
    public static final String YELLOW = "\u001B[33m";
    public static final String BLUE = "\u001B[34m";
    public static final String PURPLE = "\u001B[35m";
    public static final String CYAN = "\u001B[36m";

    // Log levels
    public enum Level {
        INFO(BLUE, "INFO"),
        WARNING(YELLOW, "WARN"),
        ERROR(RED, "ERROR"),
        SUCCESS(GREEN, "SUCCESS"),
        DEBUG(CYAN, "DEBUG");

        private final String color;
        private final String prefix;

        Level(String color, String prefix) {
            this.color = color;
            this.prefix = prefix;
        }
    }

    private final String className;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private boolean enableColors = true;

    public Logger(Class<?> clazz) {
        this.className = clazz.getSimpleName();
    }

    public Logger(String className) {
        this.className = className;
    }

    public void log(Level level, String message) {
        String timestamp = dateFormat.format(new Date());
        String colorStart = enableColors ? level.color : "";
        String colorEnd = enableColors ? RESET : "";

        System.out.printf("%s%s [%s] [%s] %s%s%n",
                colorStart, timestamp, level.prefix, className, message, colorEnd);
    }

    public void info(String message) {
        log(Level.INFO, message);
    }

    public void warning(String message) {
        log(Level.WARNING, message);
    }

    public void error(String message) {
        log(Level.ERROR, message);
    }

    public void success(String message) {
        log(Level.SUCCESS, message);
    }

    public void debug(String message) {
        log(Level.DEBUG, message);
    }

    public void disableColors() {
        this.enableColors = false;
    }

    public void enableColors() {
        this.enableColors = true;
    }
}