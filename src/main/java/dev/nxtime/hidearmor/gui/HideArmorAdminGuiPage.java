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
 * Interactive GUI page for managing DEFAULT armor visibility settings (Admin).
 *
 * @author nxtime
 * @version 0.6.0
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
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.ValueChanged, "#HideOthersHelmetSetting #CheckBox",
                EventData.of("Button", "HideOthersHelmet"), false);
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.ValueChanged, "#HideOthersChestSetting #CheckBox",
                EventData.of("Button", "HideOthersChest"), false);
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.ValueChanged, "#HideOthersGauntletsSetting #CheckBox",
                EventData.of("Button", "HideOthersGauntlets"), false);
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.ValueChanged, "#HideOthersLeggingsSetting #CheckBox",
                EventData.of("Button", "HideOthersLeggings"), false);

        // Allow others
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.ValueChanged, "#AllowOthersHelmetSetting #CheckBox",
                EventData.of("Button", "AllowOthersHelmet"), false);
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.ValueChanged, "#AllowOthersChestSetting #CheckBox",
                EventData.of("Button", "AllowOthersChest"), false);
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.ValueChanged, "#AllowOthersGauntletsSetting #CheckBox",
                EventData.of("Button", "AllowOthersGauntlets"), false);
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.ValueChanged, "#AllowOthersLeggingsSetting #CheckBox",
                EventData.of("Button", "AllowOthersLeggings"), false);
    }

    @Override
    public void handleDataEvent(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store,
            @Nonnull AdminGuiData data) {
        super.handleDataEvent(ref, store, data);

        if (data.button != null) {
            int currentDefault = HideArmorState.getDefaultMask();
            int slotToToggle = -1;

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
            }

            if (slotToToggle != -1) {
                int newMask = currentDefault ^ (1 << slotToToggle);
                HideArmorState.setDefaultMask(newMask);
            }
        }

        // Update the UI to reflect new state
        this.sendUpdate();
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
