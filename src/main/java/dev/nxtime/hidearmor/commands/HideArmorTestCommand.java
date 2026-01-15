package dev.nxtime.hidearmor.commands;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import com.hypixel.hytale.server.core.entity.entities.Player;
import dev.nxtime.hidearmor.HideArmorState;
import dev.nxtime.hidearmor.util.ColorConfig;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Debug command for testing the mutual opt-in system in single player mode.
 * <p>
 * <b>Note:</b> This command is disabled by default in production builds.
 * Uncomment the registration in {@link dev.nxtime.hidearmor.HideArmorPlugin} to
 * enable.
 * <p>
 * Allows a player to enable "test mode" where their own armor is treated as if
 * it
 * belongs to another player. This enables testing the mutual opt-in logic
 * without
 * requiring a second player:
 * <ul>
 * <li>Your "hide others" settings control what you want to hide</li>
 * <li>Your "allow others" settings control what can be hidden</li>
 * <li>Armor is hidden only where BOTH are enabled (mutual opt-in)</li>
 * </ul>
 * <p>
 * <b>Commands:</b>
 * <ul>
 * <li>{@code /hidearmor test enable} - Enable test mode</li>
 * <li>{@code /hidearmor test disable} - Disable test mode</li>
 * <li>{@code /hidearmor test status} - Show current test mode settings</li>
 * <li>{@code /hidearmor test simulate} - Preview what will be hidden</li>
 * </ul>
 *
 * @author nxtime
 * @version 0.4.0
 * @see HideArmorState#shouldHideOtherPlayerArmor(UUID, UUID, int)
 */
public class HideArmorTestCommand extends CommandBase {

    /**
     * Fixed UUID for virtual test player.
     * Not actually used; test mode uses the player's own UUID for both viewer and
     * target.
     */
    private static final UUID TEST_PLAYER_UUID = UUID.fromString("00000000-0000-0000-0000-000000000001");

    /** Tracks which players have test mode enabled. */
    private static final Map<UUID, Boolean> TEST_MODE = new HashMap<>();

    /**
     * Creates the test command.
     *
     * @param name        the command name
     * @param description the command description
     */
    public HideArmorTestCommand(String name, String description) {
        super(name, description);
        setAllowsExtraArguments(true);
    }

    /**
     * Executes the test command with the specified action.
     *
     * @param context the command execution context
     */
    @Override
    protected void executeSync(@Nonnull CommandContext context) {
        var sender = context.sender();
        if (!(sender instanceof Player player))
            return;

        String[] args = parseArgs(context);
        if (args.length == 0) {
            sendHelp(player);
            return;
        }

        String action = args[0].toLowerCase();

        switch (action) {
            case "enable" -> {
                TEST_MODE.put(player.getUuid(), true);
                player.sendMessage(Message.join(
                        Message.raw(ColorConfig.BRAND).color(ColorConfig.PREFIX_COLOR),
                        Message.raw("Test Mode Enabled! ").color(ColorConfig.SUCCESS),
                        Message.raw("Your armor will now be treated as if you're another player.")
                                .color(ColorConfig.TEXT)));
                player.sendMessage(Message.join(
                        Message.raw(ColorConfig.BRAND).color(ColorConfig.PREFIX_COLOR),
                        Message.raw("Use ").color(ColorConfig.TEXT),
                        Message.raw("/hidearmor allowothers").color(ColorConfig.HIGHLIGHT),
                        Message.raw(" to control what 'others' can see.").color(ColorConfig.TEXT)));
                player.sendMessage(Message.join(
                        Message.raw(ColorConfig.BRAND).color(ColorConfig.PREFIX_COLOR),
                        Message.raw("Your 'Hide Others' settings will affect your own armor in test mode.")
                                .color(ColorConfig.TEXT)));
            }
            case "disable" -> {
                TEST_MODE.remove(player.getUuid());
                player.sendMessage(Message.join(
                        Message.raw(ColorConfig.BRAND).color(ColorConfig.PREFIX_COLOR),
                        Message.raw("Test Mode Disabled. ").color(ColorConfig.ERROR),
                        Message.raw("Back to normal mode.").color(ColorConfig.TEXT)));
            }
            case "status" -> {
                boolean testMode = TEST_MODE.getOrDefault(player.getUuid(), false);
                player.sendMessage(Message.raw("=== Test Mode Status ===").color(ColorConfig.PREFIX_COLOR));
                player.sendMessage(Message.join(
                        Message.raw("Test Mode: ").color(ColorConfig.TEXT),
                        Message.raw(testMode ? "Enabled" : "Disabled")
                                .color(testMode ? ColorConfig.SUCCESS : ColorConfig.ERROR)));

                if (testMode) {
                    int mask = HideArmorState.getMask(player.getUuid());
                    player.sendMessage(Message.raw(""));
                    player.sendMessage(
                            Message.raw("As the viewer, you have 'Hide Others' set to:").color(ColorConfig.TEXT));
                    player.sendMessage(Message.raw("  " + formatSection(mask, 4, 7)).color(ColorConfig.HIGHLIGHT));
                    player.sendMessage(Message.raw(""));
                    player.sendMessage(
                            Message.raw("As the target, you have 'Allow Others' set to:").color(ColorConfig.TEXT));
                    player.sendMessage(Message.raw("  " + formatSection(mask, 8, 11)).color(ColorConfig.HIGHLIGHT));
                    player.sendMessage(Message.raw(""));
                    player.sendMessage(Message.raw("Your armor will be hidden for slots where BOTH are enabled.")
                            .color(ColorConfig.TEXT));
                }
            }
            case "simulate" -> {
                player.sendMessage(
                        Message.raw("=== Simulating Two-Player Scenario ===").color(ColorConfig.PREFIX_COLOR));
                player.sendMessage(Message.raw("Testing mutual opt-in logic...").color(ColorConfig.TEXT));

                int mask = HideArmorState.getMask(player.getUuid());
                String[] slots = { "Helmet", "Chestplate", "Gauntlets", "Leggings" };

                player.sendMessage(Message.raw(""));
                for (int i = 0; i < 4; i++) {
                    boolean hideOthers = (mask & (1 << (i + 4))) != 0;
                    boolean allowOthers = (mask & (1 << (i + 8))) != 0;
                    boolean willHide = hideOthers && allowOthers;

                    player.sendMessage(Message.join(
                            Message.raw("  "),
                            Message.raw(willHide ? "[HIDDEN] " : "[VISIBLE] ")
                                    .color(willHide ? ColorConfig.ERROR : ColorConfig.SUCCESS),
                            Message.raw(slots[i] + ": ").color(ColorConfig.TEXT),
                            Message.raw("Hide=").color(ColorConfig.TEXT),
                            Message.raw(hideOthers ? "Yes" : "No")
                                    .color(hideOthers ? ColorConfig.SUCCESS : ColorConfig.TEXT),
                            Message.raw(", Allow=").color(ColorConfig.TEXT),
                            Message.raw(allowOthers ? "Yes" : "No")
                                    .color(allowOthers ? ColorConfig.SUCCESS : ColorConfig.TEXT)));
                }
                player.sendMessage(Message.raw(""));
                player.sendMessage(Message.join(
                        Message.raw(ColorConfig.BRAND).color(ColorConfig.PREFIX_COLOR),
                        Message.raw("Use '/hidearmor test enable' to see this in action!").color(ColorConfig.TEXT)));
            }
            default -> sendHelp(player);
        }
    }

    /**
     * Formats a range of bits from a mask into a comma-separated string.
     *
     * @param mask     the full 12-bit mask
     * @param startBit the starting bit position (inclusive)
     * @param endBit   the ending bit position (inclusive)
     * @return comma-separated armor piece names, or "none"
     */
    private String formatSection(int mask, int startBit, int endBit) {
        StringBuilder out = new StringBuilder();
        String[] names = { "Head", "Chest", "Hands", "Legs" };

        for (int i = startBit; i <= endBit; i++) {
            if ((mask & (1 << i)) != 0) {
                if (out.length() > 0)
                    out.append(", ");
                out.append(names[i - startBit]);
            }
        }

        return out.length() > 0 ? out.toString() : "none";
    }

    /**
     * Sends help text explaining test mode commands and usage.
     *
     * @param player the player to send help to
     */
    private void sendHelp(Player player) {
        player.sendMessage(Message.raw("=== HideArmor Test Mode ===").color(ColorConfig.PREFIX_COLOR));
        player.sendMessage(Message.raw("Test the mutual opt-in system in single player:").color(ColorConfig.TEXT));
        player.sendMessage(Message.raw(""));
        player.sendMessage(Message.join(
                Message.raw("/hidearmor test enable ").color(ColorConfig.PREFIX_COLOR),
                Message.raw("- Treat your armor as 'another player'").color(ColorConfig.TEXT)));
        player.sendMessage(Message.join(
                Message.raw("/hidearmor test disable ").color(ColorConfig.PREFIX_COLOR),
                Message.raw("- Return to normal mode").color(ColorConfig.TEXT)));
        player.sendMessage(Message.join(
                Message.raw("/hidearmor test status ").color(ColorConfig.PREFIX_COLOR),
                Message.raw("- Show current test settings").color(ColorConfig.TEXT)));
        player.sendMessage(Message.join(
                Message.raw("/hidearmor test simulate ").color(ColorConfig.PREFIX_COLOR),
                Message.raw("- Preview what will be hidden").color(ColorConfig.TEXT)));
        player.sendMessage(Message.raw(""));
        player.sendMessage(Message.raw("How it works:").color(ColorConfig.TEXT));
        player.sendMessage(
                Message.raw("- Your 'Hide Others' settings = what you want to hide").color(ColorConfig.TEXT));
        player.sendMessage(Message.raw("- Your 'Allow Others' settings = what can be hidden").color(ColorConfig.TEXT));
        player.sendMessage(
                Message.raw("- In test mode, armor hidden only where BOTH are enabled").color(ColorConfig.TEXT));
    }

    /**
     * Parses arguments from the command context.
     * <p>
     * Strips "/hidearmor test" prefix and returns remaining arguments.
     *
     * @param context the command execution context
     * @return array of arguments after "test", or empty array
     */
    private String[] parseArgs(CommandContext context) {
        String input = context.getInputString();
        if (input == null)
            return new String[0];

        String trimmed = input.trim();
        if (trimmed.isEmpty())
            return new String[0];

        String[] parts = trimmed.split("\\s+");
        if (parts.length <= 2) // Need at least "/hidearmor test <action>"
            return new String[0];

        // Skip "/hidearmor test" and return the rest
        String[] result = new String[parts.length - 2];
        System.arraycopy(parts, 2, result, 0, parts.length - 2);
        return result;
    }

    /**
     * Checks if a player has test mode enabled.
     *
     * @param playerUuid the player's UUID
     * @return true if test mode is enabled for this player
     */
    public static boolean isTestModeEnabled(UUID playerUuid) {
        return TEST_MODE.getOrDefault(playerUuid, false);
    }

    /**
     * Gets the target UUID for test mode.
     * <p>
     * In test mode, returns the viewer's own UUID to simulate testing against
     * themselves.
     * In normal mode, returns null.
     *
     * @param viewerUuid the UUID of the player viewing
     * @return the viewer's UUID if test mode is enabled, null otherwise
     */
    public static UUID getTestTargetUuid(UUID viewerUuid) {
        return isTestModeEnabled(viewerUuid) ? viewerUuid : null;
    }
}
