package dev.nxtime.hidearmor.commands;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.universe.world.World;

import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import dev.nxtime.hidearmor.gui.HideArmorGuiPage;

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
        requirePermission("dev.nxtime.hidearmor.command.hidearmorui");
    }

    @Override
    protected void execute(@Nonnull CommandContext context, @Nonnull Store<EntityStore> store,
            @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world) {
        Player player = store.getComponent(ref, Player.getComponentType());
        if (player == null)
            return;

        world.execute(() -> {
            var playerRefComponent = store.getComponent(ref,
                    PlayerRef.getComponentType());

            if (playerRefComponent != null) {
                player.getPageManager().openCustomPage(ref, store,
                        new HideArmorGuiPage(playerRefComponent,
                                CustomPageLifetime.CanDismiss));
            }
        });
    }
}
