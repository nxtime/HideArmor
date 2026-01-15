package dev.nxtime.hidearmor.net;

import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.ComponentUpdate;
import com.hypixel.hytale.protocol.ComponentUpdateType;
import com.hypixel.hytale.protocol.EntityUpdate;
import com.hypixel.hytale.protocol.Equipment;
import com.hypixel.hytale.protocol.packets.entities.EntityUpdates;
import com.hypixel.hytale.server.core.receiver.IPacketReceiver;

import dev.nxtime.hidearmor.HideArmorState;
// import dev.nxtime.hidearmor.commands.HideArmorTestCommand; // Uncomment for test mode

import javax.annotation.Nonnull;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Intercepts outgoing packets sent to the client to hide armor pieces visually.
 * <p>
 * This packet receiver wraps the default packet receiver for a player (viewer) and
 * modifies {@code EntityUpdates} packets before they reach the client. It processes
 * equipment updates for:
 * <ul>
 *   <li><b>Self armor:</b> Hides the viewer's own armor based on their self-armor settings</li>
 *   <li><b>Other players' armor:</b> Hides armor on other players using mutual opt-in logic</li>
 * </ul>
 * <p>
 * The mutual opt-in system requires both:
 * <ol>
 *   <li>Viewer has "hide others" enabled for that armor slot</li>
 *   <li>Target player has "allow others" enabled for that armor slot</li>
 * </ol>
 * <p>
 * This is purely a visual modification. Server-side inventory, stats, durability,
 * and combat calculations are completely unaffected.
 * <p>
 * <b>Performance:</b> Uses caching to minimize entity UUID lookups. Early exits
 * when no settings are configured for the viewer.
 * <p>
 * <b>Thread-safety:</b> Safe for concurrent packet processing. Uses {@link ConcurrentHashMap}
 * for entity UUID caching.
 *
 * @author nxtime
 * @version 0.4.0
 * @see HideArmorState
 */
public final class HideArmorPacketReceiver implements IPacketReceiver {

    /** The underlying packet receiver that actually sends packets to the client. */
    private final IPacketReceiver delegate;

    /** The UUID of the player viewing (receiving) these packets. */
    private final UUID viewerUuid;

    /** The network ID of the viewer's own entity. */
    private final int selfNetworkId;

    /** The world instance for entity lookups. Stored as Object for SDK compatibility. */
    private final Object world;

    /**
     * Cache mapping network IDs to player UUIDs.
     * Populated on-demand to avoid repeated entity store queries.
     */
    private final ConcurrentHashMap<Integer, UUID> networkIdCache = new ConcurrentHashMap<>();

    /**
     * Creates a new packet receiver wrapper for a specific player.
     *
     * @param delegate the original packet receiver to wrap
     * @param viewerUuid the UUID of the player who will receive these packets
     * @param selfNetworkId the network ID of the viewer's own entity
     * @param world the world instance for entity lookups (uses Object type for SDK compatibility)
     */
    public HideArmorPacketReceiver(IPacketReceiver delegate, UUID viewerUuid, int selfNetworkId, Object world) {
        this.delegate = delegate;
        this.viewerUuid = viewerUuid;
        this.selfNetworkId = selfNetworkId;
        this.world = world;
    }

    /**
     * Writes a packet to the client, potentially modifying equipment data first.
     * <p>
     * If the packet is an {@code EntityUpdates} packet containing equipment information,
     * armor IDs may be replaced with empty strings based on visibility settings.
     *
     * @param packet the packet to send
     */
    @Override
    public void write(@Nonnull Packet packet) {
        delegate.write(maybeModify(packet));
    }

    /**
     * Writes a packet to the client without caching, potentially modifying equipment data first.
     * <p>
     * Behaves identically to {@link #write(Packet)} but uses the no-cache write path.
     *
     * @param packet the packet to send
     */
    @Override
    public void writeNoCache(@Nonnull Packet packet) {
        delegate.writeNoCache(maybeModify(packet));
    }

    /**
     * Potentially modifies a packet to hide armor pieces based on visibility settings.
     * <p>
     * This method performs several optimizations:
     * <ul>
     *   <li>Early exit if viewer has no settings configured (mask == 0)</li>
     *   <li>Only processes {@code EntityUpdates} packets</li>
     *   <li>Only modifies equipment components</li>
     *   <li>Uses lazy copying to avoid cloning unchanged packets</li>
     * </ul>
     * <p>
     * The actual hiding logic depends on whether the entity is the viewer's own:
     * <ul>
     *   <li><b>Self:</b> Uses self-armor settings (bits 0-3)</li>
     *   <li><b>Others:</b> Uses mutual opt-in (hide-others bits AND allow-others bits)</li>
     * </ul>
     *
     * @param packet the original packet from the server
     * @return the modified packet with hidden armor, or the original if no modifications needed
     */
    private Packet maybeModify(Packet packet) {
        int mask = HideArmorState.getMask(viewerUuid);

        // Early return: If no settings enabled for this viewer, pass packet through unchanged
        if (mask == 0)
            return packet;

        // Only process EntityUpdates packets (which contain equipment data)
        if (!(packet instanceof EntityUpdates eu))
            return packet;

        if (eu.updates == null || eu.updates.length == 0)
            return packet;

        // Track whether we've modified anything to avoid unnecessary cloning
        boolean modified = false;
        EntityUpdate[] updatesCopy = null;

        for (int i = 0; i < eu.updates.length; i++) {
            EntityUpdate upd = eu.updates[i];
            if (upd == null)
                continue;

            if (upd.updates == null || upd.updates.length == 0)
                continue;

            // Determine which UUID this entity belongs to
            UUID targetUuid;
            // Test mode disabled - uncomment to enable single-player testing
            // boolean isTestMode = HideArmorTestCommand.isTestModeEnabled(viewerUuid);
            boolean isTestMode = false;

            if (upd.networkId == selfNetworkId) {
                // This is the viewer's own entity
                targetUuid = viewerUuid;
            } else {
                targetUuid = resolveEntityUuid(upd.networkId); // This is another player's entity
                if (targetUuid == null)
                    continue; // Not a player entity or couldn't resolve
            }

            EntityUpdate updCopy = null;

            for (int j = 0; j < upd.updates.length; j++) {
                ComponentUpdate cu = upd.updates[j];
                if (cu == null)
                    continue;

                // Check if this component update contains equipment (armor) data
                if (cu.type == ComponentUpdateType.Equipment && cu.equipment != null) {
                    String[] armorIds = cu.equipment.armorIds;
                    if (armorIds == null || armorIds.length == 0)
                        continue;

                    // Check which armor slots should be hidden
                    boolean[] hideSlots = new boolean[4];
                    boolean shouldHideAny = false;

                    int[] slots = new int[] {
                            HideArmorState.SLOT_HEAD,
                            HideArmorState.SLOT_CHEST,
                            HideArmorState.SLOT_HANDS,
                            HideArmorState.SLOT_LEGS
                    };

                    for (int slot : slots) {
                        if (slot < 0 || slot >= armorIds.length)
                            continue;

                        boolean shouldHide;
                        if (targetUuid.equals(viewerUuid) && !isTestMode) {
                            // Self armor (normal mode): check self-armor settings
                            shouldHide = HideArmorState.isHidden(viewerUuid, slot);
                        } else if (targetUuid.equals(viewerUuid) && isTestMode) {
                            // Self armor (test mode): apply mutual opt-in logic to own armor
                            shouldHide = HideArmorState.shouldHideOtherPlayerArmor(viewerUuid, viewerUuid, slot);
                        } else {
                            // Other player: check mutual opt-in (viewer wants to hide AND target allows)
                            shouldHide = HideArmorState.shouldHideOtherPlayerArmor(viewerUuid, targetUuid, slot);
                        }

                        hideSlots[slot] = shouldHide;
                        if (shouldHide)
                            shouldHideAny = true;
                    }

                    if (!shouldHideAny)
                        continue;

                    // Lazy copying: Only clone the array when we actually need to modify it
                    if (!modified) {
                        updatesCopy = eu.updates.clone(); // Shallow copy of the updates array
                        modified = true;
                    }

                    // Clone the EntityUpdate only once to avoid mutating the original
                    if (updCopy == null) {
                        updCopy = new EntityUpdate();
                        updCopy.networkId = upd.networkId;
                        updCopy.removed = upd.removed; // Preserve removal state
                        updCopy.updates = upd.updates.clone(); // Shallow copy of component updates
                        updatesCopy[i] = updCopy;
                    }

                    // Clone the ComponentUpdate to modify equipment safely
                    ComponentUpdate cuCopy = new ComponentUpdate();
                    cuCopy.type = cu.type;

                    // Deep clone the Equipment object to preserve hand items while modifying armor
                    Equipment eqCopy = new Equipment();
                    eqCopy.rightHandItemId = cu.equipment.rightHandItemId;
                    eqCopy.leftHandItemId = cu.equipment.leftHandItemId;

                    // Clone armor IDs and replace hidden slots with empty strings
                    eqCopy.armorIds = armorIds.clone();
                    for (int slot : slots) {
                        if (hideSlots[slot] && slot >= 0 && slot < eqCopy.armorIds.length) {
                            eqCopy.armorIds[slot] = ""; // Empty string hides the armor piece visually
                        }
                    }

                    // Reassemble the modified component update
                    cuCopy.equipment = eqCopy;
                    updCopy.updates[j] = cuCopy;
                }
            }
        }

        // If nothing was modified, return the original packet unchanged
        if (!modified)
            return packet;

        // Build and return the modified packet with hidden armor
        EntityUpdates out = new EntityUpdates();
        out.removed = eu.removed; // Preserve entity removal state
        out.updates = updatesCopy; // Use our modified copy with hidden armor
        return out;
    }

    /**
     * Resolve a networkId to a player UUID.
     * Uses caching to avoid repeated entity store queries.
     *
     * @param networkId The network ID to resolve
     * @return Player UUID if found, null otherwise
     */
    private UUID resolveEntityUuid(int networkId) {
        // Check cache first
        UUID cached = networkIdCache.get(networkId);
        if (cached != null)
            return cached;

        // Query world using reflection to avoid type dependency
        try {
            var worldClass = world.getClass();
            var getPlayersMethod = worldClass.getMethod("getPlayers");
            var players = (Iterable<?>) getPlayersMethod.invoke(world);

            for (Object playerObj : players) {
                var playerClass = playerObj.getClass();
                var getNetworkIdMethod = playerClass.getMethod("getNetworkId");
                var getUuidMethod = playerClass.getMethod("getUuid");

                int playerId = (int) getNetworkIdMethod.invoke(playerObj);
                if (playerId == networkId) {
                    UUID uuid = (UUID) getUuidMethod.invoke(playerObj);
                    networkIdCache.put(networkId, uuid); // Cache the result
                    return uuid;
                }
            }
        } catch (Throwable t) {
            // Silently fail to avoid breaking packet processing
        }

        return null; // Not a player entity or couldn't resolve
    }
}
