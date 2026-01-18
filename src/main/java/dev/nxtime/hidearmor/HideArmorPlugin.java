package dev.nxtime.hidearmor;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.hypixel.hytale.server.core.event.events.player.PlayerReadyEvent;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.entity.LivingEntityInventoryChangeEvent;
import com.hypixel.hytale.server.core.modules.entity.tracker.EntityTrackerSystems.EntityViewer;
import dev.nxtime.hidearmor.commands.HideArmorCommand;
import dev.nxtime.hidearmor.commands.HideArmorUICommand;
import dev.nxtime.hidearmor.commands.HideHelmetCommand;
import dev.nxtime.hidearmor.commands.HideHelmetDebugCommand;
import dev.nxtime.hidearmor.gui.HideArmorGui;
import dev.nxtime.hidearmor.net.HideArmorPacketReceiver;
import dev.nxtime.hidearmor.util.PluginLogger;

import com.hypixel.hytale.server.core.universe.world.World;
import dev.nxtime.hidearmor.commands.HideArmorAdminCommand;
import dev.nxtime.hidearmor.util.TranslationManager;

import javax.annotation.Nonnull;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Main plugin class for HideArmor - advanced armor visibility control for
 * Hytale servers.
 * <p>
 * This plugin allows players to:
 * <ul>
 * <li>Hide their own armor pieces from their view</li>
 * <li>Hide other players' armor (with mutual opt-in)</li>
 * <li>Control which armor pieces others can hide on them</li>
 * </ul>
 * <p>
 * The plugin wraps each player's packet receiver to intercept outgoing
 * {@code EntityUpdates}
 * packets and modify equipment data based on visibility settings. All changes
 * are purely
 * visual and do not affect server-side gameplay mechanics.
 * <p>
 * <b>Persistence:</b> Player settings are automatically saved to
 * {@code players.json} with
 * a 1.5 second debounce to reduce disk I/O.
 * <p>
 * <b>Thread Safety:</b> Uses concurrent data structures and world-threaded
 * execution for
 * all entity operations.
 *
 * @author nxtime
 * @version 0.8.0
 * @see HideArmorState
 * @see HideArmorPacketReceiver
 */
public class HideArmorPlugin extends JavaPlugin {

    private static HideArmorPlugin instance;

    /** Maximum valid bitmask value (12 bits: 2^12 - 1). */
    private static final int MAX_MASK = 4095;

    /** Lock for synchronizing save operations. */
    private final Object saveLock = new Object();

    /** JSON serializer with pretty printing for human-readable save files. */
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    /** The file where player settings are persisted. */
    private File dataFile;

    /** Executor service for debounced background saves. */
    private ScheduledExecutorService saveExecutor;

    /** Currently scheduled save task, or null if none pending. */
    private ScheduledFuture<?> pendingSave;

    /** Whether unsaved changes exist. */
    private volatile boolean dirty = false;

    /**
     * Rate limiting for equipment invalidation per player.
     * Maps player UUID to last invalidation timestamp in milliseconds.
     */
    /**
     * Active scheduled refresh tasks for inventory changes.
     * Used to throttle refreshes to max 1 per 50ms (1 tick).
     */
    private final ConcurrentHashMap<UUID, ScheduledFuture<?>> inventoryRefreshTasks = new ConcurrentHashMap<>();

    /**
     * Constructs the plugin instance.
     *
     * @param init the plugin initialization context
     */
    public HideArmorPlugin(@Nonnull JavaPluginInit init) {
        super(init);
        instance = this;
    }

    public static HideArmorPlugin getInstance() {
        return instance;
    }

    /**
     * Tracked worlds for global equipment refresh. Uses weak references to prevent
     * memory leaks.
     */
    private final Set<World> trackedWorlds = Collections
            .synchronizedSet(Collections.newSetFromMap(new WeakHashMap<>()));

    /**
     * Registers a world for tracking (called during player join events).
     *
     * @param world the world to track
     */
    public void trackWorld(World world) {
        if (world != null) {
            trackedWorlds.add(world);
        }
    }

    /**
     * Refreshes equipment visibility for all online players in tracked worlds.
     * <p>
     * Call this when global settings (like forcedMask) change to immediately
     * apply the new visibility rules to all players without requiring rejoin.
     */
    public void refreshAllPlayersEquipment() {
        for (var world : trackedWorlds) {
            if (world == null)
                continue;

            world.execute(() -> {
                try {
                    for (var player : world.getPlayers()) {
                        if (player == null)
                            continue;
                        try {
                            player.invalidateEquipmentNetwork();
                        } catch (Throwable ignored) {
                            // Player might have disconnected
                        }
                    }
                } catch (Throwable t) {
                    PluginLogger.debug("Failed to refresh in world: " + t.getMessage());
                }
            });
        }
        PluginLogger.debug("Triggered equipment refresh for all players.");
    }

    /**
     * Sets up the plugin during server startup.
     * <p>
     * Initialization sequence:
     * <ol>
     * <li>Initialize GUI system with logging bridge</li>
     * <li>Create/load persistent storage file</li>
     * <li>Load saved player settings from disk</li>
     * <li>Register onChange callback for auto-save</li>
     * <li>Register commands</li>
     * <li>Install packet receivers for all players</li>
     * <li>Hook inventory change events for armor refresh</li>
     * </ol>
     */
    @Override
    protected void setup() {
        // Initialize GUI with logger bridge
        HideArmorGui.init(PluginLogger.createGuiBridge());

        // Initialize translations
        TranslationManager.init();

        initDataFile();
        int loadedCount = loadStateFromDisk();
        PluginLogger.info("Plugin enabled. Loaded %d players.", loadedCount);
        HideArmorState.setOnChange(this::markDirtyAndScheduleSave);

        // Commands
        this.getCommandRegistry().registerCommand(
                new HideHelmetCommand("hidehelmet", "Toggle helmet visibility"));

        this.getCommandRegistry().registerCommand(
                new HideArmorCommand("hidearmor", "Toggle armor visibility"));

        this.getCommandRegistry().registerCommand(
                new HideArmorUICommand("hidearmorui", "Open armor visibility UI menu"));

        this.getCommandRegistry().registerCommand(
                new HideHelmetDebugCommand("hhdebug", "Print armor slot indices"));

        this.getCommandRegistry().registerCommand(
                new HideArmorAdminCommand("hidearmoradmin", "Admin configuration menu"));

        // Test mode for single-player testing (disabled in production)
        // Uncomment to enable: /hidearmor test enable/disable/status/simulate
        // this.getCommandRegistry().registerCommand(
        // new HideArmorTestCommand("hidearmortest", "Test mode for single player"));

        // Install packet wrapper per player when they are ready
        this.getEventRegistry().registerGlobal(PlayerReadyEvent.class, (event) -> {
            Player player = event.getPlayer();
            var world = player.getWorld();
            if (world == null)
                return;

            // Track this world for global equipment refresh
            trackWorld(world);

            world.execute(() -> {
                try {
                    // Get the EntityViewer component from the entity store and wrap its packet
                    // receiver
                    // This allows us to intercept outgoing packets to hide armor visually
                    var store = world.getEntityStore().getStore();
                    var ref = player.getReference();

                    EntityViewer viewer = store.getComponent(ref, EntityViewer.getComponentType());
                    if (viewer == null || viewer.packetReceiver == null)
                        return;

                    // Prevent double-wrapping the packet receiver to avoid performance issues
                    if (!(viewer.packetReceiver instanceof HideArmorPacketReceiver)) {
                        viewer.packetReceiver = new HideArmorPacketReceiver(
                                viewer.packetReceiver,
                                player.getUuid(),
                                player.getNetworkId(),
                                world);
                    }

                    if (HideArmorState.getMask(player.getUuid()) != 0) {
                        try {
                            player.invalidateEquipmentNetwork();
                        } catch (Throwable ignored) {
                        }
                    }

                } catch (Throwable t) {
                    // Catch all exceptions to prevent server crashes if SDK internals change in
                    // future updates
                }
            });
        });

        // Fail-safe mechanism: Ensure armor hiding persists after inventory changes
        // (because client-side Inventory updates can override visual state).
        // Uses a Throttled Fixed Delay strategy:
        // - Schedules a refresh for 50ms (1 tick) later.
        // - If a refresh is already pending/running, doesn't schedule another.
        // This ensures the refresh happens AFTER the event (resolving race conditions)
        // while preventing server overload during rapid inventory changes (vacuuming).
        this.getEventRegistry().registerGlobal(LivingEntityInventoryChangeEvent.class, (event) -> {
            if (!(event.getEntity() instanceof Player player))
                return;

            // Check if this player has hide settings OR if they have forced settings
            // applied
            int mask = HideArmorState.getMask(player.getUuid());
            int forcedMask = HideArmorState.getForcedMask();
            if (mask == 0 && forcedMask == 0)
                return;

            UUID uuid = player.getUuid();
            var world = player.getWorld();
            if (world == null)
                return;

            // Check if a refresh task is already pending or running
            java.util.concurrent.ScheduledFuture<?> task = inventoryRefreshTasks.get(uuid);
            if (task == null || task.isDone()) {
                // Schedule a new refresh for 50ms later (1 tick)
                // This delay ensures our "Fix" packet arrives AFTER the natural "Bad" packet
                task = saveExecutor.schedule(() -> {
                    world.execute(() -> {
                        try {
                            player.invalidateEquipmentNetwork();
                        } catch (Throwable ignored) {
                        }
                    });
                }, HideArmorState.getRefreshDelayMs(), TimeUnit.MILLISECONDS);
                inventoryRefreshTasks.put(uuid, task);
            }
        });

    }

    /**
     * Cleans up resources during server shutdown.
     * <p>
     * Performs a final save of all player settings and shuts down the save
     * executor.
     */
    @Override
    protected void shutdown() {
        int savedCount = saveStateToDisk();
        PluginLogger.info("Plugin disabled. Saved %d players.", savedCount);
        if (saveExecutor != null) {
            saveExecutor.shutdownNow();
        }
    }

    /**
     * Initializes the data directory and creates the players.json file if it
     * doesn't exist.
     * <p>
     * Also creates the background executor service for debounced saves.
     */
    private void initDataFile() {
        Path dataDir = getDataDirectory();
        if (dataDir == null)
            return;

        File dir = dataDir.toFile();
        if (!dir.exists()) {
            dir.mkdirs();
        }

        dataFile = new File(dir, "players.json");
        if (!dataFile.exists()) {
            try {
                Files.writeString(dataFile.toPath(), "{\"players\":{}}", StandardCharsets.UTF_8);
            } catch (Exception e) {
                PluginLogger.error("Failed to create players.json", e);
            }
        }

        saveExecutor = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "HideHelmet-Save");
            t.setDaemon(true);
            return t;
        });
    }

    /**
     * Marks state as dirty and schedules a debounced save operation.
     * <p>
     * If a save is already scheduled, this does nothing. Otherwise, schedules
     * a save to execute 1.5 seconds from now. This debouncing reduces disk I/O
     * when multiple rapid changes occur.
     */
    private void markDirtyAndScheduleSave() {
        dirty = true;
        if (saveExecutor == null)
            return;

        synchronized (saveLock) {
            if (pendingSave == null || pendingSave.isDone()) {
                pendingSave = saveExecutor.schedule(this::saveStateToDisk, 1500, TimeUnit.MILLISECONDS);
            }
        }
    }

    /**
     * Loads player settings from the persistent storage file.
     * <p>
     * Reads {@code players.json}, validates all entries, and populates
     * {@link HideArmorState}
     * silently (without triggering save callbacks). Invalid entries are skipped.
     *
     * @return the number of players successfully loaded
     */
    /**
     * Reloads the configuration and player states from disk.
     * This overrides any in-memory changes that haven't been saved yet.
     */
    public void reloadConfiguration() {
        if (loadStateFromDisk() > 0) {
            PluginLogger.info("Configuration reloaded from disk.");
        } else {
            PluginLogger.error("Failed to reload configuration or file is empty.");
        }
    }

    private int loadStateFromDisk() {
        if (dataFile == null || !dataFile.exists())
            return 0;

        try {
            String json = Files.readString(dataFile.toPath(), StandardCharsets.UTF_8);
            SaveModel model = gson.fromJson(json, SaveModel.class);
            if (model == null || model.players == null)
                return 0;

            int loaded = 0;
            for (Map.Entry<String, Integer> entry : model.players.entrySet()) {
                Integer mask = entry.getValue();
                if (mask == null)
                    continue;

                int clamped = Math.max(0, Math.min(MAX_MASK, mask));
                if (clamped == 0)
                    continue;

                try {
                    UUID uuid = UUID.fromString(entry.getKey());
                    HideArmorState.setMaskSilently(uuid, clamped);
                    loaded++;
                } catch (IllegalArgumentException ignored) {
                }
            }

            // Load languages
            if (model.languages != null) {
                for (Map.Entry<String, String> entry : model.languages.entrySet()) {
                    try {
                        UUID uuid = UUID.fromString(entry.getKey());
                        if (entry.getValue() != null) {
                            HideArmorState.setLanguage(uuid, entry.getValue());
                        }
                    } catch (IllegalArgumentException ignored) {
                    }
                }
            }

            // Load global config
            if (model.config != null) {
                HideArmorState.setDefaultMask(model.config.defaultMask);
                HideArmorState.setForcedMask(model.config.forcedMask);
                HideArmorState.setRefreshDelayMs(model.config.refreshDelayMs);
                if (model.config.defaultLanguage != null) {
                    HideArmorState.setDefaultLanguage(model.config.defaultLanguage);
                }
            }

            return loaded;
        } catch (Exception e) {
            PluginLogger.error("Failed to load state", e);
            return 0;
        }
    }

    /**
     * Saves all player settings to the persistent storage file.
     * <p>
     * Creates a snapshot of current state, filters out invalid/zero masks,
     * serializes
     * to JSON, and writes to disk. Only saves if dirty flag is set.
     * <p>
     * If save fails, re-marks state as dirty for retry on next trigger.
     *
     * @return the number of players successfully saved
     */
    private int saveStateToDisk() {
        if (dataFile == null)
            return 0;

        synchronized (saveLock) {
            if (!dirty)
                return 0;
            dirty = false;
        }

        try {
            Map<UUID, Integer> snapshot = HideArmorState.snapshot();
            Map<String, Integer> out = new HashMap<>();

            for (Map.Entry<UUID, Integer> entry : snapshot.entrySet()) {
                Integer mask = entry.getValue();
                if (mask == null)
                    continue;
                int clamped = Math.max(0, Math.min(MAX_MASK, mask));
                if (clamped == 0)
                    continue;
                out.put(entry.getKey().toString(), clamped);
            }

            Map<String, String> outLang = new HashMap<>();
            Map<UUID, String> snapshotLang = HideArmorState.snapshotLanguages();
            for (Map.Entry<UUID, String> entry : snapshotLang.entrySet()) {
                if (entry.getValue() != null) {
                    outLang.put(entry.getKey().toString(), entry.getValue());
                }
            }

            SaveModel model = new SaveModel();
            model.players = out;
            model.languages = outLang;
            model.config = new GlobalConfig();
            model.config.defaultMask = HideArmorState.getDefaultMask();
            model.config.forcedMask = HideArmorState.getForcedMask();
            model.config.refreshDelayMs = HideArmorState.getRefreshDelayMs();
            model.config.defaultLanguage = HideArmorState.getDefaultLanguage();

            String json = gson.toJson(model);
            Files.writeString(dataFile.toPath(), json, StandardCharsets.UTF_8);
            return out.size();
        } catch (Exception e) {
            synchronized (saveLock) {
                dirty = true;
            }
            PluginLogger.error("Failed to save state", e);
            return 0;
        }
    }

    /**
     * Data model for JSON serialization of player settings.
     * <p>
     * Format: {@code {"players": {"uuid-string": mask-integer}}}
     */
    private static final class SaveModel {
        /** Map of player UUID strings to their 12-bit mask values. */
        Map<String, Integer> players = new HashMap<>();
        /** Map of player UUID strings to their language code. */
        Map<String, String> languages = new HashMap<>();
        /** Global configuration settings. */
        GlobalConfig config = new GlobalConfig();
    }

    /**
     * Global configuration settings.
     */
    private static final class GlobalConfig {
        /** Default mask for new users or users with no explicit settings. */
        int defaultMask = 0;
        /** Forced mask that overrides all user settings. */
        int forcedMask = 0;
        /** Refresh delay in milliseconds for inventory change events. */
        int refreshDelayMs = 50;
        /** Default language for new players. */
        String defaultLanguage = "en_us";
    }
}
