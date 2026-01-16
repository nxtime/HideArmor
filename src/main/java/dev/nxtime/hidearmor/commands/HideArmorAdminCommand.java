package dev.nxtime.hidearmor.commands;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.universe.world.World;
import dev.nxtime.hidearmor.util.CommandUtils;

import javax.annotation.Nonnull;

/**
 * Admin command to open the default settings configuration GUI or reload
 * config.
 * <p>
 * Usage:
 * <ul>
 * <li>{@code /hidearmoradmin} - Opens the admin configuration GUI</li>
 * <li>{@code /hidearmoradmin reload} - Reloads configuration from disk</li>
 * </ul>
 *
 * @author nxtime
 * @version 0.7.0
 */
public class HideArmorAdminCommand extends AbstractPlayerCommand {

    public HideArmorAdminCommand(String name, String description) {
        super(name, description);
        setAllowsExtraArguments(true);
    }

    @Override
    protected void execute(@Nonnull CommandContext context, @Nonnull Store<EntityStore> store,
            @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world) {

        String[] args = CommandUtils.parseArgs(context, "hidearmoradmin");

        // Handle reload subcommand
        if (args.length > 0 && "reload".equalsIgnoreCase(args[0])) {
            dev.nxtime.hidearmor.HideArmorPlugin plugin = dev.nxtime.hidearmor.HideArmorPlugin.getInstance();
            if (plugin != null) {
                plugin.reloadConfiguration();
                playerRef.sendMessage(com.hypixel.hytale.server.core.Message.raw("Configuration reloaded from disk."));
            } else {
                playerRef.sendMessage(com.hypixel.hytale.server.core.Message.raw("Error: Plugin instance not found."));
            }
            return;
        }

        // Open admin GUI
        Player player = store.getComponent(ref, Player.getComponentType());
        if (player == null)
            return;

        world.execute(() -> {
            var playerRefComponent = store.getComponent(ref,
                    com.hypixel.hytale.server.core.universe.PlayerRef.getComponentType());

            if (playerRefComponent != null) {
                player.getPageManager().openCustomPage(ref, store,
                        new dev.nxtime.hidearmor.gui.HideArmorAdminGuiPage(playerRefComponent,
                                com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime.CanDismiss));
            }
        });
    }
}
