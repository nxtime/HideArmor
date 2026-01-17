package dev.nxtime.hidearmor.util;

/**
 * Utility class for LuckPerms integration detection and fallback.
 * <p>
 * Detects if LuckPerms is installed on the server and provides
 * a unified API for permission registration that falls back to
 * the native permissions.json approach when LP is not available.
 *
 * @author nxtime
 * @version 0.7.1
 */
public final class LuckPermsIntegration {

    private static Boolean luckPermsAvailable = null;

    private LuckPermsIntegration() {
        // Utility class - prevent instantiation
    }

    /**
     * Checks if LuckPerms is installed on the server.
     * <p>
     * Detection is done by checking for the presence of the
     * LuckPerms API provider class. Result is cached after first check.
     *
     * @return true if LuckPerms is available, false otherwise
     */
    public static boolean isLuckPermsAvailable() {
        if (luckPermsAvailable == null) {
            luckPermsAvailable = detectLuckPerms();
        }
        return luckPermsAvailable;
    }

    /**
     * Attempts to detect LuckPerms by checking for its API class.
     */
    private static boolean detectLuckPerms() {
        try {
            // Check for LuckPerms API class
            Class.forName("net.luckperms.api.LuckPermsProvider");
            PluginLogger.info("LuckPerms detected - using LP for permissions");
            return true;
        } catch (ClassNotFoundException e) {
            PluginLogger.debug("LuckPerms not found - using native permissions");
            return false;
        }
    }

    /**
     * Resets the cached LuckPerms detection state.
     * Useful for testing or hot-reload scenarios.
     */
    public static void resetCache() {
        luckPermsAvailable = null;
    }

    /**
     * Returns a human-readable name of the active permission system.
     *
     * @return "LuckPerms" or "Native (permissions.json)"
     */
    public static String getActiveSystemName() {
        return isLuckPermsAvailable() ? "LuckPerms" : "Native (permissions.json)";
    }
}
