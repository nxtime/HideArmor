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
import dev.nxtime.hidearmor.util.CommandUtils;
import dev.nxtime.hidearmor.gui.HideArmorGuiPage;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;

import javax.annotation.Nonnull;

/**
 * Primary command handler for all armor visibility controls.
 * <p>
 * Handles the {@code /hidearmor} command with multiple subcommands:
 * <ul>
 * <li>No args: Opens the interactive GUI</li>
 * <li>{@code status}: Shows current settings for all three categories</li>
 * <li>{@code <piece>}: Toggles self-armor for specific piece</li>
 * <li>{@code all}: Toggles all self-armor pieces</li>
 * <li>{@code on/off <piece|all>}: Sets self-armor state explicitly</li>
 * <li>{@code hideothers <piece|all>}: Controls hiding on other players</li>
 * <li>{@code allowothers <piece|all>}: Controls permission for others</li>
 * </ul>
 * <p>
 * All armor changes trigger an equipment refresh to update the visual state
 * immediately.
 *
 * @author nxtime
 * @version 0.4.0
 * @see HideArmorState
 */
public class HideArmorCommand extends AbstractPlayerCommand {

    /**
     * Creates the command with the specified name and description.
     *
     * @param name        the command name (typically "hidearmor")
     * @param description the command description shown in help
     */
    public HideArmorCommand(String name, String description) {
        super(name, description);
        setAllowsExtraArguments(true);
        requirePermission("dev.nxtime.hidearmor.command.hidearmor");
    }

    /**
     * Executes the command based on the provided arguments.
     * <p>
     * Routes to appropriate handler based on subcommand or opens GUI if no args.
     *
     * @param context the command execution context containing sender and arguments
     */
    @Override
    protected void execute(@Nonnull CommandContext context, @Nonnull Store<EntityStore> store,
            @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world) {
        Player player = store.getComponent(ref, Player.getComponentType());
        if (player == null)
            return;

        String[] args = CommandUtils.parseArgs(context, "hidearmor");
        if (args.length == 0) {
            // Open the GUI when no arguments provided
            openGui(player);
            return;
        }

        String first = args[0].toLowerCase();
        if ("status".equals(first)) {
            sendStatus(player);
            return;
        }

        // Handle hideothers subcommand
        if ("hideothers".equals(first)) {
            handleHideOthers(player, args);
            return;
        }

        // Handle allowothers subcommand
        if ("allowothers".equals(first)) {
            handleAllowOthers(player, args);
            return;
        }

        // Test mode (disabled in production)
        // Uncomment to enable single-player testing
        // if ("test".equals(first)) {
        // new HideArmorTestCommand("test", "Test mode").executeSync(context);
        // return;
        // }

        if ("all".equals(first)) {
            int current = HideArmorState.getMask(player.getUuid());
            int newMask = HideArmorState.setAll(player.getUuid(), (current & 0xF) != 0xF);
            player.sendMessage(Message.join(
                    Message.raw(ColorConfig.BRAND).color(ColorConfig.PREFIX_COLOR),
                    Message.raw(HideArmorState.formatMask(newMask)).color(ColorConfig.TEXT)));
            forceRefresh(player);
            return;
        }

        if ("on".equals(first) || "off".equals(first)) {
            if (args.length < 2) {
                sendHelp(player);
                return;
            }

            boolean enable = "on".equals(first);
            String target = args[1].toLowerCase();
            if ("all".equals(target)) {
                int newMask = HideArmorState.setAll(player.getUuid(), enable);
                player.sendMessage(Message.join(
                        Message.raw(ColorConfig.BRAND).color(ColorConfig.PREFIX_COLOR),
                        Message.raw(HideArmorState.formatMask(newMask)).color(ColorConfig.TEXT)));
                forceRefresh(player);
                return;
            }

            int slot = resolveSlot(target);
            if (slot < 0) {
                sendHelp(player);
                return;
            }

            int mask = HideArmorState.getMask(player.getUuid());
            int bit = 1 << slot;
            int newMask = enable ? (mask | bit) : (mask & ~bit);
            HideArmorState.setMask(player.getUuid(), newMask);
            player.sendMessage(Message.join(
                    Message.raw(ColorConfig.BRAND).color(ColorConfig.PREFIX_COLOR),
                    Message.raw(HideArmorState.formatMask(newMask)).color(ColorConfig.TEXT)));
            forceRefresh(player);
            return;
        }

        int slot = resolveSlot(first);
        if (slot >= 0) {
            int newMask = HideArmorState.toggleSlot(player.getUuid(), slot);
            player.sendMessage(Message.join(
                    Message.raw(ColorConfig.BRAND).color(ColorConfig.PREFIX_COLOR),
                    Message.raw(HideArmorState.formatMask(newMask)).color(ColorConfig.TEXT)));
            forceRefresh(player);
            return;
        }

        sendHelp(player);
    }

    /**
     * Sends command usage help to the player.
     *
     * @param player the player to send help to
     */
    private void sendHelp(Player player) {
        player.sendMessage(Message.join(
                Message.raw(ColorConfig.BRAND).color(ColorConfig.PREFIX_COLOR),
                Message.raw("Usage: /hidearmor [status|all|hideothers|allowothers|on|off] [piece]")
                        .color(ColorConfig.HIGHLIGHT)));
        player.sendMessage(Message.join(
                Message.raw("  /hidearmor ").color(ColorConfig.TEXT),
                Message.raw("- Open GUI").color(ColorConfig.HIGHLIGHT)));
        player.sendMessage(Message.join(
                Message.raw("  /hidearmor status ").color(ColorConfig.TEXT),
                Message.raw("- Show all settings").color(ColorConfig.HIGHLIGHT)));
        player.sendMessage(Message.join(
                Message.raw("  /hidearmor hideothers [all|head|chest|hands|legs] ").color(ColorConfig.TEXT),
                Message.raw("- Hide other players' armor").color(ColorConfig.HIGHLIGHT)));
        player.sendMessage(Message.join(
                Message.raw("  /hidearmor allowothers [all|head|chest|hands|legs] ").color(ColorConfig.TEXT),
                Message.raw("- Allow others to hide your armor").color(ColorConfig.HIGHLIGHT)));
    }

    /**
     * Sends a formatted status message showing all three categories of settings.
     * <p>
     * Displays:
     * <ul>
     * <li>Hide My Armor (bits 0-3)</li>
     * <li>Hide Others' Armor (bits 4-7)</li>
     * <li>Allow Others (bits 8-11)</li>
     * </ul>
     *
     * @param player the player to send status to
     */
    private void sendStatus(Player player) {
        int mask = HideArmorState.getMask(player.getUuid());

        // Self armor (bits 0-3)
        String selfArmor = formatSectionMask(mask, 0, 3);

        // Hide others (bits 4-7)
        String hideOthers = formatSectionMask(mask, 4, 7);

        // Allow others (bits 8-11)
        String allowOthers = formatSectionMask(mask, 8, 11);

        player.sendMessage(Message.raw("=== Armor Visibility Settings ===").color(ColorConfig.PREFIX_COLOR));
        player.sendMessage(Message.join(
                Message.raw("Hide My Armor: ").color(ColorConfig.HIGHLIGHT),
                Message.raw(selfArmor).color(ColorConfig.TEXT)));
        player.sendMessage(Message.join(
                Message.raw("Hide Others' Armor: ").color(ColorConfig.HIGHLIGHT),
                Message.raw(hideOthers).color(ColorConfig.TEXT)));
        player.sendMessage(Message.join(
                Message.raw("Allow Others: ").color(ColorConfig.HIGHLIGHT),
                Message.raw(allowOthers).color(ColorConfig.TEXT)));
    }

    /**
     * Formats a range of bits from a mask into a human-readable string.
     * <p>
     * Example: bits 4-7 with value 0b00110000 returns "Chest, Hands"
     *
     * @param mask     the full 12-bit mask
     * @param startBit the starting bit position (inclusive)
     * @param endBit   the ending bit position (inclusive)
     * @return comma-separated list of armor pieces, or "none"
     */
    private String formatSectionMask(int mask, int startBit, int endBit) {
        StringBuilder out = new StringBuilder();
        String[] names = { "Head", "Chest", "Hands", "Legs" };

        for (int i = startBit; i <= endBit; i++) {
            if ((mask & (1 << i)) != 0) {
                if (out.length() > 0)
                    out.append(", ");
                out.append(names[i - startBit]);
            }
        }

        return out.length() > 0 ? out.toString() : "none";
    }

    /**
     * Resolves a string argument to an armor slot index.
     *
     * @param arg the argument string ("head", "chest", "hands", or "legs")
     * @return the slot index (0-3), or -1 if invalid
     */
    private int resolveSlot(String arg) {
        return switch (arg) {
            case "head" -> HideArmorState.SLOT_HEAD;
            case "chest" -> HideArmorState.SLOT_CHEST;
            case "hands" -> HideArmorState.SLOT_HANDS;
            case "legs" -> HideArmorState.SLOT_LEGS;
            default -> -1;
        };
    }

    /**
     * Handles the "hideothers" subcommand for controlling visibility of other
     * players' armor.
     * <p>
     * Usage: {@code /hidearmor hideothers <piece|all>}
     *
     * @param player the player executing the command
     * @param args   the full argument array (including "hideothers")
     */
    private void handleHideOthers(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(
                    Message.raw("Usage: /hidearmor hideothers [all|head|chest|hands|legs]").color(ColorConfig.ERROR));
            return;
        }

        String target = args[1].toLowerCase();
        if ("all".equals(target)) {
            int current = HideArmorState.getMask(player.getUuid());
            boolean hideAll = (current & 0xF0) != 0xF0;
            HideArmorState.setAllHideOthers(player.getUuid(), hideAll);
            player.sendMessage(Message.join(
                    Message.raw(ColorConfig.BRAND).color(ColorConfig.PREFIX_COLOR),
                    Message.raw("Hide Others' Armor: ").color(ColorConfig.TEXT),
                    Message.raw(hideAll ? "All" : "None").color(hideAll ? ColorConfig.ERROR : ColorConfig.SUCCESS)));
            forceRefresh(player);
            return;
        }

        int slot = resolveSlot(target);
        if (slot < 0) {
            player.sendMessage(Message.raw("Invalid piece. Use: head, chest, hands, or legs").color(ColorConfig.ERROR));
            return;
        }

        HideArmorState.toggleHideOthers(player.getUuid(), slot);
        int mask = HideArmorState.getMask(player.getUuid());
        String hideOthers = formatSectionMask(mask, 4, 7);
        player.sendMessage(Message.join(
                Message.raw(ColorConfig.BRAND).color(ColorConfig.PREFIX_COLOR),
                Message.raw("Hide Others' Armor: ").color(ColorConfig.TEXT),
                Message.raw(hideOthers).color(ColorConfig.HIGHLIGHT)));
        forceRefresh(player);
    }

    /**
     * Handles the "allowothers" subcommand for controlling permissions.
     * <p>
     * Usage: {@code /hidearmor allowothers <piece|all>}
     *
     * @param player the player executing the command
     * @param args   the full argument array (including "allowothers")
     */
    private void handleAllowOthers(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(
                    Message.raw("Usage: /hidearmor allowothers [all|head|chest|hands|legs]").color(ColorConfig.ERROR));
            return;
        }

        String target = args[1].toLowerCase();
        if ("all".equals(target)) {
            int current = HideArmorState.getMask(player.getUuid());
            boolean allowAll = (current & 0xF00) != 0xF00;
            HideArmorState.setAllAllowOthers(player.getUuid(), allowAll);
            player.sendMessage(Message.join(
                    Message.raw(ColorConfig.BRAND).color(ColorConfig.PREFIX_COLOR),
                    Message.raw("Allow Others: ").color(ColorConfig.TEXT),
                    Message.raw(allowAll ? "All" : "None").color(allowAll ? ColorConfig.SUCCESS : ColorConfig.ERROR)));
            forceRefresh(player);
            return;
        }

        int slot = resolveSlot(target);
        if (slot < 0) {
            player.sendMessage(Message.raw("Invalid piece. Use: head, chest, hands, or legs").color(ColorConfig.ERROR));
            return;
        }

        HideArmorState.toggleAllowOthers(player.getUuid(), slot);
        int mask = HideArmorState.getMask(player.getUuid());
        String allowOthers = formatSectionMask(mask, 8, 11);
        player.sendMessage(Message.join(
                Message.raw(ColorConfig.BRAND).color(ColorConfig.PREFIX_COLOR),
                Message.raw("Allow Others: ").color(ColorConfig.TEXT),
                Message.raw(allowOthers).color(ColorConfig.HIGHLIGHT)));
        forceRefresh(player);
    }

    /**
     * Opens the interactive armor visibility GUI for the player.
     * <p>
     * Creates and displays a {@link HideArmorGuiPage} with
     * dismissible lifetime.
     *
     * @param player the player to show the GUI to
     */
    private void openGui(Player player) {
        var ref = player.getReference();
        if (ref != null && ref.isValid()) {
            var store = ref.getStore();
            var world = store.getExternalData().getWorld();

            world.execute(() -> {
                var playerRefComponent = store.getComponent(ref,
                        com.hypixel.hytale.server.core.universe.PlayerRef.getComponentType());

                if (playerRefComponent != null) {
                    player.getPageManager().openCustomPage(ref, store,
                            new HideArmorGuiPage(playerRefComponent,
                                    CustomPageLifetime.CanDismiss));
                }
            });
        }
    }

    /**
     * Forces an equipment refresh for the player to apply visual changes
     * immediately.
     * <p>
     * Executes on the world thread to avoid concurrent modification issues.
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
