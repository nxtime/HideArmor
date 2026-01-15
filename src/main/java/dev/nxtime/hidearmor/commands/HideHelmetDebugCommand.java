package dev.nxtime.hidearmor.commands;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import dev.nxtime.hidearmor.util.ColorConfig;

import javax.annotation.Nonnull;
import java.util.Arrays;

/**
 * Debug command for inspecting armor inventory slot indices and item IDs.
 * <p>
 * This command is useful during development to verify which armor slot
 * corresponds
 * to which equipment piece (helmet, chestplate, gauntlets, leggings). It
 * displays
 * the total armor container capacity and lists each slot with its current item
 * ID.
 * <p>
 * <b>Usage:</b> {@code /hhdebug}
 * <p>
 * <b>Output example:</b>
 * 
 * <pre>
 * Armor capacity: 4
 * Armor[0]: hytale:items/armor/iron_helmet
 * Armor[1]: hytale:items/armor/iron_chestplate
 * Armor[2]: hytale:items/armor/iron_gauntlets
 * Armor[3]: hytale:items/armor/iron_leggings
 * </pre>
 * <p>
 * This information helps map the correct
 * {@link dev.nxtime.hidearmor.HideArmorState} slot constants
 * (SLOT_HEAD, SLOT_CHEST, SLOT_HANDS, SLOT_LEGS) to the actual inventory
 * indices
 * used by the Hytale server.
 *
 * @author nxtime
 * @version 0.4.0
 * @see dev.nxtime.hidearmor.HideArmorState
 */
public class HideHelmetDebugCommand extends CommandBase {

    /**
     * Creates the debug command.
     *
     * @param name        the command name (typically "hhdebug")
     * @param description the command description shown in help
     */
    public HideHelmetDebugCommand(String name, String description) {
        super(name, description);
    }

    /**
     * Executes the debug command to display armor slot information.
     * <p>
     * Retrieves the player's armor container and iterates through all slots,
     * displaying the slot index and item ID for each equipped armor piece.
     * Empty slots show "null" as the item ID.
     * <p>
     * The output helps verify that the armor slot indices used in
     * {@link dev.nxtime.hidearmor.HideArmorState} match the actual inventory
     * structure.
     *
     * @param context the command execution context containing the sender
     */
    @Override
    protected void executeSync(@Nonnull CommandContext context) {
        var sender = context.sender();
        if (!(sender instanceof Player player))
            return;

        ItemContainer armor = player.getInventory().getArmor();

        // First print the total armor capacity, then iterate through each slot showing
        // its item ID
        player.sendMessage(Message.join(
                Message.raw(ColorConfig.BRAND).color(ColorConfig.PREFIX_COLOR),
                Message.raw("Armor capacity: ").color(ColorConfig.TEXT),
                Message.raw(String.valueOf(armor.getCapacity())).color(ColorConfig.HIGHLIGHT)));

        // Use forEachWithMeta to iterate through armor slots with their metadata
        // We pass an array to collect item IDs from each slot
        String[] ids = new String[armor.getCapacity()];
        Arrays.fill(ids, "");

        armor.forEachWithMeta((slot, itemStack, arr) -> {
            String id = (itemStack != null) ? itemStack.getItemId() : "null";
            ids[slot] = id;
        }, ids);

        for (int i = 0; i < ids.length; i++) {
            player.sendMessage(Message.join(
                    Message.raw(ColorConfig.BRAND).color(ColorConfig.PREFIX_COLOR),
                    Message.raw("Armor[").color(ColorConfig.TEXT),
                    Message.raw(String.valueOf(i)).color(ColorConfig.HIGHLIGHT),
                    Message.raw("]: ").color(ColorConfig.TEXT),
                    Message.raw(ids[i]).color(ColorConfig.TEXT))); // ID in white/gray
        }
    }
}
