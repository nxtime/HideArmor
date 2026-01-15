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
import dev.nxtime.hidearmor.util.ColorConfig;

import javax.annotation.Nonnull;

/**
 * Simple legacy command for toggling helmet visibility.
 *
 * @author nxtime
 * @version 0.5.0
 */
public class HideHelmetCommand extends AbstractPlayerCommand {

    public HideHelmetCommand(String name, String description) {
        super(name, description);
    }

    @Override
    protected void execute(@Nonnull CommandContext context, @Nonnull Store<EntityStore> store,
            @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world) {
        Player player = store.getComponent(ref, Player.getComponentType());
        if (player == null)
            return;

        HideArmorState.toggleSlot(player.getUuid(), HideArmorState.SLOT_HEAD);
        boolean enabled = !HideArmorState.isHidden(player.getUuid(), HideArmorState.SLOT_HEAD);
        player.sendMessage(Message.join(
                Message.raw(ColorConfig.BRAND).color(ColorConfig.PREFIX_COLOR),
                Message.raw("Helmet: ").color(ColorConfig.TEXT),
                Message.raw(enabled ? "Visible" : "Hidden").color(enabled ? ColorConfig.SUCCESS : ColorConfig.ERROR)));

        forceRefresh(player);
    }

    private void forceRefresh(Player player) {
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
