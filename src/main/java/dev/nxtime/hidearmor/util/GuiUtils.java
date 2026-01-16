package dev.nxtime.hidearmor.util;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;
import java.util.function.BiFunction;

/**
 * Utility class for GUI-related operations.
 * <p>
 * Provides helper methods for opening custom UI pages with proper
 * world-thread execution and component retrieval.
 *
 * @author nxtime
 * @version 0.7.0
 */
public final class GuiUtils {

    private GuiUtils() {
        // Utility class - prevent instantiation
    }

    /**
     * Opens a custom UI page for a player with standard boilerplate handling.
     * <p>
     * This method handles:
     * <ul>
     * <li>Null checks for player and world</li>
     * <li>World-threaded execution</li>
     * <li>PlayerRef component retrieval</li>
     * <li>Page manager invocation</li>
     * </ul>
     *
     * @param player      the player to show the UI to
     * @param pageFactory factory function that creates the page given PlayerRef and
     *                    lifetime
     * @param lifetime    the page lifetime (CanDismiss, etc.)
     * @return true if the page opening was scheduled, false if prerequisites failed
     */
    public static boolean openPage(
            @Nonnull Player player,
            @Nonnull BiFunction<PlayerRef, CustomPageLifetime, ? extends InteractiveCustomUIPage<?>> pageFactory,
            @Nonnull CustomPageLifetime lifetime) {

        var ref = player.getReference();
        if (ref == null || !ref.isValid()) {
            return false;
        }

        var store = ref.getStore();
        var world = store.getExternalData().getWorld();
        if (world == null) {
            return false;
        }

        world.execute(() -> {
            var playerRefComponent = store.getComponent(ref, PlayerRef.getComponentType());
            if (playerRefComponent != null) {
                player.getPageManager().openCustomPage(ref, store,
                        pageFactory.apply(playerRefComponent, lifetime));
            }
        });

        return true;
    }

    /**
     * Opens a custom UI page for a player using the provided store and ref.
     * <p>
     * Use this overload when you already have access to store and ref from
     * an AbstractPlayerCommand execution context.
     *
     * @param player      the player to show the UI to
     * @param store       the entity store
     * @param ref         the entity reference
     * @param world       the world for threaded execution
     * @param pageFactory factory function that creates the page given PlayerRef and
     *                    lifetime
     * @param lifetime    the page lifetime
     */
    public static void openPage(
            @Nonnull Player player,
            @Nonnull Store<EntityStore> store,
            @Nonnull Ref<EntityStore> ref,
            @Nonnull World world,
            @Nonnull BiFunction<PlayerRef, CustomPageLifetime, ? extends InteractiveCustomUIPage<?>> pageFactory,
            @Nonnull CustomPageLifetime lifetime) {

        world.execute(() -> {
            var playerRefComponent = store.getComponent(ref, PlayerRef.getComponentType());
            if (playerRefComponent != null) {
                player.getPageManager().openCustomPage(ref, store,
                        pageFactory.apply(playerRefComponent, lifetime));
            }
        });
    }

    /**
     * Opens a dismissible custom UI page for a player.
     * <p>
     * Convenience method that uses {@link CustomPageLifetime#CanDismiss}.
     *
     * @param player      the player to show the UI to
     * @param pageFactory factory function that creates the page
     * @return true if the page opening was scheduled
     */
    public static boolean openDismissiblePage(
            @Nonnull Player player,
            @Nonnull BiFunction<PlayerRef, CustomPageLifetime, ? extends InteractiveCustomUIPage<?>> pageFactory) {
        return openPage(player, pageFactory, CustomPageLifetime.CanDismiss);
    }
}
