package dev.nxtime.hidearmor.commands;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import com.hypixel.hytale.server.core.entity.entities.Player;
import dev.nxtime.hidearmor.HideArmorState;

import javax.annotation.Nonnull;

/**
 * Interactive UI command that displays a visual checkbox menu for armor
 * visibility.
 */
public class HideArmorUICommand extends CommandBase {

    public HideArmorUICommand(String name, String description) {
        super(name, description);
        setAllowsExtraArguments(true);
    }

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

    private void sendAllCheckbox(Player player, int mask) {
        boolean allHidden = mask == 15;
        String checkbox = allHidden ? "§a[§2✓§a]" : "§7[§8 §7]";
        String status = allHidden ? "§7Hidden" : "§fVisible";

        String line = String.format("§6║  %s §e§lAll Armor: %s", checkbox, status);
        while (removeFormatting(line).length() < 29) {
            line += " ";
        }
        line += "§6║";

        player.sendMessage(Message.raw(line));
    }

    private String removeFormatting(String text) {
        return text.replaceAll("§.", "");
    }
}
