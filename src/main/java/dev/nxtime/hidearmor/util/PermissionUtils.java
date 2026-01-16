package dev.nxtime.hidearmor.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

/**
 * Utility class for managing server permissions.json integration.
 * <p>
 * Provides methods to automatically add HideArmor permissions to
 * the server's permission configuration.
 *
 * @author nxtime
 * @version 0.7.0
 */
public final class PermissionUtils {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    /** HideArmor permission nodes to add. */
    private static final String[] HIDEARMOR_PERMISSIONS = {
            "dev.nxtime.hidearmor.command.hidearmor",
            "dev.nxtime.hidearmor.command.hidehelmet",
            "dev.nxtime.hidearmor.command.hidearmorui"
    };

    /** Target group to add permissions to. */
    private static final String DEFAULT_GROUP = "Adventure";

    private PermissionUtils() {
        // Utility class - prevent instantiation
    }

    /**
     * Result of a permission setup operation.
     */
    public static class SetupResult {
        public final boolean success;
        public final String message;
        public final int permissionsAdded;

        public SetupResult(boolean success, String message, int permissionsAdded) {
            this.success = success;
            this.message = message;
            this.permissionsAdded = permissionsAdded;
        }
    }

    /**
     * Attempts to add HideArmor permissions to the server's permissions.json.
     * <p>
     * Looks for permissions.json in the server root directory and adds
     * the HideArmor command permissions to the Adventure group (or creates it).
     *
     * @param serverDataDir path to the plugin's data directory (used to find server
     *                      root)
     * @return SetupResult indicating success/failure and details
     */
    public static SetupResult setupPermissions(Path serverDataDir) {
        if (serverDataDir == null) {
            PluginLogger.debug("Data directory is null");
            return new SetupResult(false, "Plugin data directory not available", 0);
        }

        PluginLogger.debug("Data directory: " + serverDataDir.toAbsolutePath());

        // Try to find server root by looking for permissions.json
        Path serverRoot = findServerRoot(serverDataDir);

        if (serverRoot == null) {
            PluginLogger.error("Could not determine server root from: " + serverDataDir);
            return new SetupResult(false, "Could not determine server root directory", 0);
        }

        PluginLogger.debug("Server root: " + serverRoot.toAbsolutePath());

        File permissionsFile = serverRoot.resolve("permissions.json").toFile();

        if (!permissionsFile.exists()) {
            // Create a new permissions.json with our permissions
            return createNewPermissionsFile(permissionsFile);
        }

        return updateExistingPermissionsFile(permissionsFile);
    }

    /**
     * Tries to find the server root directory by checking parent directories
     * for the existence of permissions.json or other server markers.
     */
    private static Path findServerRoot(Path startPath) {
        Path current = startPath;

        // Strategy 1: Walk up parents looking for permissions.json
        for (int i = 0; i < 5; i++) {
            if (current == null)
                break;

            File permFile = current.resolve("permissions.json").toFile();
            if (permFile.exists()) {
                return current;
            }

            // Check for other server markers (like server.properties or similar)
            File serverMarker = current.resolve("server.properties").toFile();
            if (serverMarker.exists()) {
                return current;
            }

            current = current.getParent();
        }

        // Strategy 2: Assume plugins/HideArmor structure - go up 2 levels
        if (startPath.getParent() != null && startPath.getParent().getParent() != null) {
            return startPath.getParent().getParent();
        }

        // Strategy 3: Just try the working directory
        return Path.of(System.getProperty("user.dir"));
    }

    private static SetupResult createNewPermissionsFile(File file) {
        try {
            JsonObject root = new JsonObject();
            JsonObject groups = new JsonObject();

            JsonObject adventureGroup = new JsonObject();
            JsonArray permissions = new JsonArray();
            for (String perm : HIDEARMOR_PERMISSIONS) {
                permissions.add(perm);
            }
            adventureGroup.add("permissions", permissions);

            groups.add(DEFAULT_GROUP, adventureGroup);
            root.add("groups", groups);
            root.add("users", new JsonObject());

            String json = GSON.toJson(root);
            Files.writeString(file.toPath(), json, StandardCharsets.UTF_8);

            PluginLogger.info("Created permissions.json with HideArmor permissions");
            return new SetupResult(true,
                    "Created permissions.json with " + HIDEARMOR_PERMISSIONS.length + " permissions",
                    HIDEARMOR_PERMISSIONS.length);
        } catch (Exception e) {
            PluginLogger.error("Failed to create permissions.json", e);
            return new SetupResult(false, "Failed to create file: " + e.getMessage(), 0);
        }
    }

    private static SetupResult updateExistingPermissionsFile(File file) {
        try {
            String content = Files.readString(file.toPath(), StandardCharsets.UTF_8);
            JsonElement parsed = GSON.fromJson(content, JsonElement.class);

            JsonObject root;

            // Handle different JSON structures
            if (parsed == null || parsed.isJsonNull()) {
                root = new JsonObject();
            } else if (parsed.isJsonObject()) {
                root = parsed.getAsJsonObject();
            } else if (parsed.isJsonArray()) {
                // Wrap array in an object structure
                root = new JsonObject();
                root.add("permissions", parsed.getAsJsonArray());
                PluginLogger.debug("Wrapped existing array into object structure");
            } else {
                return new SetupResult(false, "permissions.json has unexpected format", 0);
            }

            // Get or create groups object
            JsonObject groups = root.getAsJsonObject("groups");
            if (groups == null) {
                groups = new JsonObject();
                root.add("groups", groups);
            }

            // Get or create Adventure group permissions array
            // The format is: "Adventure": [] (array directly, not object with permissions
            // key)
            JsonArray permissions;
            JsonElement adventureElement = groups.get(DEFAULT_GROUP);

            if (adventureElement == null) {
                // Create new array for Adventure group
                permissions = new JsonArray();
                groups.add(DEFAULT_GROUP, permissions);
            } else if (adventureElement.isJsonArray()) {
                // Direct array format: "Adventure": ["perm1", "perm2"]
                permissions = adventureElement.getAsJsonArray();
            } else if (adventureElement.isJsonObject()) {
                // Object format: "Adventure": { "permissions": [...] }
                JsonObject adventureGroup = adventureElement.getAsJsonObject();
                permissions = adventureGroup.getAsJsonArray("permissions");
                if (permissions == null) {
                    permissions = new JsonArray();
                    adventureGroup.add("permissions", permissions);
                }
            } else {
                return new SetupResult(false, "Adventure group has unexpected format", 0);
            }

            // Collect existing permissions
            Set<String> existingPerms = new HashSet<>();
            for (JsonElement elem : permissions) {
                if (elem.isJsonPrimitive()) {
                    existingPerms.add(elem.getAsString());
                }
            }

            // Add missing HideArmor permissions
            int added = 0;
            for (String perm : HIDEARMOR_PERMISSIONS) {
                if (!existingPerms.contains(perm)) {
                    permissions.add(perm);
                    added++;
                }
            }

            if (added == 0) {
                return new SetupResult(true, "Permissions already configured", 0);
            }

            // Write back
            String json = GSON.toJson(root);
            Files.writeString(file.toPath(), json, StandardCharsets.UTF_8);

            PluginLogger.info("Added %d HideArmor permissions to %s group", added, DEFAULT_GROUP);
            return new SetupResult(true,
                    "Added " + added + " permissions to " + DEFAULT_GROUP + " group",
                    added);
        } catch (Exception e) {
            PluginLogger.error("Failed to update permissions.json", e);
            return new SetupResult(false, "Failed to update file: " + e.getMessage(), 0);
        }
    }

    /**
     * Gets the list of HideArmor permission nodes.
     * 
     * @return array of permission strings
     */
    public static String[] getPermissionNodes() {
        return HIDEARMOR_PERMISSIONS.clone();
    }
}
