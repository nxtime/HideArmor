package dev.nxtime.hidearmor.commands;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import com.hypixel.hytale.server.core.entity.entities.Player;
import dev.nxtime.hidearmor.HideArmorState;

import javax.annotation.Nonnull;

/**
 * Command to open the interactive armor visibility GUI.
 * <p>
 * This command opens a native Hytale UI page ({@link dev.nxtime.hidearmor.gui.HideArmorGuiPage})
 * that provides a visual interface with checkboxes for managing all three categories of
 * armor visibility settings:
 * <ul>
 *   <li><b>Hide My Own Armor:</b> Control which of your armor pieces are hidden from your view</li>
 *   <li><b>Hide Other Players' Armor:</b> Choose which armor pieces to hide on other players</li>
 *   <li><b>Let Others Hide My Armor:</b> Grant permissions for which pieces others can hide</li>
 * </ul>
 * <p>
 * The GUI provides real-time updates and is more user-friendly than command-based controls.
 * <p>
 * <b>Usage:</b> {@code /hidearmorui}
 * <p>
 * This command is equivalent to running {@code /hidearmor} with no arguments, which also
 * opens the GUI.
 *
 * @author nxtime
 * @version 0.4.0
 * @see dev.nxtime.hidearmor.gui.HideArmorGuiPage
 * @see HideArmorCommand
 */
public class HideArmorUICommand extends CommandBase {

    /**
     * Creates the UI command.
     *
     * @param name the command name (typically "hidearmorui")
     * @param description the command description shown in help
     */
    public HideArmorUICommand(String name, String description) {
        super(name, description);
        setAllowsExtraArguments(true);
    }

    /**
     * Executes the command to open the armor visibility GUI.
     * <p>
     * Retrieves the player's reference and entity store, then executes on the world
     * thread to safely open the custom UI page. The page is configured with
     * {@link com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime#CanDismiss}
     * so players can close it at any time.
     *
     * @param context the command execution context containing the sender
     */
    @Override
    protected void executeSync(@Nonnull CommandContext context) {
        var sender = context.sender();
        if (!(sender instanceof Player player))
            return;

        var ref = player.getReference();
        if (ref != null && ref.isValid()) {
            var store = ref.getStore();
            var world = store.getExternalData().getWorld();

            world.execute(() -> {
                var playerRefComponent = store.getComponent(ref,
                        com.hypixel.hytale.server.core.universe.PlayerRef.getComponentType());

                if (playerRefComponent != null) {
                    player.getPageManager().openCustomPage(ref, store,
                            new dev.nxtime.hidearmor.gui.HideArmorGuiPage(playerRefComponent,
                                    com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime.CanDismiss));
                }
            });
        }
    }

    /**
     * Legacy method that displays a text-based UI using formatted chat messages.
     * <p>
     * This method is no longer used but is kept for reference. The plugin now uses
     * a native Hytale UI page ({@link dev.nxtime.hidearmor.gui.HideArmorGuiPage})
     * instead of chat-based UI.
     * <p>
     * The text UI displayed a bordered menu with checkboxes showing the current
     * visibility state for each armor piece and an "All Armor" toggle.
     *
     * @param player the player to display the UI to
     * @deprecated Replaced by native UI page, no longer called
     */
    @Deprecated
    private void displayUI(Player player) {
        int mask = HideArmorState.getMask(player.getUuid());

        // Create a Hytale-styled UI using formatted chat messages
        player.sendMessage(Message.raw(""));
        player.sendMessage(Message.raw("§6╔═══════════════════════════╗"));
        player.sendMessage(Message.raw("§6║  §e§lArmor Visibility Menu  §6║"));
        player.sendMessage(Message.raw("§6╠═══════════════════════════╣"));
        player.sendMessage(Message.raw(""));

        // Helmet checkbox
        sendCheckbox(player, "Helmet", HideArmorState.SLOT_HEAD, mask);

        // Chestplate checkbox
        sendCheckbox(player, "Chestplate", HideArmorState.SLOT_CHEST, mask);

        // Gauntlets checkbox
        sendCheckbox(player, "Gauntlets", HideArmorState.SLOT_HANDS, mask);

        // Leggings checkbox
        sendCheckbox(player, "Leggings", HideArmorState.SLOT_LEGS, mask);

        player.sendMessage(Message.raw(""));
        player.sendMessage(Message.raw("§6╠═══════════════════════════╣"));
        sendAllCheckbox(player, mask);
        player.sendMessage(Message.raw("§6╚═══════════════════════════╝"));
        player.sendMessage(Message.raw(""));
        player.sendMessage(Message.raw("§7Click to toggle • Use /hidearmor for commands"));
    }

    /**
     * Sends a formatted checkbox line for a specific armor piece.
     * <p>
     * Displays a checkbox (checked or unchecked) alongside the armor piece name
     * and its visibility status (Hidden/Visible). The line is formatted with
     * color codes and padded to align with the UI border.
     * <p>
     * This is part of the legacy text-based UI and is no longer used.
     *
     * @param player the player to send the message to
     * @param label the armor piece name (e.g., "Helmet", "Chestplate")
     * @param slot the armor slot index (0-3)
     * @param mask the player's current visibility mask
     * @deprecated Part of legacy text-based UI, no longer called
     */
    @Deprecated
    private void sendCheckbox(Player player, String label, int slot, int mask) {
        boolean hidden = (mask & (1 << slot)) != 0;
        String checkbox = hidden ? "§a[§2✓§a]" : "§7[§8 §7]";
        String status = hidden ? "§7Hidden" : "§fVisible";

        // Format: [✓] Helmet: Hidden
        String line = String.format("§6║  %s §e%s: %s", checkbox, label, status);

        // Pad to align with box
        while (removeFormatting(line).length() < 29) {
            line += " ";
        }
        line += "§6║";

        player.sendMessage(Message.raw(line));
    }

    /**
     * Sends a formatted checkbox line for the "All Armor" toggle.
     * <p>
     * Displays whether all armor pieces are currently hidden or visible,
     * with a bold label to emphasize it controls all slots at once.
     * <p>
     * This is part of the legacy text-based UI and is no longer used.
     *
     * @param player the player to send the message to
     * @param mask the player's current visibility mask
     * @deprecated Part of legacy text-based UI, no longer called
     */
    @Deprecated
    private void sendAllCheckbox(Player player, int mask) {
        boolean allHidden = (mask & 0xF) == 0xF; // Only check self-armor bits (0-3)
        String checkbox = allHidden ? "§a[§2✓§a]" : "§7[§8 §7]";
        String status = allHidden ? "§7Hidden" : "§fVisible";

        String line = String.format("§6║  %s §e§lAll Armor: %s", checkbox, status);
        while (removeFormatting(line).length() < 29) {
            line += " ";
        }
        line += "§6║";

        player.sendMessage(Message.raw(line));
    }

    /**
     * Removes Minecraft color formatting codes from a string.
     * <p>
     * Strips all § (section sign) color codes to get the plain text length
     * for alignment calculations in the text-based UI.
     * <p>
     * This is a helper for the legacy text-based UI and is no longer used.
     *
     * @param text the text with formatting codes
     * @return the plain text without color codes
     * @deprecated Part of legacy text-based UI, no longer called
     */
    @Deprecated
    private String removeFormatting(String text) {
        return text.replaceAll("§.", "");
    }
}
