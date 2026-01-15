package dev.nxtime.hidearmor.commands;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.universe.world.World;
import dev.nxtime.hidearmor.HideArmorState;

import javax.annotation.Nonnull;

/**
 * Command to open the interactive armor visibility GUI.
 *
 * @author nxtime
 * @version 0.5.0
 */
public class HideArmorUICommand extends AbstractPlayerCommand {

    public HideArmorUICommand(String name, String description) {
        super(name, description);
        setAllowsExtraArguments(true);
    }

    @Override
    protected void execute(@Nonnull CommandContext context, @Nonnull Store<EntityStore> store,
            @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world) {
        Player player = store.getComponent(ref, Player.getComponentType());
        if (player == null)
            return;

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

    @Deprecated
    private void displayUI(Player player) {
        int mask = HideArmorState.getMask(player.getUuid());
        player.sendMessage(Message.raw(""));
        player.sendMessage(Message.raw("§6╔═══════════════════════════╗"));
        player.sendMessage(Message.raw("§6║  §e§lArmor Visibility Menu  §6║"));
        player.sendMessage(Message.raw("§6╠═══════════════════════════╣"));
        player.sendMessage(Message.raw(""));
        sendCheckbox(player, "Helmet", HideArmorState.SLOT_HEAD, mask);
        sendCheckbox(player, "Chestplate", HideArmorState.SLOT_CHEST, mask);
        sendCheckbox(player, "Gauntlets", HideArmorState.SLOT_HANDS, mask);
        sendCheckbox(player, "Leggings", HideArmorState.SLOT_LEGS, mask);
        player.sendMessage(Message.raw(""));
        player.sendMessage(Message.raw("§6╠═══════════════════════════╣"));
        sendAllCheckbox(player, mask);
        player.sendMessage(Message.raw("§6╚═══════════════════════════╝"));
        player.sendMessage(Message.raw(""));
        player.sendMessage(Message.raw("§7Click to toggle • Use /hidearmor for commands"));
    }

    @Deprecated
    private void sendCheckbox(Player player, String label, int slot, int mask) {
        boolean hidden = (mask & (1 << slot)) != 0;
        String checkbox = hidden ? "§a[§2✓§a]" : "§7[§8 §7]";
        String status = hidden ? "§7Hidden" : "§fVisible";
        String line = String.format("§6║  %s §e%s: %s", checkbox, label, status);
        while (removeFormatting(line).length() < 29) {
            line += " ";
        }
        line += "§6║";
        player.sendMessage(Message.raw(line));
    }

    @Deprecated
    private void sendAllCheckbox(Player player, int mask) {
        boolean allHidden = (mask & 0xF) == 0xF;
        String checkbox = allHidden ? "§a[§2✓§a]" : "§7[§8 §7]";
        String status = allHidden ? "§7Hidden" : "§fVisible";
        String line = String.format("§6║  %s §e§lAll Armor: %s", checkbox, status);
        while (removeFormatting(line).length() < 29) {
            line += " ";
        }
        line += "§6║";
        player.sendMessage(Message.raw(line));
    }

    @Deprecated
    private String removeFormatting(String text) {
        return text.replaceAll("§.", "");
    }
}
