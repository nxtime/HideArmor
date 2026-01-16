package dev.nxtime.hidearmor.util;

/**
 * Centralized logging utility for the HideArmor plugin.
 * <p>
 * Provides consistent formatting for console output with proper prefixing
 * and log levels. Uses System.out/System.err for compatibility with the
 * Hytale server environment.
 *
 * @author nxtime
 * @version 0.7.0
 */
public final class PluginLogger {

    private static final String PREFIX = "[HideArmor]";

    /** Enable/disable debug logging globally */
    private static boolean debugEnabled = false;

    private PluginLogger() {
        // Utility class - prevent instantiation
    }

    /**
     * Sets whether debug logging is enabled.
     *
     * @param enabled true to enable debug logging
     */
    public static void setDebugEnabled(boolean enabled) {
        debugEnabled = enabled;
    }

    /**
     * Checks if debug logging is enabled.
     *
     * @return true if debug logging is enabled
     */
    public static boolean isDebugEnabled() {
        return debugEnabled;
    }

    /**
     * Logs an informational message.
     *
     * @param message the message to log
     */
    public static void info(String message) {
        System.out.println(PREFIX + " [INFO] " + message);
    }

    /**
     * Logs an informational message with format arguments.
     *
     * @param format the format string
     * @param args   the format arguments
     */
    public static void info(String format, Object... args) {
        System.out.println(PREFIX + " [INFO] " + String.format(format, args));
    }

    /**
     * Logs a warning message.
     *
     * @param message the message to log
     */
    public static void warn(String message) {
        System.err.println(PREFIX + " [WARN] " + message);
    }

    /**
     * Logs a warning message with format arguments.
     *
     * @param format the format string
     * @param args   the format arguments
     */
    public static void warn(String format, Object... args) {
        System.err.println(PREFIX + " [WARN] " + String.format(format, args));
    }

    /**
     * Logs an error message.
     *
     * @param message the message to log
     */
    public static void error(String message) {
        System.err.println(PREFIX + " [ERROR] " + message);
    }

    /**
     * Logs an error message with format arguments.
     *
     * @param format the format string
     * @param args   the format arguments
     */
    public static void error(String format, Object... args) {
        System.err.println(PREFIX + " [ERROR] " + String.format(format, args));
    }

    /**
     * Logs an error message with an exception.
     *
     * @param message   the message to log
     * @param throwable the exception to log
     */
    public static void error(String message, Throwable throwable) {
        System.err.println(PREFIX + " [ERROR] " + message + ": " + throwable.getMessage());
        if (debugEnabled) {
            throwable.printStackTrace();
        }
    }

    /**
     * Logs a debug message (only if debug mode is enabled).
     *
     * @param message the message to log
     */
    public static void debug(String message) {
        if (debugEnabled) {
            System.out.println(PREFIX + " [DEBUG] " + message);
        }
    }

    /**
     * Logs a debug message with format arguments (only if debug mode is enabled).
     *
     * @param format the format string
     * @param args   the format arguments
     */
    public static void debug(String format, Object... args) {
        if (debugEnabled) {
            System.out.println(PREFIX + " [DEBUG] " + String.format(format, args));
        }
    }

    /**
     * Creates a logging bridge for GUI initialization.
     * <p>
     * Returns a BiConsumer that can be passed to HideArmorGui.init().
     *
     * @return a logging bridge function
     */
    public static java.util.function.BiConsumer<String, String> createGuiBridge() {
        return (level, message) -> {
            switch (level) {
                case "WARNING" -> warn(message);
                case "SEVERE" -> error(message);
                default -> info(message);
            }
        };
    }
}
