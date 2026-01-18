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
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.Message;

import dev.nxtime.hidearmor.util.TranslationManager;
import dev.nxtime.hidearmor.util.ColorConfig;
import javax.annotation.Nonnull;

/**
 * Interactive GUI page for managing armor visibility settings.
 * Uses Hytale Settings-style UI with static layout.
 *
 * @author nxtime
 * @version 0.6.0
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
                                Player.getComponentType());

                // Set translated text
                if (player != null) {
                        uiCommandBuilder.set("#MainTitle.Text", TranslationManager.get(player, "ui.title"));

                        uiCommandBuilder.set("#SectionSelf.Text", TranslationManager.get(player, "ui.section.self"));
                        uiCommandBuilder.set("#SectionHideOthers.Text",
                                        TranslationManager.get(player, "ui.section.hide_others"));
                        uiCommandBuilder.set("#SectionAllowOthers.Text",
                                        TranslationManager.get(player, "ui.section.allow_others"));
                        uiCommandBuilder.set("#SectionLanguage.Text",
                                        TranslationManager.get(player, "ui.section.language"));
                        uiCommandBuilder.set("#LanguageLabel.Text",
                                        TranslationManager.get(player, "ui.label.language"));

                        uiCommandBuilder.set("#HelmetSetting #Name.Text",
                                        TranslationManager.get(player, "ui.label.hide_helmet"));
                        uiCommandBuilder.set("#ChestSetting #Name.Text",
                                        TranslationManager.get(player, "ui.label.hide_chest"));
                        uiCommandBuilder.set("#GauntletsSetting #Name.Text",
                                        TranslationManager.get(player, "ui.label.hide_hands"));
                        uiCommandBuilder.set("#LeggingsSetting #Name.Text",
                                        TranslationManager.get(player, "ui.label.hide_legs"));

                        uiCommandBuilder.set("#HideOthersHelmetSetting #Name.Text",
                                        TranslationManager.get(player, "ui.label.hide_others_helmet"));
                        uiCommandBuilder.set("#HideOthersChestSetting #Name.Text",
                                        TranslationManager.get(player, "ui.label.hide_others_chest"));
                        uiCommandBuilder.set("#HideOthersGauntletsSetting #Name.Text",
                                        TranslationManager.get(player, "ui.label.hide_others_hands"));
                        uiCommandBuilder.set("#HideOthersLeggingsSetting #Name.Text",
                                        TranslationManager.get(player, "ui.label.hide_others_legs"));

                        uiCommandBuilder.set("#AllowOthersHelmetSetting #Name.Text",
                                        TranslationManager.get(player, "ui.label.allow_helmet"));
                        uiCommandBuilder.set("#AllowOthersChestSetting #Name.Text",
                                        TranslationManager.get(player, "ui.label.allow_chest"));
                        uiCommandBuilder.set("#AllowOthersGauntletsSetting #Name.Text",
                                        TranslationManager.get(player, "ui.label.allow_hands"));
                        uiCommandBuilder.set("#AllowOthersLeggingsSetting #Name.Text",
                                        TranslationManager.get(player, "ui.label.allow_legs"));
                }

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

                // Register event handlers - Language buttons
                uiEventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#LangEnBtn",
                                EventData.of("Button", "LangEn"), false);
                uiEventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#LangEsBtn",
                                EventData.of("Button", "LangEs"), false);
                uiEventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#LangPtBtn",
                                EventData.of("Button", "LangPt"), false);
                uiEventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#LangFrBtn",
                                EventData.of("Button", "LangFr"), false);
                uiEventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#LangDeBtn",
                                EventData.of("Button", "LangDe"), false);
                uiEventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#LangRuBtn",
                                EventData.of("Button", "LangRu"), false);
        }

        @Override
        public void handleDataEvent(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store,
                        @Nonnull ArmorGuiData data) {
                super.handleDataEvent(ref, store, data);

                var player = store.getComponent(ref,
                                Player.getComponentType());

                if (data.button != null && player != null) {
                        // Check for language buttons first
                        String langToSet = null;
                        switch (data.button) {
                                case "LangEn" -> langToSet = "en_us";
                                case "LangEs" -> langToSet = "es_es";
                                case "LangPt" -> langToSet = "pt_br";
                                case "LangFr" -> langToSet = "fr_fr";
                                case "LangDe" -> langToSet = "de_de";
                                case "LangRu" -> langToSet = "ru_ru";
                        }

                        if (langToSet != null) {
                                HideArmorState.setLanguage(player.getUuid(), langToSet);
                                player.sendMessage(Message.join(
                                                Message.raw(ColorConfig.BRAND).color(ColorConfig.PREFIX_COLOR),
                                                Message.raw(TranslationManager.get(player, "command.language_set",
                                                                langToSet.toUpperCase()))
                                                                .color(ColorConfig.SUCCESS)));
                                // Update the UI to reflect new language
                                this.sendUpdate();
                                return;
                        }

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
