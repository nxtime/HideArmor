package dev.nxtime.hidearmor.gui;

import com.hypixel.hytale.server.core.entity.entities.Player;
import dev.nxtime.hidearmor.HideArmorState;

import javax.annotation.Nonnull;
import java.util.function.BiConsumer;

/**
 * Legacy GUI controller for the HideArmor interactive menu.
 * <p>
 * This class was created during early development to explore UI integration options.
 * It includes fallback logic for chat-based UI and experimental checkbox handling.
 * <p>
 * <b>Current Status:</b> This class is no longer actively used. The plugin now uses
 * {@link HideArmorGuiPage} which implements Hytale's native {@code InteractiveCustomUIPage}
 * system with proper event bindings and UI asset loading.
 * <p>
 * The class is kept for reference and as a fallback in case the native UI system
 * encounters issues. The {@link #init(BiConsumer)} method is still called during
 * plugin setup to initialize logging.
 *
 * @author nxtime
 * @version 0.4.0
 * @see HideArmorGuiPage
 * @see HideArmorState
 * @deprecated Replaced by {@link HideArmorGuiPage}, no longer actively used
 */
@Deprecated
public class HideArmorGui {

    /** Path to the UI asset file in the resources directory. */
    private static final String UI_PATH = "dev.nxtime_HideArmor_Menu.ui";

    /** Logger function for warning and error messages. */
    private static BiConsumer<String, String> logger;

    /**
     * Initializes the GUI system with a logging function.
     * <p>
     * This should be called during plugin setup to enable warning and error
     * logging from the GUI system. The logger is still used even though this
     * class is deprecated, as it provides diagnostic information.
     *
     * @param logFunction callback function that accepts (level, message) for logging,
     *                    where level is "INFO", "WARNING", or "SEVERE"
     */
    public static void init(@Nonnull BiConsumer<String, String> logFunction) {
        logger = logFunction;
    }

    /**
     * Attempts to open the armor visibility GUI for the specified player.
     * <p>
     * This method was designed to explore different UI integration approaches but
     * is no longer used. The plugin now opens the GUI via {@link HideArmorGuiPage}
     * directly instead of calling this method.
     * <p>
     * If called, it will log a warning and fall back to the chat-based UI.
     *
     * @param player the player to open the GUI for
     * @deprecated Use {@link dev.nxtime.hidearmor.commands.HideArmorCommand} or
     *             {@link dev.nxtime.hidearmor.commands.HideArmorUICommand} instead
     */
    @Deprecated
    public static void openFor(@Nonnull Player player) {
        try {
            // Load current armor state
            int mask = HideArmorState.getMask(player.getUuid());

            // TODO: Hytale UI API integration
            // The exact API for opening .ui files is not documented yet.
            // Possible methods based on AdminUI reverse engineering:
            //
            // Option 1: UIManager or similar
            // player.openUI(UI_PATH);
            //
            // Option 2: GUI Registry
            // HytaleServer.getUIRegistry().open(player, UI_PATH);
            //
            // Option 3: Custom GUI class
            // new CustomGui(UI_PATH).open(player);

            // For now, log attempt and fall back to chat UI
            if (logger != null) {
                logger.accept("WARNING",
                        "HideArmor: UI system not yet implemented. " +
                                "Falling back to chat-based UI. " +
                                "UI Path: " + UI_PATH);
            }

            // Fallback: Send formatted chat message (our existing UI)
            sendChatFallback(player, mask);

        } catch (Exception e) {
            if (logger != null) {
                logger.accept("SEVERE",
                        "HideArmor: Failed to open GUI - " + e.getMessage());
            }
            e.printStackTrace();
        }
    }

    /**
     * Handles checkbox toggle events from the UI.
     * <p>
     * This method was designed to process click events from the UI asset system
     * but is no longer used. The plugin now handles events through
     * {@link HideArmorGuiPage#handleDataEvent} instead.
     * <p>
     * If called, it would toggle the appropriate armor slot based on the checkbox ID
     * and trigger an equipment refresh.
     *
     * @param player the player who clicked the checkbox
     * @param checkboxId the ID of the checkbox clicked (e.g., "HelmetCheckbox")
     * @deprecated Use {@link HideArmorGuiPage#handleDataEvent} instead
     */
    @Deprecated
    public static void handleCheckboxToggle(@Nonnull Player player, @Nonnull String checkboxId) {
        int currentMask = HideArmorState.getMask(player.getUuid());
        int newMask = currentMask;

        switch (checkboxId) {
            case "HelmetCheckbox" -> newMask = HideArmorState.toggleSlot(player.getUuid(), HideArmorState.SLOT_HEAD);
            case "ChestCheckbox" -> newMask = HideArmorState.toggleSlot(player.getUuid(), HideArmorState.SLOT_CHEST);
            case "GauntletsCheckbox" ->
                newMask = HideArmorState.toggleSlot(player.getUuid(), HideArmorState.SLOT_HANDS);
            case "LeggingsCheckbox" -> newMask = HideArmorState.toggleSlot(player.getUuid(), HideArmorState.SLOT_LEGS);
            case "AllArmorCheckbox" -> {
                boolean hideAll = (currentMask & 0xF) != 0xF; // Only check self-armor bits (0-3)
                newMask = HideArmorState.setAll(player.getUuid(), hideAll);
            }
        }

        // Force equipment refresh to apply changes
        forceRefresh(player);

        // Update UI to reflect new state (refresh the UI)
        // TODO: Refresh UI elements
        // refreshUI(player, newMask);
    }

    /**
     * Displays a fallback chat-based UI when the native UI system is unavailable.
     * <p>
     * Creates a bordered menu using formatted chat messages with checkbox-style
     * indicators showing the current visibility state for each armor piece.
     * <p>
     * This fallback is no longer needed since {@link HideArmorGuiPage} successfully
     * integrates with Hytale's native UI system.
     *
     * @param player the player to display the UI to
     * @param mask the player's current 12-bit visibility mask
     * @deprecated Fallback no longer needed with {@link HideArmorGuiPage}
     */
    @Deprecated
    private static void sendChatFallback(Player player, int mask) {
        player.sendMessage(com.hypixel.hytale.server.core.Message.raw(""));
        player.sendMessage(com.hypixel.hytale.server.core.Message.raw("§6╔═══════════════════════════╗"));
        player.sendMessage(com.hypixel.hytale.server.core.Message.raw("§6║  §e§lArmor Visibility Menu  §6║"));
        player.sendMessage(com.hypixel.hytale.server.core.Message.raw("§6╠═══════════════════════════╣"));
        player.sendMessage(com.hypixel.hytale.server.core.Message.raw(""));

        sendCheckboxLine(player, "Helmet", HideArmorState.SLOT_HEAD, mask);
        sendCheckboxLine(player, "Chestplate", HideArmorState.SLOT_CHEST, mask);
        sendCheckboxLine(player, "Gauntlets", HideArmorState.SLOT_HANDS, mask);
        sendCheckboxLine(player, "Leggings", HideArmorState.SLOT_LEGS, mask);

        player.sendMessage(com.hypixel.hytale.server.core.Message.raw(""));
        player.sendMessage(com.hypixel.hytale.server.core.Message.raw("§6╠═══════════════════════════╣"));

        boolean allHidden = (mask & 0xF) == 0xF; // Only check self-armor bits (0-3)
        String allCheckbox = allHidden ? "§a[§2✓§a]" : "§7[§8 §7]";
        String allStatus = allHidden ? "§7Hidden" : "§fVisible";
        player.sendMessage(com.hypixel.hytale.server.core.Message.raw(
                String.format("§6║  %s §e§lAll Armor: %s     §6║", allCheckbox, allStatus)));

        player.sendMessage(com.hypixel.hytale.server.core.Message.raw("§6╚═══════════════════════════╝"));
        player.sendMessage(com.hypixel.hytale.server.core.Message.raw(""));
        player.sendMessage(com.hypixel.hytale.server.core.Message.raw("§7Use /hidearmor <piece> to toggle"));
    }

    /**
     * Sends a formatted checkbox line for a specific armor piece.
     * <p>
     * Part of the chat-based fallback UI. Displays a checkbox (checked or unchecked)
     * alongside the armor piece name and its visibility status.
     *
     * @param player the player to send the message to
     * @param label the armor piece name (e.g., "Helmet", "Chestplate")
     * @param slot the armor slot index (0-3)
     * @param mask the player's current 12-bit visibility mask
     * @deprecated Part of fallback UI, no longer needed
     */
    @Deprecated
    private static void sendCheckboxLine(Player player, String label, int slot, int mask) {
        boolean hidden = (mask & (1 << slot)) != 0;
        String checkbox = hidden ? "§a[§2✓§a]" : "§7[§8 §7]";
        String status = hidden ? "§7Hidden" : "§fVisible";
        String line = String.format("§6║  %s §e%s: %s", checkbox, label, status);

        while (removeFormatting(line).length() < 29) {
            line += " ";
        }
        line += "§6║";

        player.sendMessage(com.hypixel.hytale.server.core.Message.raw(line));
    }

    /**
     * Removes Minecraft-style color formatting codes from a string.
     * <p>
     * Strips all § (section sign) color codes to get the plain text length
     * for alignment calculations in the chat-based UI.
     *
     * @param text the text with formatting codes
     * @return the plain text without color codes
     * @deprecated Part of fallback UI, no longer needed
     */
    @Deprecated
    private static String removeFormatting(String text) {
        return text.replaceAll("§.", "");
    }

    /**
     * Forces an equipment refresh for the player to apply visual changes immediately.
     * <p>
     * Executes on the world thread to avoid concurrent modification issues.
     * Silently catches and ignores any exceptions to prevent disrupting gameplay.
     *
     * @param player the player whose equipment should be refreshed
     * @deprecated Use the forceRefresh methods in command classes instead
     */
    @Deprecated
    private static void forceRefresh(Player player) {
        var world = player.getWorld();
        if (world == null)
            return;

        world.execute(() -> {
            try {
                player.invalidateEquipmentNetwork();
            } catch (Throwable ignored) {
            }
        });
    }
}
