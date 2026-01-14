package dev.nxtime.hidearmor.gui;

import com.hypixel.hytale.server.core.entity.entities.Player;
import dev.nxtime.hidearmor.HideArmorState;

import javax.annotation.Nonnull;
import java.util.function.BiConsumer;

/**
 * GUI controller for the HideArmor interactive menu.
 * Displays a UI page with checkboxes for toggling armor visibility.
 * 
 * This class attempts to use Hytale's .ui asset system (similar to AdminUI).
 * NOTE: The UI API may not be fully available in current Early Access SDK.
 */
public class HideArmorGui {

    private static final String UI_PATH = "dev.nxtime_HideArmor_Menu.ui";
    private static BiConsumer<String, String> logger;

    /**
     * Initializes the GUI with logging functions.
     * This should be called during plugin setup.
     * 
     * @param logFunction Function that accepts (level, message) for logging
     */
    public static void init(@Nonnull BiConsumer<String, String> logFunction) {
        logger = logFunction;
    }

    /**
     * Opens the armor visibility GUI for the specified player.
     * 
     * @param player The player to open the GUI for
     */
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
     * This will be called when a player clicks a checkbox.
     * 
     * @param player     The player who clicked
     * @param checkboxId The ID of the checkbox clicked
     */
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
                boolean hideAll = currentMask != 15;
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
     * Fallback to chat-based UI when .ui system is not available.
     */
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

        boolean allHidden = mask == 15;
        String allCheckbox = allHidden ? "§a[§2✓§a]" : "§7[§8 §7]";
        String allStatus = allHidden ? "§7Hidden" : "§fVisible";
        player.sendMessage(com.hypixel.hytale.server.core.Message.raw(
                String.format("§6║  %s §e§lAll Armor: %s     §6║", allCheckbox, allStatus)));

        player.sendMessage(com.hypixel.hytale.server.core.Message.raw("§6╚═══════════════════════════╝"));
        player.sendMessage(com.hypixel.hytale.server.core.Message.raw(""));
        player.sendMessage(com.hypixel.hytale.server.core.Message.raw("§7Use /hidearmor <piece> to toggle"));
    }

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

    private static String removeFormatting(String text) {
        return text.replaceAll("§.", "");
    }

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
