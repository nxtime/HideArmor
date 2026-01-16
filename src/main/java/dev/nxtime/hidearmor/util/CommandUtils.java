package dev.nxtime.hidearmor.util;

import com.hypixel.hytale.server.core.command.system.CommandContext;

import java.util.Arrays;

/**
 * Utility class for command argument parsing.
 * <p>
 * Provides standardized methods for extracting arguments from Hytale's
 * {@link CommandContext}, which only provides raw input strings.
 *
 * @author nxtime
 * @version 0.7.0
 */
public final class CommandUtils {

    private CommandUtils() {
        // Utility class - prevent instantiation
    }

    /**
     * Parses command arguments from the context, stripping the command name.
     * <p>
     * Given input "/hidearmor status foo", returns ["status", "foo"].
     *
     * @param context     the command execution context
     * @param commandName the command name to strip (without leading slash)
     * @return array of arguments after the command name, or empty array if none
     */
    public static String[] parseArgs(CommandContext context, String commandName) {
        String input = context.getInputString();
        if (input == null) {
            return new String[0];
        }

        String trimmed = input.trim();
        if (trimmed.isEmpty()) {
            return new String[0];
        }

        String[] parts = trimmed.split("\\s+");
        if (parts.length == 0) {
            return new String[0];
        }

        String first = parts[0];
        if (first.startsWith("/")) {
            first = first.substring(1);
        }

        if (first.equalsIgnoreCase(commandName)) {
            return parts.length > 1 ? Arrays.copyOfRange(parts, 1, parts.length) : new String[0];
        }

        return parts;
    }

    /**
     * Parses subcommand arguments, stripping both command and subcommand.
     * <p>
     * Useful for nested commands like "/hidearmor test enable".
     * Given that input with commandName="hidearmor" and subcommand="test",
     * returns ["enable"].
     *
     * @param context        the command execution context
     * @param commandName    the main command name
     * @param subcommandName the subcommand name to also strip
     * @return array of arguments after the subcommand, or empty array if none
     */
    public static String[] parseSubcommandArgs(CommandContext context, String commandName, String subcommandName) {
        String[] args = parseArgs(context, commandName);
        if (args.length == 0) {
            return new String[0];
        }

        // Check if first arg is the expected subcommand
        if (args[0].equalsIgnoreCase(subcommandName)) {
            return args.length > 1 ? Arrays.copyOfRange(args, 1, args.length) : new String[0];
        }

        return args;
    }

    /**
     * Gets a specific argument by index, or null if not present.
     *
     * @param args  the argument array
     * @param index the index to retrieve
     * @return the argument at the index, or null if out of bounds
     */
    public static String getArg(String[] args, int index) {
        return (args != null && index >= 0 && index < args.length) ? args[index] : null;
    }

    /**
     * Gets a specific argument by index, or a default value if not present.
     *
     * @param args         the argument array
     * @param index        the index to retrieve
     * @param defaultValue the value to return if not found
     * @return the argument at the index, or defaultValue if out of bounds
     */
    public static String getArgOrDefault(String[] args, int index, String defaultValue) {
        String arg = getArg(args, index);
        return arg != null ? arg : defaultValue;
    }
}
