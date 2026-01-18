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

import dev.nxtime.hidearmor.HideArmorPlugin;
import dev.nxtime.hidearmor.util.PermissionUtils;
import dev.nxtime.hidearmor.util.ColorConfig;
import dev.nxtime.hidearmor.util.PluginLogger;
import dev.nxtime.hidearmor.util.TranslationManager;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.Message;

import javax.annotation.Nonnull;

/**
 * Interactive GUI page for managing DEFAULT armor visibility settings (Admin).
 *
 * @author nxtime
 * @version 0.7.0
 */
public class HideArmorAdminGuiPage extends InteractiveCustomUIPage<HideArmorAdminGuiPage.AdminGuiData> {

        public HideArmorAdminGuiPage(@Nonnull PlayerRef playerRef, @Nonnull CustomPageLifetime lifetime) {
                super(playerRef, lifetime, AdminGuiData.CODEC);
        }

        @Override
        public void build(@Nonnull Ref<EntityStore> ref, @Nonnull UICommandBuilder uiCommandBuilder,
                        @Nonnull UIEventBuilder uiEventBuilder, @Nonnull Store<EntityStore> store) {
                // Load the Admin UI file
                uiCommandBuilder.append("Pages/dev.nxtime_HideArmor_AdminMenu.ui");

                // Get player for translations
                Player player = store.getComponent(ref, Player.getComponentType());

                // Set translated text for all UI elements
                if (player != null) {
                        // Title
                        uiCommandBuilder.set("#MainTitle.Text", TranslationManager.get(player, "admin.title"));

                        // Description
                        uiCommandBuilder.set("#DescriptionLabel.Text",
                                        TranslationManager.get(player, "admin.description"));

                        // Section headers
                        uiCommandBuilder.set("#SectionDefaultSelf.Text",
                                        TranslationManager.get(player, "admin.section.default_self"));
                        uiCommandBuilder.set("#SectionDefaultHideOthers.Text",
                                        TranslationManager.get(player, "admin.section.default_hide_others"));
                        uiCommandBuilder.set("#SectionDefaultAllowOthers.Text",
                                        TranslationManager.get(player, "admin.section.default_allow_others"));
                        uiCommandBuilder.set("#SectionForced.Text",
                                        TranslationManager.get(player, "admin.section.forced"));
                        uiCommandBuilder.set("#ForceWarningLabel.Text",
                                        TranslationManager.get(player, "admin.force_warning"));
                        uiCommandBuilder.set("#SectionQuickSetup.Text",
                                        TranslationManager.get(player, "admin.section.quick_setup"));
                        uiCommandBuilder.set("#SectionLanguage.Text",
                                        TranslationManager.get(player, "admin.section.language"));

                        // Labels
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

                        uiCommandBuilder.set("#ForceHelmetSetting #Name.Text",
                                        TranslationManager.get(player, "admin.label.force_helmet"));
                        uiCommandBuilder.set("#ForceChestSetting #Name.Text",
                                        TranslationManager.get(player, "admin.label.force_chest"));
                        uiCommandBuilder.set("#ForceGauntletsSetting #Name.Text",
                                        TranslationManager.get(player, "admin.label.force_hands"));
                        uiCommandBuilder.set("#ForceLeggingsSetting #Name.Text",
                                        TranslationManager.get(player, "admin.label.force_legs"));

                        uiCommandBuilder.set("#PermissionLabel.Text",
                                        TranslationManager.get(player, "admin.permission_label"));
                        uiCommandBuilder.set("#SetupPermissionsBtn.Text",
                                        TranslationManager.get(player, "admin.setup_permissions"));
                        uiCommandBuilder.set("#LanguageLabel.Text",
                                        TranslationManager.get(player, "admin.language_label"));
                }

                // Use DEFAULT mask instead of player mask
                int mask = HideArmorState.getDefaultMask();

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

                // Forced settings (bits 0-3 re-used)
                int forced = HideArmorState.getForcedMask();
                uiCommandBuilder.set("#ForceHelmetSetting #CheckBox.Value",
                                (forced & (1 << HideArmorState.SLOT_HEAD)) != 0);
                uiCommandBuilder.set("#ForceChestSetting #CheckBox.Value",
                                (forced & (1 << HideArmorState.SLOT_CHEST)) != 0);
                uiCommandBuilder.set("#ForceGauntletsSetting #CheckBox.Value",
                                (forced & (1 << HideArmorState.SLOT_HANDS)) != 0);
                uiCommandBuilder.set("#ForceLeggingsSetting #CheckBox.Value",
                                (forced & (1 << HideArmorState.SLOT_LEGS)) != 0);

                // Register event handlers - reused from player page since IDs are same
                registerBindings(uiEventBuilder);
        }

        private void registerBindings(UIEventBuilder uiEventBuilder) {
                // Self armor
                uiEventBuilder.addEventBinding(CustomUIEventBindingType.ValueChanged, "#HelmetSetting #CheckBox",
                                EventData.of("Button", "Helmet"), false);
                uiEventBuilder.addEventBinding(CustomUIEventBindingType.ValueChanged, "#ChestSetting #CheckBox",
                                EventData.of("Button", "Chest"), false);
                uiEventBuilder.addEventBinding(CustomUIEventBindingType.ValueChanged, "#GauntletsSetting #CheckBox",
                                EventData.of("Button", "Gauntlets"), false);
                uiEventBuilder.addEventBinding(CustomUIEventBindingType.ValueChanged, "#LeggingsSetting #CheckBox",
                                EventData.of("Button", "Leggings"), false);

                // Hide others
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

                // Allow others
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

                // Forced settings
                uiEventBuilder.addEventBinding(CustomUIEventBindingType.ValueChanged, "#ForceHelmetSetting #CheckBox",
                                EventData.of("Button", "ForceHelmet"), false);
                uiEventBuilder.addEventBinding(CustomUIEventBindingType.ValueChanged, "#ForceChestSetting #CheckBox",
                                EventData.of("Button", "ForceChest"), false);
                uiEventBuilder.addEventBinding(CustomUIEventBindingType.ValueChanged,
                                "#ForceGauntletsSetting #CheckBox",
                                EventData.of("Button", "ForceGauntlets"), false);
                uiEventBuilder.addEventBinding(CustomUIEventBindingType.ValueChanged, "#ForceLeggingsSetting #CheckBox",
                                EventData.of("Button", "ForceLeggings"), false);

                // Setup Permissions button
                uiEventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#SetupPermissionsBtn",
                                EventData.of("Button", "SetupPermissions"), false);

                // Language buttons
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
                        @Nonnull AdminGuiData data) {
                super.handleDataEvent(ref, store, data);

                if (data.button != null) {
                        int currentDefault = HideArmorState.getDefaultMask();
                        int currentForced = HideArmorState.getForcedMask();
                        int slotToToggle = -1;
                        boolean isForced = false;
                        String langToSet = null;

                        switch (data.button) {
                                // Self armor
                                case "Helmet" -> slotToToggle = HideArmorState.SLOT_HEAD;
                                case "Chest" -> slotToToggle = HideArmorState.SLOT_CHEST;
                                case "Gauntlets" -> slotToToggle = HideArmorState.SLOT_HANDS;
                                case "Leggings" -> slotToToggle = HideArmorState.SLOT_LEGS;

                                // Hide others
                                case "HideOthersHelmet" -> slotToToggle = HideArmorState.SLOT_HIDE_OTHERS_HEAD;
                                case "HideOthersChest" -> slotToToggle = HideArmorState.SLOT_HIDE_OTHERS_CHEST;
                                case "HideOthersGauntlets" -> slotToToggle = HideArmorState.SLOT_HIDE_OTHERS_HANDS;
                                case "HideOthersLeggings" -> slotToToggle = HideArmorState.SLOT_HIDE_OTHERS_LEGS;

                                // Allow others
                                case "AllowOthersHelmet" -> slotToToggle = HideArmorState.SLOT_ALLOW_OTHERS_HEAD;
                                case "AllowOthersChest" -> slotToToggle = HideArmorState.SLOT_ALLOW_OTHERS_CHEST;
                                case "AllowOthersGauntlets" -> slotToToggle = HideArmorState.SLOT_ALLOW_OTHERS_HANDS;
                                case "AllowOthersLeggings" -> slotToToggle = HideArmorState.SLOT_ALLOW_OTHERS_LEGS;

                                // Forced settings
                                case "ForceHelmet" -> {
                                        slotToToggle = HideArmorState.SLOT_HEAD;
                                        isForced = true;
                                }
                                case "ForceChest" -> {
                                        slotToToggle = HideArmorState.SLOT_CHEST;
                                        isForced = true;
                                }
                                case "ForceGauntlets" -> {
                                        slotToToggle = HideArmorState.SLOT_HANDS;
                                        isForced = true;
                                }
                                case "ForceLeggings" -> {
                                        slotToToggle = HideArmorState.SLOT_LEGS;
                                        isForced = true;
                                }
                                case "SetupPermissions" -> {
                                        // Handle permission setup button click
                                        handleSetupPermissions(store, ref);
                                        return; // Don't process as armor toggle
                                }
                                // Language buttons
                                case "LangEn" -> langToSet = "en_us";
                                case "LangEs" -> langToSet = "es_es";
                                case "LangPt" -> langToSet = "pt_br";
                                case "LangFr" -> langToSet = "fr_fr";
                                case "LangDe" -> langToSet = "de_de";
                                case "LangRu" -> langToSet = "ru_ru";
                        }

                        // Handle language change
                        if (langToSet != null) {
                                handleLanguageChange(store, ref, langToSet);
                                return;
                        }

                        if (slotToToggle != -1) {
                                if (isForced) {
                                        int newMask = currentForced ^ (1 << slotToToggle);
                                        HideArmorState.setForcedMask(newMask);
                                        // Immediately refresh all players' equipment when force settings change
                                        var plugin = HideArmorPlugin.getInstance();
                                        if (plugin != null) {
                                                plugin.refreshAllPlayersEquipment();
                                        }
                                } else {
                                        int newMask = currentDefault ^ (1 << slotToToggle);
                                        HideArmorState.setDefaultMask(newMask);
                                }
                        }
                }

                // Update the UI to reflect new state
                this.sendUpdate();
        }

        /**
         * Handles language change button click.
         */
        private void handleLanguageChange(Store<EntityStore> store, Ref<EntityStore> ref, String langCode) {
                HideArmorState.setDefaultLanguage(langCode);

                var player = store.getComponent(ref, Player.getComponentType());
                if (player != null) {
                        player.sendMessage(Message.join(
                                        Message.raw(ColorConfig.BRAND).color(ColorConfig.PREFIX_COLOR),
                                        Message.raw(TranslationManager.get(player, "admin.default_language_set",
                                                        langCode.toUpperCase()))
                                                        .color(ColorConfig.SUCCESS)));
                }

                // Refresh the UI to show new translations
                this.sendUpdate();
        }

        /**
         * Handles the Setup Permissions button click.
         * Attempts to add HideArmor permissions to the server's permissions.json.
         */
        private void handleSetupPermissions(Store<EntityStore> store, Ref<EntityStore> ref) {
                var plugin = HideArmorPlugin.getInstance();
                if (plugin == null) {
                        PluginLogger
                                        .error("Plugin instance not available for permission setup");
                        return;
                }

                var dataDir = plugin.getDataDirectory();
                var result = PermissionUtils.setupPermissions(dataDir);

                // Send feedback to player
                var player = store.getComponent(ref,
                                Player.getComponentType());
                if (player != null) {
                        var color = result.success
                                        ? ColorConfig.SUCCESS
                                        : ColorConfig.ERROR;
                        player.sendMessage(Message.join(
                                        Message
                                                        .raw(ColorConfig.BRAND)
                                                        .color(ColorConfig.PREFIX_COLOR),
                                        Message.raw(result.message).color(color)));
                }
        }

        /**
         * Data model for GUI events.
         */
        public static class AdminGuiData {
                static final String KEY_BUTTON = "Button";

                public static final BuilderCodec<AdminGuiData> CODEC = BuilderCodec
                                .<AdminGuiData>builder(AdminGuiData.class, AdminGuiData::new)
                                .addField(new KeyedCodec<>(KEY_BUTTON, Codec.STRING),
                                                (data, s) -> data.button = s,
                                                data -> data.button)
                                .build();

                private String button;
        }
}
