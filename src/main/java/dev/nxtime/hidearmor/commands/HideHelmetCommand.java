package dev.nxtime.hidearmor.commands;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import com.hypixel.hytale.server.core.entity.entities.Player;
import dev.nxtime.hidearmor.HideArmorState;
import dev.nxtime.hidearmor.util.ColorConfig;

import javax.annotation.Nonnull;

/**
 * Simple legacy command for toggling helmet visibility.
 * <p>
 * This command provides a quick shortcut for hiding/showing the helmet armor
 * piece
 * without needing to use the full {@code /hidearmor} command or GUI. It toggles
 * the helmet visibility state and displays the current status to the player.
 * <p>
 * <b>Usage:</b> {@code /hidehelmet}
 * <p>
 * This is a convenience command that predates the full armor visibility system.
 * For more control, use {@code /hidearmor} or {@code /hidearmorui} instead.
 *
 * @author nxtime
 * @version 0.4.0
 * @see HideArmorState#toggleSlot(java.util.UUID, int)
 * @see HideArmorCommand
 */
public class HideHelmetCommand extends CommandBase {

    /**
     * Creates the helmet toggle command.
     *
     * @param name        the command name (typically "hidehelmet")
     * @param description the command description shown in help
     */
    public HideHelmetCommand(String name, String description) {
        super(name, description);
    }

    /**
     * Executes the helmet toggle command.
     * <p>
     * Toggles the helmet visibility state for the executing player and sends
     * a confirmation message showing the new state (ON/OFF). Triggers an
     * equipment refresh to apply the visual change immediately.
     *
     * @param context the command execution context containing the sender
     */
    @Override
    protected void executeSync(@Nonnull CommandContext context) {

        var sender = context.sender();
        if (!(sender instanceof Player player)) {
            return;
        }

        HideArmorState.toggleSlot(player.getUuid(), HideArmorState.SLOT_HEAD);
        boolean enabled = !HideArmorState.isHidden(player.getUuid(), HideArmorState.SLOT_HEAD);
        player.sendMessage(Message.join(
                Message.raw(ColorConfig.BRAND).color(ColorConfig.PREFIX_COLOR),
                Message.raw("Helmet: ").color(ColorConfig.TEXT),
                Message.raw(enabled ? "Visible" : "Hidden").color(enabled ? ColorConfig.SUCCESS : ColorConfig.ERROR)));

        forceRefresh(player);
    }

    /**
     * Forces an equipment refresh for the player to apply visual changes
     * immediately.
     * <p>
     * Executes on the world thread to avoid concurrent modification issues.
     * Silently catches and ignores any exceptions to prevent disrupting gameplay.
     *
     * @param player the player whose equipment should be refreshed
     */
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
