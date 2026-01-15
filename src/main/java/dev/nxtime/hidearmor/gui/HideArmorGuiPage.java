package dev.nxtime.hidearmor.gui;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.nxtime.hidearmor.HideArmorState;

import javax.annotation.Nonnull;

/**
 * Interactive GUI page for managing armor visibility settings.
 * <p>
 * This page displays three sections of checkbox controls:
 * <ul>
 *   <li><b>Hide My Own Armor:</b> Control self-armor visibility (5 checkboxes)</li>
 *   <li><b>Hide Other Players' Armor:</b> Choose which pieces to hide on others (5 checkboxes)</li>
 *   <li><b>Let Others Hide My Armor:</b> Grant per-slot permissions (5 checkboxes)</li>
 * </ul>
 * <p>
 * The GUI loads from {@code dev.nxtime_HideArmor_Menu.ui} and uses Hytale's native
 * {@link InteractiveCustomUIPage} system with event bindings for real-time updates.
 * <p>
 * Changes are applied immediately when checkboxes are toggled, triggering both
 * state updates and equipment refreshes.
 *
 * @author nxtime
 * @version 0.4.0
 * @see HideArmorState
 */
public class HideArmorGuiPage extends InteractiveCustomUIPage<HideArmorGuiPage.ArmorGuiData> {

    /**
     * Creates a new GUI page instance for a player.
     *
     * @param playerRef reference to the player opening the GUI
     * @param lifetime the page lifetime (typically {@link CustomPageLifetime#CanDismiss})
     */
    public HideArmorGuiPage(@Nonnull PlayerRef playerRef, @Nonnull CustomPageLifetime lifetime) {
        super(playerRef, lifetime, ArmorGuiData.CODEC);
    }

    /**
     * Builds and initializes the GUI with current state.
     * <p>
     * Loads the UI file, reads the player's current mask, sets checkbox values
     * for all 15 checkboxes (5 per section), and registers event handlers for
     * value changed events.
     *
     * @param ref entity reference for the player
     * @param uiCommandBuilder builder for setting UI element values
     * @param uiEventBuilder builder for registering event handlers
     * @param store entity store for component lookups
     */
    @Override
    public void build(@Nonnull Ref<EntityStore> ref, @Nonnull UICommandBuilder uiCommandBuilder,
            @Nonnull UIEventBuilder uiEventBuilder, @Nonnull Store<EntityStore> store) {
        // Load the UI file
        uiCommandBuilder.append("Pages/dev.nxtime_HideArmor_Menu.ui");

        // Get current armor state and set checkbox values
        var player = store.getComponent(ref, com.hypixel.hytale.server.core.entity.entities.Player.getComponentType());
        int mask = HideArmorState.getMask(player.getUuid());

        // Self armor (bits 0-3)
        boolean helmetHidden = (mask & (1 << HideArmorState.SLOT_HEAD)) != 0;
        boolean chestHidden = (mask & (1 << HideArmorState.SLOT_CHEST)) != 0;
        boolean gauntletsHidden = (mask & (1 << HideArmorState.SLOT_HANDS)) != 0;
        boolean leggingsHidden = (mask & (1 << HideArmorState.SLOT_LEGS)) != 0;
        boolean allHidden = (mask & 0xF) == 0xF;

        // Hide others' armor (bits 4-7)
        boolean hideOthersHelmet = (mask & (1 << HideArmorState.SLOT_HIDE_OTHERS_HEAD)) != 0;
        boolean hideOthersChest = (mask & (1 << HideArmorState.SLOT_HIDE_OTHERS_CHEST)) != 0;
        boolean hideOthersGauntlets = (mask & (1 << HideArmorState.SLOT_HIDE_OTHERS_HANDS)) != 0;
        boolean hideOthersLeggings = (mask & (1 << HideArmorState.SLOT_HIDE_OTHERS_LEGS)) != 0;
        boolean hideOthersAll = (mask & 0xF0) == 0xF0;

        // Allow others to hide my armor (bits 8-11)
        boolean allowOthersHelmet = (mask & (1 << HideArmorState.SLOT_ALLOW_OTHERS_HEAD)) != 0;
        boolean allowOthersChest = (mask & (1 << HideArmorState.SLOT_ALLOW_OTHERS_CHEST)) != 0;
        boolean allowOthersGauntlets = (mask & (1 << HideArmorState.SLOT_ALLOW_OTHERS_HANDS)) != 0;
        boolean allowOthersLeggings = (mask & (1 << HideArmorState.SLOT_ALLOW_OTHERS_LEGS)) != 0;
        boolean allowOthersAll = (mask & 0xF00) == 0xF00;

        // Set self armor checkbox values
        uiCommandBuilder.set("#HelmetSetting #CheckBox.Value", helmetHidden);
        uiCommandBuilder.set("#ChestSetting #CheckBox.Value", chestHidden);
        uiCommandBuilder.set("#GauntletsSetting #CheckBox.Value", gauntletsHidden);
        uiCommandBuilder.set("#LeggingsSetting #CheckBox.Value", leggingsHidden);
        uiCommandBuilder.set("#AllArmorSetting #CheckBox.Value", allHidden);

        // Set hide-others checkbox values
        uiCommandBuilder.set("#HideOthersHelmetSetting #CheckBox.Value", hideOthersHelmet);
        uiCommandBuilder.set("#HideOthersChestSetting #CheckBox.Value", hideOthersChest);
        uiCommandBuilder.set("#HideOthersGauntletsSetting #CheckBox.Value", hideOthersGauntlets);
        uiCommandBuilder.set("#HideOthersLeggingsSetting #CheckBox.Value", hideOthersLeggings);
        uiCommandBuilder.set("#HideOthersAllSetting #CheckBox.Value", hideOthersAll);

        // Set allow-others checkbox values
        uiCommandBuilder.set("#AllowOthersHelmetSetting #CheckBox.Value", allowOthersHelmet);
        uiCommandBuilder.set("#AllowOthersChestSetting #CheckBox.Value", allowOthersChest);
        uiCommandBuilder.set("#AllowOthersGauntletsSetting #CheckBox.Value", allowOthersGauntlets);
        uiCommandBuilder.set("#AllowOthersLeggingsSetting #CheckBox.Value", allowOthersLeggings);
        uiCommandBuilder.set("#AllowOthersAllSetting #CheckBox.Value", allowOthersAll);

        // Register event handlers for self armor checkboxes
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.ValueChanged, "#HelmetSetting #CheckBox",
                EventData.of("Button", "Helmet"), false);
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.ValueChanged, "#ChestSetting #CheckBox",
                EventData.of("Button", "Chest"), false);
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.ValueChanged, "#GauntletsSetting #CheckBox",
                EventData.of("Button", "Gauntlets"), false);
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.ValueChanged, "#LeggingsSetting #CheckBox",
                EventData.of("Button", "Leggings"), false);
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.ValueChanged, "#AllArmorSetting #CheckBox",
                EventData.of("Button", "All"), false);

        // Register event handlers for hide-others checkboxes
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.ValueChanged, "#HideOthersHelmetSetting #CheckBox",
                EventData.of("Button", "HideOthersHelmet"), false);
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.ValueChanged, "#HideOthersChestSetting #CheckBox",
                EventData.of("Button", "HideOthersChest"), false);
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.ValueChanged, "#HideOthersGauntletsSetting #CheckBox",
                EventData.of("Button", "HideOthersGauntlets"), false);
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.ValueChanged, "#HideOthersLeggingsSetting #CheckBox",
                EventData.of("Button", "HideOthersLeggings"), false);
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.ValueChanged, "#HideOthersAllSetting #CheckBox",
                EventData.of("Button", "HideOthersAll"), false);

        // Register event handlers for allow-others checkboxes
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.ValueChanged, "#AllowOthersHelmetSetting #CheckBox",
                EventData.of("Button", "AllowOthersHelmet"), false);
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.ValueChanged, "#AllowOthersChestSetting #CheckBox",
                EventData.of("Button", "AllowOthersChest"), false);
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.ValueChanged, "#AllowOthersGauntletsSetting #CheckBox",
                EventData.of("Button", "AllowOthersGauntlets"), false);
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.ValueChanged, "#AllowOthersLeggingsSetting #CheckBox",
                EventData.of("Button", "AllowOthersLeggings"), false);
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.ValueChanged, "#AllowOthersAllSetting #CheckBox",
                EventData.of("Button", "AllowOthersAll"), false);
    }

    /**
     * Handles checkbox toggle events from the GUI.
     * <p>
     * Routes the event to the appropriate {@link HideArmorState} method based on
     * the button ID, then triggers an equipment refresh and UI update.
     * <p>
     * Handles 15 different button IDs:
     * <ul>
     *   <li>Self armor: Helmet, Chest, Gauntlets, Leggings, All</li>
     *   <li>Hide others: HideOthersHelmet, HideOthersChest, etc., HideOthersAll</li>
     *   <li>Allow others: AllowOthersHelmet, AllowOthersChest, etc., AllowOthersAll</li>
     * </ul>
     *
     * @param ref entity reference for the player
     * @param store entity store for component lookups
     * @param data the event data containing the button ID
     */
    @Override
    public void handleDataEvent(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store,
            @Nonnull ArmorGuiData data) {
        super.handleDataEvent(ref, store, data);

        var player = store.getComponent(ref, com.hypixel.hytale.server.core.entity.entities.Player.getComponentType());

        if (data.button != null) {
            switch (data.button) {
                // Self armor toggles
                case "Helmet" -> HideArmorState.toggleSlot(player.getUuid(), HideArmorState.SLOT_HEAD);
                case "Chest" -> HideArmorState.toggleSlot(player.getUuid(), HideArmorState.SLOT_CHEST);
                case "Gauntlets" -> HideArmorState.toggleSlot(player.getUuid(), HideArmorState.SLOT_HANDS);
                case "Leggings" -> HideArmorState.toggleSlot(player.getUuid(), HideArmorState.SLOT_LEGS);
                case "All" -> {
                    int currentMask = HideArmorState.getMask(player.getUuid());
                    boolean hideAll = (currentMask & 0xF) != 0xF;
                    HideArmorState.setAll(player.getUuid(), hideAll);
                }

                // Hide others' armor toggles
                case "HideOthersHelmet" -> HideArmorState.toggleHideOthers(player.getUuid(), HideArmorState.SLOT_HEAD);
                case "HideOthersChest" -> HideArmorState.toggleHideOthers(player.getUuid(), HideArmorState.SLOT_CHEST);
                case "HideOthersGauntlets" -> HideArmorState.toggleHideOthers(player.getUuid(), HideArmorState.SLOT_HANDS);
                case "HideOthersLeggings" -> HideArmorState.toggleHideOthers(player.getUuid(), HideArmorState.SLOT_LEGS);
                case "HideOthersAll" -> {
                    int currentMask = HideArmorState.getMask(player.getUuid());
                    boolean hideAll = (currentMask & 0xF0) != 0xF0;
                    HideArmorState.setAllHideOthers(player.getUuid(), hideAll);
                }

                // Allow others to hide my armor toggles
                case "AllowOthersHelmet" -> HideArmorState.toggleAllowOthers(player.getUuid(), HideArmorState.SLOT_HEAD);
                case "AllowOthersChest" -> HideArmorState.toggleAllowOthers(player.getUuid(), HideArmorState.SLOT_CHEST);
                case "AllowOthersGauntlets" -> HideArmorState.toggleAllowOthers(player.getUuid(), HideArmorState.SLOT_HANDS);
                case "AllowOthersLeggings" -> HideArmorState.toggleAllowOthers(player.getUuid(), HideArmorState.SLOT_LEGS);
                case "AllowOthersAll" -> {
                    int currentMask = HideArmorState.getMask(player.getUuid());
                    boolean allowAll = (currentMask & 0xF00) != 0xF00;
                    HideArmorState.setAllAllowOthers(player.getUuid(), allowAll);
                }
            }

            // Force equipment refresh to apply changes
            var world = player.getWorld();
            if (world != null) {
                world.execute(() -> {
                    try {
                        player.invalidateEquipmentNetwork();
                    } catch (Throwable ignored) {
                    }
                });
            }
        }

        // Update the UI to reflect new state
        this.sendUpdate();
    }

    /**
     * Data model for GUI events.
     * <p>
     * Encodes and decodes the button ID from checkbox value changed events.
     * Uses Hytale's codec system for serialization.
     */
    public static class ArmorGuiData {
        /** Key name for button field in event data. */
        static final String KEY_BUTTON = "Button";

        /** Codec for serializing/deserializing event data. */
        public static final BuilderCodec<ArmorGuiData> CODEC = BuilderCodec
                .<ArmorGuiData>builder(ArmorGuiData.class, ArmorGuiData::new)
                .addField(new KeyedCodec<>(KEY_BUTTON, Codec.STRING),
                        (data, s) -> data.button = s,
                        data -> data.button)
                .build();

        /** The ID of the button/checkbox that triggered the event. */
        private String button;
    }
}
