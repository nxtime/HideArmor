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
// import dev.nxtime.hidearmor.commands.HideArmorTestCommand; // Uncomment for test mode
import dev.nxtime.hidearmor.gui.HideArmorGui;
import dev.nxtime.hidearmor.net.HideArmorPacketReceiver;

import javax.annotation.Nonnull;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

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
 * @version 0.4.0
 * @see HideArmorState
 * @see HideArmorPacketReceiver
 */
public class HideArmorPlugin extends JavaPlugin {

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
    private boolean dirty;

    /**
     * Rate limiting for equipment invalidation per player.
     * Maps player UUID to last invalidation timestamp in milliseconds.
     */
    private final Map<UUID, Long> lastInvalidateByPlayer = new ConcurrentHashMap<>();

    /**
     * Constructs the plugin instance.
     *
     * @param init the plugin initialization context
     */
    public HideArmorPlugin(@Nonnull JavaPluginInit init) {
        super(init);
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
        // Initialize GUI with logger
        HideArmorGui.init((level, message) -> {
            switch (level) {
                case "WARNING" -> this.getLogger().at(Level.WARNING).log(message);
                case "SEVERE" -> this.getLogger().at(Level.SEVERE).log(message);
                default -> this.getLogger().at(Level.INFO).log(message);
            }
        });

        initDataFile();
        int loadedCount = loadStateFromDisk();
        this.getLogger().at(Level.INFO).log("HideHelmet enabled (self-only). Loaded " + loadedCount + " players.");
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
                new dev.nxtime.hidearmor.commands.HideArmorAdminCommand("hidearmoradmin", "Admin configuration menu"));

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

        // Fallback mechanism: Re-apply armor hiding whenever the inventory changes
        // The client sometimes rehydrates armor on events like damage, repair, or item
        // changes
        // This event listener ensures hidden armor stays hidden by forcing a refresh
        this.getEventRegistry().registerGlobal(LivingEntityInventoryChangeEvent.class, (event) -> {
            if (!(event.getEntity() instanceof Player player))
                return;
            if (HideArmorState.getMask(player.getUuid()) == 0)
                return;

            long now = System.currentTimeMillis();
            Long last = lastInvalidateByPlayer.get(player.getUuid());
            if (last != null && (now - last) < 10)
                return;
            lastInvalidateByPlayer.put(player.getUuid(), now);

            var world = player.getWorld();
            if (world == null)
                return;

            world.execute(() -> {
                try {
                    player.invalidateEquipmentNetwork();
                } catch (Throwable ignored) {
                }
            });
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
        this.getLogger().at(Level.INFO).log("HideHelmet disabled. Saved " + savedCount + " players.");
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
                System.err.println("HideHelmet: Failed to create players.json: " + e.getMessage());
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

            // Load global config
            if (model.config != null) {
                HideArmorState.setDefaultMask(model.config.defaultMask);
            }

            return loaded;
        } catch (Exception e) {
            System.err.println("HideHelmet: Failed to load state: " + e.getMessage());
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

            SaveModel model = new SaveModel();
            model.players = out;
            model.config = new GlobalConfig();
            model.config.defaultMask = HideArmorState.getDefaultMask();

            String json = gson.toJson(model);
            Files.writeString(dataFile.toPath(), json, StandardCharsets.UTF_8);
            return out.size();
        } catch (Exception e) {
            synchronized (saveLock) {
                dirty = true;
            }
            System.err.println("HideHelmet: Failed to save state: " + e.getMessage());
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
        /** Global configuration settings. */
        GlobalConfig config = new GlobalConfig();
    }

    /**
     * Global configuration settings.
     */
    private static final class GlobalConfig {
        /** Default mask for new users or users with no explicit settings. */
        int defaultMask = 0;
    }
}
