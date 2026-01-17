package dev.nxtime.hidearmor.commands;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.universe.world.World;
import dev.nxtime.hidearmor.util.ColorConfig;

import javax.annotation.Nonnull;
import java.util.Arrays;

/**
 * Debug command for inspecting armor inventory slot indices and item IDs.
 *
 * @author nxtime
 * @version 0.5.0
 */
public class HideHelmetDebugCommand extends AbstractPlayerCommand {

    public HideHelmetDebugCommand(String name, String description) {
        super(name, description);
        requirePermission("dev.nxtime.hidearmor.command.debug");
    }

    @Override
    protected void execute(@Nonnull CommandContext context, @Nonnull Store<EntityStore> store,
            @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world) {
        Player player = store.getComponent(ref, Player.getComponentType());
        if (player == null)
            return;

        ItemContainer armor = player.getInventory().getArmor();

        player.sendMessage(Message.join(
                Message.raw(ColorConfig.BRAND).color(ColorConfig.PREFIX_COLOR),
                Message.raw("Armor capacity: ").color(ColorConfig.TEXT),
                Message.raw(String.valueOf(armor.getCapacity())).color(ColorConfig.HIGHLIGHT)));

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
                    Message.raw(ids[i]).color(ColorConfig.TEXT)));
        }
    }
}
