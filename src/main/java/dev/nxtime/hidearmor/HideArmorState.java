package dev.nxtime.hidearmor;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Centralized state management for armor visibility settings.
 * <p>
 * Uses a 12-bit bitmask system to track three categories of settings per
 * player:
 * <ul>
 * <li>Bits 0-3: Self armor visibility (which of your own armor pieces are
 * hidden from you)</li>
 * <li>Bits 4-7: Hide others' armor (which armor pieces you want to hide on
 * other players)</li>
 * <li>Bits 8-11: Allow others (which armor pieces others are allowed to hide on
 * you)</li>
 * </ul>
 * <p>
 * Thread-safe implementation using {@link ConcurrentHashMap} for concurrent
 * access.
 * Changes trigger a callback for persistence (debounced save).
 * <p>
 * The mutual opt-in system requires both conditions to be met for armor to be
 * hidden:
 * <ol>
 * <li>Viewer must have "hide others" enabled for that slot</li>
 * <li>Target player must have "allow others" enabled for that slot</li>
 * </ol>
 *
 * @author nxtime
 * @version 0.4.0
 */
public final class HideArmorState {

    // Self armor slots (bits 0-3)
    public static final int SLOT_HEAD = 0;
    public static final int SLOT_CHEST = 1;
    public static final int SLOT_HANDS = 2;
    public static final int SLOT_LEGS = 3;

    // Hide other players' armor slots (bits 4-7)
    public static final int SLOT_HIDE_OTHERS_HEAD = 4;
    public static final int SLOT_HIDE_OTHERS_CHEST = 5;
    public static final int SLOT_HIDE_OTHERS_HANDS = 6;
    public static final int SLOT_HIDE_OTHERS_LEGS = 7;

    // Allow others to hide my armor (bits 8-11)
    public static final int SLOT_ALLOW_OTHERS_HEAD = 8;
    public static final int SLOT_ALLOW_OTHERS_CHEST = 9;
    public static final int SLOT_ALLOW_OTHERS_HANDS = 10;
    public static final int SLOT_ALLOW_OTHERS_LEGS = 11;

    private static final ConcurrentHashMap<UUID, Integer> MASKS = new ConcurrentHashMap<>();
    private static volatile Runnable onChange;
    private static volatile int defaultMask = 0;

    /**
     * Private constructor to prevent instantiation.
     * This is a utility class with static methods only.
     */
    private HideArmorState() {
    }

    /**
     * Sets the callback to be invoked when any player's state (or global config)
     * changes.
     * <p>
     * This callback is typically used to trigger debounced disk persistence.
     * The callback runs on the same thread that modified the state.
     *
     * @param callback the runnable to execute on state changes, or null to clear
     */
    public static void setOnChange(Runnable callback) {
        onChange = callback;
    }

    /**
     * Retrieves the global default mask applied to users with no explicit settings.
     *
     * @return the default 12-bit bitmask
     */
    public static int getDefaultMask() {
        return defaultMask;
    }

    /**
     * Sets the global default mask and triggers persistence.
     *
     * @param mask the new default 12-bit bitmask
     */
    public static void setDefaultMask(int mask) {
        int clamped = Math.max(0, Math.min(4095, mask));
        if (defaultMask != clamped) {
            defaultMask = clamped;
            Runnable callback = onChange;
            if (callback != null)
                callback.run();
        }
    }

    /**
     * Retrieves the full 12-bit mask for a player.
     * <p>
     * Returns the player's explicit setting if valid, otherwise returns the global
     * default mask.
     *
     * @param uuid the player's UUID
     * @return the bitmask (0-4095)
     */
    public static int getMask(UUID uuid) {
        Integer mask = MASKS.get(uuid);
        return mask == null ? defaultMask : mask;
    }

    /**
     * Sets the full 12-bit mask for a player and triggers the onChange callback.
     *
     * @param uuid the player's UUID
     * @param mask the new bitmask value (0-4095)
     */
    public static void setMask(UUID uuid, int mask) {
        setMaskInternal(uuid, mask, true);
    }

    /**
     * Sets the full 12-bit mask for a player without triggering the onChange
     * callback.
     * <p>
     * Used during initial state loading from disk to avoid triggering saves.
     *
     * @param uuid the player's UUID
     * @param mask the new bitmask value (0-4095)
     */
    public static void setMaskSilently(UUID uuid, int mask) {
        setMaskInternal(uuid, mask, false);
    }

    /**
     * Checks if a specific bit/slot is set in the player's mask.
     * <p>
     * Can be used for any of the 12 bits (self armor, hide others, allow others).
     *
     * @param uuid the player's UUID
     * @param slot the bit position to check (0-11)
     * @return true if the bit is set (1), false otherwise
     */
    public static boolean isHidden(UUID uuid, int slot) {
        int mask = getMask(uuid);
        return (mask & (1 << slot)) != 0;
    }

    /**
     * Toggles a specific bit/slot in the player's mask.
     * <p>
     * Flips the bit: 0 becomes 1, 1 becomes 0.
     *
     * @param uuid the player's UUID
     * @param slot the bit position to toggle (0-11)
     * @return the new mask value after toggling
     */
    public static int toggleSlot(UUID uuid, int slot) {
        int mask = getMask(uuid);
        int newMask = mask ^ (1 << slot);
        setMask(uuid, newMask);
        return newMask;
    }

    /**
     * Sets or clears all self-armor bits (0-3) while preserving other bits.
     * <p>
     * Used for the "Hide All Armor" toggle that only affects your own armor.
     *
     * @param uuid the player's UUID
     * @param hide true to hide all self-armor, false to show all
     * @return the new mask value after modification
     */
    public static int setAll(UUID uuid, boolean hide) {
        int mask = getMask(uuid);
        // Only modify self-armor bits (0-3), preserve other bits (4-11)
        int newMask = hide ? (mask | 0xF) : (mask & ~0xF);
        setMask(uuid, newMask);
        return newMask;
    }

    /**
     * Creates an immutable snapshot of all current player states.
     * <p>
     * Used for persistence to disk. Returns a copy to avoid concurrent modification
     * issues.
     *
     * @return a map of UUID to mask values
     */
    public static Map<UUID, Integer> snapshot() {
        return new HashMap<>(MASKS);
    }

    /**
     * Formats the self-armor bits (0-3) into a human-readable string.
     * <p>
     * Example output: "Head, Chest" or "none" if no bits are set.
     *
     * @param mask the full bitmask value
     * @return comma-separated list of hidden armor pieces, or "none"
     */
    public static String formatMask(int mask) {
        if (mask == 0)
            return "none";

        StringBuilder out = new StringBuilder();
        appendIfSet(out, mask, SLOT_HEAD, "Head");
        appendIfSet(out, mask, SLOT_CHEST, "Chest");
        appendIfSet(out, mask, SLOT_HANDS, "Hands");
        appendIfSet(out, mask, SLOT_LEGS, "Legs");
        return out.toString();
    }

    /**
     * Internal method to set a player's mask with optional notification.
     * <p>
     * Clamps the value to valid range (0-4095) and removes entry if mask is 0.
     *
     * @param uuid   the player's UUID
     * @param mask   the new bitmask value
     * @param notify whether to trigger the onChange callback
     */
    private static void setMaskInternal(UUID uuid, int mask, boolean notify) {
        int clamped = Math.max(0, Math.min(4095, mask)); // 12 bits: 2^12 - 1
        if (clamped == 0) {
            MASKS.remove(uuid);
        } else {
            MASKS.put(uuid, clamped);
        }

        if (notify) {
            Runnable callback = onChange;
            if (callback != null)
                callback.run();
        }
    }

    /**
     * Helper method to append a slot label to a string builder if the bit is set.
     *
     * @param out   the StringBuilder to append to
     * @param mask  the bitmask to check
     * @param slot  the bit position to check
     * @param label the label to append if bit is set
     */
    private static void appendIfSet(StringBuilder out, int mask, int slot, String label) {
        if ((mask & (1 << slot)) == 0)
            return;
        if (out.length() > 0)
            out.append(", ");
        out.append(label);
    }

    // === Hide Others Functionality ===

    /**
     * Check if viewer wants to hide this armor slot on other players
     * 
     * @param viewerUuid UUID of the player viewing
     * @param armorSlot  The base armor slot (0-3)
     * @return true if viewer has enabled hiding this slot on others
     */
    public static boolean isHideOthersEnabled(UUID viewerUuid, int armorSlot) {
        if (armorSlot < 0 || armorSlot > 3)
            return false;
        int hideOthersSlot = SLOT_HIDE_OTHERS_HEAD + armorSlot;
        return isHidden(viewerUuid, hideOthersSlot);
    }

    /**
     * Check if target player allows others to hide this armor slot
     * 
     * @param targetUuid UUID of the player whose armor might be hidden
     * @param armorSlot  The base armor slot (0-3)
     * @return true if target allows this slot to be hidden by others
     */
    public static boolean isAllowOthersEnabled(UUID targetUuid, int armorSlot) {
        if (armorSlot < 0 || armorSlot > 3)
            return false;
        int allowOthersSlot = SLOT_ALLOW_OTHERS_HEAD + armorSlot;
        return isHidden(targetUuid, allowOthersSlot);
    }

    /**
     * Check both conditions for hiding another player's armor (mutual opt-in)
     * 
     * @param viewerUuid UUID of the player viewing
     * @param targetUuid UUID of the player whose armor might be hidden
     * @param armorSlot  The base armor slot (0-3)
     * @return true if BOTH viewer wants to hide AND target allows hiding
     */
    public static boolean shouldHideOtherPlayerArmor(UUID viewerUuid, UUID targetUuid, int armorSlot) {
        return isHideOthersEnabled(viewerUuid, armorSlot) &&
                isAllowOthersEnabled(targetUuid, armorSlot);
    }

    /**
     * Toggle hide-others setting for a specific armor slot
     * 
     * @param uuid      Player UUID
     * @param armorSlot The base armor slot (0-3)
     * @return new mask value
     */
    public static int toggleHideOthers(UUID uuid, int armorSlot) {
        if (armorSlot < 0 || armorSlot > 3)
            return getMask(uuid);
        int hideOthersSlot = SLOT_HIDE_OTHERS_HEAD + armorSlot;
        return toggleSlot(uuid, hideOthersSlot);
    }

    /**
     * Toggle allow-others setting for a specific armor slot
     * 
     * @param uuid      Player UUID
     * @param armorSlot The base armor slot (0-3)
     * @return new mask value
     */
    public static int toggleAllowOthers(UUID uuid, int armorSlot) {
        if (armorSlot < 0 || armorSlot > 3)
            return getMask(uuid);
        int allowOthersSlot = SLOT_ALLOW_OTHERS_HEAD + armorSlot;
        return toggleSlot(uuid, allowOthersSlot);
    }

    /**
     * Set all hide-others slots at once
     * 
     * @param uuid Player UUID
     * @param hide true to hide all others' armor, false to show
     * @return new mask value
     */
    public static int setAllHideOthers(UUID uuid, boolean hide) {
        int mask = getMask(uuid);
        int hideOthersBits = hide ? 0xF0 : 0; // Bits 4-7
        int newMask = hide ? (mask | hideOthersBits) : (mask & ~0xF0);
        setMask(uuid, newMask);
        return newMask;
    }

    /**
     * Set all allow-others slots at once
     * 
     * @param uuid  Player UUID
     * @param allow true to allow all, false to disallow
     * @return new mask value
     */
    public static int setAllAllowOthers(UUID uuid, boolean allow) {
        int mask = getMask(uuid);
        int allowOthersBits = allow ? 0xF00 : 0; // Bits 8-11
        int newMask = allow ? (mask | allowOthersBits) : (mask & ~0xF00);
        setMask(uuid, newMask);
        return newMask;
    }
}
