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
 * Uses Hytale's custom UI system with checkboxes.
 */
public class HideArmorGuiPage extends InteractiveCustomUIPage<HideArmorGuiPage.ArmorGuiData> {

    public HideArmorGuiPage(@Nonnull PlayerRef playerRef, @Nonnull CustomPageLifetime lifetime) {
        super(playerRef, lifetime, ArmorGuiData.CODEC);
    }

    @Override
    public void build(@Nonnull Ref<EntityStore> ref, @Nonnull UICommandBuilder uiCommandBuilder,
            @Nonnull UIEventBuilder uiEventBuilder, @Nonnull Store<EntityStore> store) {
        // Load the UI file
        uiCommandBuilder.append("Pages/dev.nxtime_HideArmor_Menu.ui");

        // Get current armor state and set checkbox values
        var player = store.getComponent(ref, com.hypixel.hytale.server.core.entity.entities.Player.getComponentType());
        int mask = HideArmorState.getMask(player.getUuid());

        boolean helmetHidden = (mask & (1 << HideArmorState.SLOT_HEAD)) != 0;
        boolean chestHidden = (mask & (1 << HideArmorState.SLOT_CHEST)) != 0;
        boolean gauntletsHidden = (mask & (1 << HideArmorState.SLOT_HANDS)) != 0;
        boolean leggingsHidden = (mask & (1 << HideArmorState.SLOT_LEGS)) != 0;
        boolean allHidden = mask == 15;

        uiCommandBuilder.set("#HelmetSetting #CheckBox.Value", helmetHidden);
        uiCommandBuilder.set("#ChestSetting #CheckBox.Value", chestHidden);
        uiCommandBuilder.set("#GauntletsSetting #CheckBox.Value", gauntletsHidden);
        uiCommandBuilder.set("#LeggingsSetting #CheckBox.Value", leggingsHidden);
        uiCommandBuilder.set("#AllArmorSetting #CheckBox.Value", allHidden);

        // Register event handlers for checkbox changes (matching AdminUI's pattern)
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
    }

    @Override
    public void handleDataEvent(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store,
            @Nonnull ArmorGuiData data) {
        super.handleDataEvent(ref, store, data);

        var player = store.getComponent(ref, com.hypixel.hytale.server.core.entity.entities.Player.getComponentType());

        if (data.button != null) {
            switch (data.button) {
                case "Helmet" -> HideArmorState.toggleSlot(player.getUuid(), HideArmorState.SLOT_HEAD);
                case "Chest" -> HideArmorState.toggleSlot(player.getUuid(), HideArmorState.SLOT_CHEST);
                case "Gauntlets" -> HideArmorState.toggleSlot(player.getUuid(), HideArmorState.SLOT_HANDS);
                case "Leggings" -> HideArmorState.toggleSlot(player.getUuid(), HideArmorState.SLOT_LEGS);
                case "All" -> {
                    int currentMask = HideArmorState.getMask(player.getUuid());
                    boolean hideAll = currentMask != 15;
                    HideArmorState.setAll(player.getUuid(), hideAll);
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

    public static class ArmorGuiData {
        static final String KEY_BUTTON = "Button";

        public static final BuilderCodec<ArmorGuiData> CODEC = BuilderCodec
                .<ArmorGuiData>builder(ArmorGuiData.class, ArmorGuiData::new)
                .addField(new KeyedCodec<>(KEY_BUTTON, Codec.STRING),
                        (data, s) -> data.button = s,
                        data -> data.button)
                .build();

        private String button;
    }
}
