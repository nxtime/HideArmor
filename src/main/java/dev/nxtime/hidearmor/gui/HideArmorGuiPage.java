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
 * Uses Hytale Settings-style UI with static layout.
 *
 * @author nxtime
 * @version 0.5.0
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

                var player = store.getComponent(ref,
                                com.hypixel.hytale.server.core.entity.entities.Player.getComponentType());
                int mask = HideArmorState.getMask(player.getUuid());

                // Self armor (bits 0-3)
                uiCommandBuilder.set("#HelmetSetting #CheckBox.Value",
                                (mask & (1 << HideArmorState.SLOT_HEAD)) != 0);
                uiCommandBuilder.set("#ChestSetting #CheckBox.Value",
                                (mask & (1 << HideArmorState.SLOT_CHEST)) != 0);
                uiCommandBuilder.set("#GauntletsSetting #CheckBox.Value",
                                (mask & (1 << HideArmorState.SLOT_HANDS)) != 0);
                uiCommandBuilder.set("#LeggingsSetting #CheckBox.Value",
                                (mask & (1 << HideArmorState.SLOT_LEGS)) != 0);

                // Hide others (bits 4-7)
                uiCommandBuilder.set("#HideOthersHelmetSetting #CheckBox.Value",
                                (mask & (1 << HideArmorState.SLOT_HIDE_OTHERS_HEAD)) != 0);
                uiCommandBuilder.set("#HideOthersChestSetting #CheckBox.Value",
                                (mask & (1 << HideArmorState.SLOT_HIDE_OTHERS_CHEST)) != 0);
                uiCommandBuilder.set("#HideOthersGauntletsSetting #CheckBox.Value",
                                (mask & (1 << HideArmorState.SLOT_HIDE_OTHERS_HANDS)) != 0);
                uiCommandBuilder.set("#HideOthersLeggingsSetting #CheckBox.Value",
                                (mask & (1 << HideArmorState.SLOT_HIDE_OTHERS_LEGS)) != 0);

                // Allow others (bits 8-11)
                uiCommandBuilder.set("#AllowOthersHelmetSetting #CheckBox.Value",
                                (mask & (1 << HideArmorState.SLOT_ALLOW_OTHERS_HEAD)) != 0);
                uiCommandBuilder.set("#AllowOthersChestSetting #CheckBox.Value",
                                (mask & (1 << HideArmorState.SLOT_ALLOW_OTHERS_CHEST)) != 0);
                uiCommandBuilder.set("#AllowOthersGauntletsSetting #CheckBox.Value",
                                (mask & (1 << HideArmorState.SLOT_ALLOW_OTHERS_HANDS)) != 0);
                uiCommandBuilder.set("#AllowOthersLeggingsSetting #CheckBox.Value",
                                (mask & (1 << HideArmorState.SLOT_ALLOW_OTHERS_LEGS)) != 0);

                // Register event handlers - Self armor
                uiEventBuilder.addEventBinding(CustomUIEventBindingType.ValueChanged, "#HelmetSetting #CheckBox",
                                EventData.of("Button", "Helmet"), false);
                uiEventBuilder.addEventBinding(CustomUIEventBindingType.ValueChanged, "#ChestSetting #CheckBox",
                                EventData.of("Button", "Chest"), false);
                uiEventBuilder.addEventBinding(CustomUIEventBindingType.ValueChanged, "#GauntletsSetting #CheckBox",
                                EventData.of("Button", "Gauntlets"), false);
                uiEventBuilder.addEventBinding(CustomUIEventBindingType.ValueChanged, "#LeggingsSetting #CheckBox",
                                EventData.of("Button", "Leggings"), false);

                // Register event handlers - Hide others
                uiEventBuilder.addEventBinding(CustomUIEventBindingType.ValueChanged,
                                "#HideOthersHelmetSetting #CheckBox",
                                EventData.of("Button", "HideOthersHelmet"), false);
                uiEventBuilder.addEventBinding(CustomUIEventBindingType.ValueChanged,
                                "#HideOthersChestSetting #CheckBox",
                                EventData.of("Button", "HideOthersChest"), false);
                uiEventBuilder.addEventBinding(CustomUIEventBindingType.ValueChanged,
                                "#HideOthersGauntletsSetting #CheckBox",
                                EventData.of("Button", "HideOthersGauntlets"), false);
                uiEventBuilder.addEventBinding(CustomUIEventBindingType.ValueChanged,
                                "#HideOthersLeggingsSetting #CheckBox",
                                EventData.of("Button", "HideOthersLeggings"), false);

                // Register event handlers - Allow others
                uiEventBuilder.addEventBinding(CustomUIEventBindingType.ValueChanged,
                                "#AllowOthersHelmetSetting #CheckBox",
                                EventData.of("Button", "AllowOthersHelmet"), false);
                uiEventBuilder.addEventBinding(CustomUIEventBindingType.ValueChanged,
                                "#AllowOthersChestSetting #CheckBox",
                                EventData.of("Button", "AllowOthersChest"), false);
                uiEventBuilder.addEventBinding(CustomUIEventBindingType.ValueChanged,
                                "#AllowOthersGauntletsSetting #CheckBox",
                                EventData.of("Button", "AllowOthersGauntlets"), false);
                uiEventBuilder.addEventBinding(CustomUIEventBindingType.ValueChanged,
                                "#AllowOthersLeggingsSetting #CheckBox",
                                EventData.of("Button", "AllowOthersLeggings"), false);
        }

        @Override
        public void handleDataEvent(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store,
                        @Nonnull ArmorGuiData data) {
                super.handleDataEvent(ref, store, data);

                var player = store.getComponent(ref,
                                com.hypixel.hytale.server.core.entity.entities.Player.getComponentType());

                if (data.button != null) {
                        switch (data.button) {
                                // Self armor toggles
                                case "Helmet" -> HideArmorState.toggleSlot(player.getUuid(), HideArmorState.SLOT_HEAD);
                                case "Chest" -> HideArmorState.toggleSlot(player.getUuid(), HideArmorState.SLOT_CHEST);
                                case "Gauntlets" ->
                                        HideArmorState.toggleSlot(player.getUuid(), HideArmorState.SLOT_HANDS);
                                case "Leggings" ->
                                        HideArmorState.toggleSlot(player.getUuid(), HideArmorState.SLOT_LEGS);

                                // Hide others' armor toggles
                                case "HideOthersHelmet" ->
                                        HideArmorState.toggleHideOthers(player.getUuid(), HideArmorState.SLOT_HEAD);
                                case "HideOthersChest" ->
                                        HideArmorState.toggleHideOthers(player.getUuid(), HideArmorState.SLOT_CHEST);
                                case "HideOthersGauntlets" ->
                                        HideArmorState.toggleHideOthers(player.getUuid(), HideArmorState.SLOT_HANDS);
                                case "HideOthersLeggings" ->
                                        HideArmorState.toggleHideOthers(player.getUuid(), HideArmorState.SLOT_LEGS);

                                // Allow others to hide my armor toggles
                                case "AllowOthersHelmet" ->
                                        HideArmorState.toggleAllowOthers(player.getUuid(), HideArmorState.SLOT_HEAD);
                                case "AllowOthersChest" ->
                                        HideArmorState.toggleAllowOthers(player.getUuid(), HideArmorState.SLOT_CHEST);
                                case "AllowOthersGauntlets" ->
                                        HideArmorState.toggleAllowOthers(player.getUuid(), HideArmorState.SLOT_HANDS);
                                case "AllowOthersLeggings" ->
                                        HideArmorState.toggleAllowOthers(player.getUuid(), HideArmorState.SLOT_LEGS);
                        }

                        // Force equipment refresh
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
         */
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
