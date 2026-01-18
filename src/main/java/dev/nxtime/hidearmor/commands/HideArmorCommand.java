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
import dev.nxtime.hidearmor.util.TranslationManager;
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
            openGui(player);
            return;
        }

        String first = args[0].toLowerCase();

        // Language command
        if ("language".equals(first) || "lang".equals(first)) {
            if (args.length < 2) {
                player.sendMessage(
                        Message.raw(TranslationManager.get(player, "command.usage.header")).color(ColorConfig.ERROR));
                return;
            }
            String lang = args[1].toLowerCase();
            HideArmorState.setLanguage(player.getUuid(), lang);
            player.sendMessage(Message.join(
                    Message.raw(ColorConfig.BRAND).color(ColorConfig.PREFIX_COLOR),
                    Message.raw(TranslationManager.get(player, "command.language_set", lang.toUpperCase()))
                            .color(ColorConfig.SUCCESS)));
            return;
        }

        if ("status".equals(first)) {
            sendStatus(player);
            return;
        }

        if ("hideothers".equals(first)) {
            handleHideOthers(player, args);
            return;
        }

        if ("allowothers".equals(first)) {
            handleAllowOthers(player, args);
            return;
        }

        if ("all".equals(first)) {
            int current = HideArmorState.getMask(player.getUuid());
            int newMask = HideArmorState.setAll(player.getUuid(), (current & 0xF) != 0xF);
            player.sendMessage(Message.join(
                    Message.raw(ColorConfig.BRAND).color(ColorConfig.PREFIX_COLOR),
                    Message.raw(formatMask(player, newMask)).color(ColorConfig.TEXT)));
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
                        Message.raw(formatMask(player, newMask)).color(ColorConfig.TEXT)));
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
                    Message.raw(formatMask(player, newMask)).color(ColorConfig.TEXT)));
            forceRefresh(player);
            return;
        }

        int slot = resolveSlot(first);
        if (slot >= 0) {
            int newMask = HideArmorState.toggleSlot(player.getUuid(), slot);
            player.sendMessage(Message.join(
                    Message.raw(ColorConfig.BRAND).color(ColorConfig.PREFIX_COLOR),
                    Message.raw(formatMask(player, newMask)).color(ColorConfig.TEXT)));
            forceRefresh(player);
            return;
        }

        sendHelp(player);
    }

    private void sendHelp(Player player) {
        player.sendMessage(Message.join(
                Message.raw(ColorConfig.BRAND).color(ColorConfig.PREFIX_COLOR),
                Message.raw(TranslationManager.get(player, "command.usage.header")).color(ColorConfig.HIGHLIGHT)));
        player.sendMessage(Message.join(
                Message.raw("  /hidearmor ").color(ColorConfig.TEXT),
                Message.raw(TranslationManager.get(player, "command.usage.gui")).color(ColorConfig.HIGHLIGHT)));
        player.sendMessage(Message.join(
                Message.raw("  /hidearmor status ").color(ColorConfig.TEXT),
                Message.raw(TranslationManager.get(player, "command.usage.status")).color(ColorConfig.HIGHLIGHT)));
        player.sendMessage(Message.join(
                Message.raw("  /hidearmor hideothers [all|head|chest|hands|legs] ").color(ColorConfig.TEXT),
                Message.raw(TranslationManager.get(player, "command.usage.hideothers")).color(ColorConfig.HIGHLIGHT)));
        player.sendMessage(Message.join(
                Message.raw("  /hidearmor allowothers [all|head|chest|hands|legs] ").color(ColorConfig.TEXT),
                Message.raw(TranslationManager.get(player, "command.usage.allowothers")).color(ColorConfig.HIGHLIGHT)));
    }

    private void sendStatus(Player player) {
        int mask = HideArmorState.getMask(player.getUuid());

        String selfArmor = formatSectionMask(player, mask, 0, 3);
        String hideOthers = formatSectionMask(player, mask, 4, 7);
        String allowOthers = formatSectionMask(player, mask, 8, 11);

        player.sendMessage(
                Message.raw(TranslationManager.get(player, "status.header")).color(ColorConfig.PREFIX_COLOR));
        player.sendMessage(Message.join(
                Message.raw(TranslationManager.get(player, "status.hide_my_armor")).color(ColorConfig.HIGHLIGHT),
                Message.raw(selfArmor).color(ColorConfig.TEXT)));
        player.sendMessage(Message.join(
                Message.raw(TranslationManager.get(player, "status.hide_others_armor")).color(ColorConfig.HIGHLIGHT),
                Message.raw(hideOthers).color(ColorConfig.TEXT)));
        player.sendMessage(Message.join(
                Message.raw(TranslationManager.get(player, "status.allow_others")).color(ColorConfig.HIGHLIGHT),
                Message.raw(allowOthers).color(ColorConfig.TEXT)));
    }

    private String formatMask(Player player, int mask) {
        if (mask == 0)
            return TranslationManager.get(player, "status.none");
        // For simplicity, we only show self armor in simple toggle feedback unless we
        // want to be more verbose
        // But the previous implementation showed everything set in self armor slots
        // only for simple toggles.
        // Let's stick to showing active self armor for consistency with previous
        // behavior if possible,
        // OR format based on what changed.
        // The previous simple `HideArmorState.formatMask` only showed self armor (bits
        // 0-3).
        return formatSectionMask(player, mask, 0, 3);
    }

    private String formatSectionMask(Player player, int mask, int startBit, int endBit) {
        StringBuilder out = new StringBuilder();
        String[] keys = { "armor.head", "armor.chest", "armor.hands", "armor.legs" };

        for (int i = startBit; i <= endBit; i++) {
            if ((mask & (1 << i)) != 0) {
                if (out.length() > 0)
                    out.append(", ");
                out.append(TranslationManager.get(player, keys[i - startBit]));
            }
        }

        return out.length() > 0 ? out.toString() : TranslationManager.get(player, "status.none");
    }

    private int resolveSlot(String arg) {
        return switch (arg) {
            case "head" -> HideArmorState.SLOT_HEAD;
            case "chest" -> HideArmorState.SLOT_CHEST;
            case "hands" -> HideArmorState.SLOT_HANDS;
            case "legs" -> HideArmorState.SLOT_LEGS;
            default -> -1;
        };
    }

    private void handleHideOthers(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(
                    Message.raw(TranslationManager.get(player, "error.usage.hideothers")).color(ColorConfig.ERROR));
            return;
        }

        String target = args[1].toLowerCase();
        if ("all".equals(target)) {
            int current = HideArmorState.getMask(player.getUuid());
            boolean hideAll = (current & 0xF0) != 0xF0;
            HideArmorState.setAllHideOthers(player.getUuid(), hideAll);
            player.sendMessage(Message.join(
                    Message.raw(ColorConfig.BRAND).color(ColorConfig.PREFIX_COLOR),
                    Message.raw(TranslationManager.get(player, "status.hide_others_armor")).color(ColorConfig.TEXT),
                    Message.raw(hideAll ? TranslationManager.get(player, "common.all")
                            : TranslationManager.get(player, "common.none"))
                            .color(hideAll ? ColorConfig.ERROR : ColorConfig.SUCCESS)));
            forceRefresh(player);
            return;
        }

        int slot = resolveSlot(target);
        if (slot < 0) {
            player.sendMessage(
                    Message.raw(TranslationManager.get(player, "error.invalid_piece")).color(ColorConfig.ERROR));
            return;
        }

        HideArmorState.toggleHideOthers(player.getUuid(), slot);
        int mask = HideArmorState.getMask(player.getUuid());
        String hideOthers = formatSectionMask(player, mask, 4, 7);
        player.sendMessage(Message.join(
                Message.raw(ColorConfig.BRAND).color(ColorConfig.PREFIX_COLOR),
                Message.raw(TranslationManager.get(player, "status.hide_others_armor")).color(ColorConfig.TEXT),
                Message.raw(hideOthers).color(ColorConfig.HIGHLIGHT)));
        forceRefresh(player);
    }

    private void handleAllowOthers(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(
                    Message.raw(TranslationManager.get(player, "error.usage.allowothers")).color(ColorConfig.ERROR));
            return;
        }

        String target = args[1].toLowerCase();
        if ("all".equals(target)) {
            int current = HideArmorState.getMask(player.getUuid());
            boolean allowAll = (current & 0xF00) != 0xF00;
            HideArmorState.setAllAllowOthers(player.getUuid(), allowAll);
            player.sendMessage(Message.join(
                    Message.raw(ColorConfig.BRAND).color(ColorConfig.PREFIX_COLOR),
                    Message.raw(TranslationManager.get(player, "status.allow_others")).color(ColorConfig.TEXT),
                    Message.raw(allowAll ? TranslationManager.get(player, "common.all")
                            : TranslationManager.get(player, "common.none"))
                            .color(allowAll ? ColorConfig.SUCCESS : ColorConfig.ERROR)));
            forceRefresh(player);
            return;
        }

        int slot = resolveSlot(target);
        if (slot < 0) {
            player.sendMessage(
                    Message.raw(TranslationManager.get(player, "error.invalid_piece")).color(ColorConfig.ERROR));
            return;
        }

        HideArmorState.toggleAllowOthers(player.getUuid(), slot);
        int mask = HideArmorState.getMask(player.getUuid());
        String allowOthers = formatSectionMask(player, mask, 8, 11);
        player.sendMessage(Message.join(
                Message.raw(ColorConfig.BRAND).color(ColorConfig.PREFIX_COLOR),
                Message.raw(TranslationManager.get(player, "status.allow_others")).color(ColorConfig.TEXT),
                Message.raw(allowOthers).color(ColorConfig.HIGHLIGHT)));
        forceRefresh(player);
    }

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
