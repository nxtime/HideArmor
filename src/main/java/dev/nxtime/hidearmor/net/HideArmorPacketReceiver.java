package dev.nxtime.hidearmor.net;

import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.ComponentUpdate;
import com.hypixel.hytale.protocol.ComponentUpdateType;
import com.hypixel.hytale.protocol.EntityUpdate;
import com.hypixel.hytale.protocol.Equipment;
import com.hypixel.hytale.protocol.packets.entities.EntityUpdates;
import com.hypixel.hytale.server.core.receiver.IPacketReceiver;

import dev.nxtime.hidearmor.HideArmorState;

import javax.annotation.Nonnull;
import java.util.UUID;

/**
 * Intercepts outgoing packets sent to the client (viewer).
 * Only modifies EntityUpdates → Equipment packets for the player's own entity
 * (selfNetworkId).
 * This allows hiding armor pieces client-side without affecting the server
 * inventory or other players' views.
 */
public final class HideArmorPacketReceiver implements IPacketReceiver {

    private final IPacketReceiver delegate;
    private final UUID viewerUuid;
    private final int selfNetworkId;

    public HideArmorPacketReceiver(IPacketReceiver delegate, UUID viewerUuid, int selfNetworkId) {
        this.delegate = delegate;
        this.viewerUuid = viewerUuid;
        this.selfNetworkId = selfNetworkId;
    }

    @Override
    public void write(@Nonnull Packet packet) {
        delegate.write(maybeModify(packet));
    }

    @Override
    public void writeNoCache(@Nonnull Packet packet) {
        delegate.writeNoCache(maybeModify(packet));
    }

    private Packet maybeModify(Packet packet) {
        int mask = HideArmorState.getMask(viewerUuid);

        // Early return: If no armor is hidden for this player, pass packet through
        // unchanged
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

            // Only modify packets for the player's own entity (not other players or mobs)
            if (upd.networkId != selfNetworkId)
                continue;

            if (upd.updates == null || upd.updates.length == 0)
                continue;

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

                    // Check if any armor slots are marked as hidden
                    boolean shouldHide = false;
                    int[] slots = new int[] {
                            HideArmorState.SLOT_HEAD,
                            HideArmorState.SLOT_CHEST,
                            HideArmorState.SLOT_HANDS,
                            HideArmorState.SLOT_LEGS
                    };

                    for (int slot : slots) {
                        if (!HideArmorState.isHidden(viewerUuid, slot))
                            continue;
                        if (slot >= 0 && slot < armorIds.length) {
                            shouldHide = true;
                            break;
                        }
                    }

                    if (!shouldHide)
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
                        if (!HideArmorState.isHidden(viewerUuid, slot))
                            continue;
                        if (slot >= 0 && slot < eqCopy.armorIds.length) {
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
}
