package dev.nxtime.hidearmor.util;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import dev.nxtime.hidearmor.HideArmorState;
import com.hypixel.hytale.server.core.entity.entities.Player;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages translations for the HideArmor plugin.
 * Loads language files from /lang/ directory in the JAR.
 */
public class TranslationManager {
    private static final Map<String, Map<String, String>> translations = new ConcurrentHashMap<>();
    private static final String DEFAULT_LANG = "en_us";
    private static final Gson gson = new Gson();
    private static boolean initialized = false;

    /** All supported language codes */
    private static final String[] LANGUAGES = {
            "en_us", "es_es", "pt_br", "fr_fr", "ru_ru", "de_de", "zh_cn"
    };

    /**
     * Initialize and preload all language files.
     * Should be called during plugin setup.
     */
    public static void init() {
        if (initialized)
            return;
        initialized = true;

        PluginLogger.info("Initializing TranslationManager...");
        for (String lang : LANGUAGES) {
            load(lang);
        }
        PluginLogger.info("TranslationManager initialized with %d languages.", translations.size());
    }

    /**
     * Load a specific language file.
     */
    public static void load(String lang) {
        if (translations.containsKey(lang)) {
            return; // Already loaded
        }

        String path = "/lang/" + lang + ".json";
        PluginLogger.debug("Attempting to load language file: " + path);

        try (InputStream is = TranslationManager.class.getResourceAsStream(path)) {
            if (is == null) {
                PluginLogger.warn("Language file not found: " + path);
                return;
            }
            try (InputStreamReader reader = new InputStreamReader(is, StandardCharsets.UTF_8)) {
                JsonObject json = gson.fromJson(reader, JsonObject.class);
                if (json == null) {
                    PluginLogger.warn("Language file is empty or invalid: " + path);
                    return;
                }
                Map<String, String> map = new HashMap<>();
                for (String key : json.keySet()) {
                    map.put(key, json.get(key).getAsString());
                }
                translations.put(lang, map);
                PluginLogger.info("Loaded language: %s with %d keys", lang, map.size());
            }
        } catch (Exception e) {
            PluginLogger.error("Failed to load language: " + lang, e);
        }
    }

    /**
     * Get a translated string for the given player.
     */
    public static String get(UUID playerUuid, String key, Object... args) {
        // Ensure initialized
        if (!initialized) {
            init();
        }

        String lang = HideArmorState.getLanguage(playerUuid);

        // Use player's language if available and loaded, otherwise default
        if (lang == null || !translations.containsKey(lang)) {
            // Try loading the language if not found
            if (lang != null && !translations.containsKey(lang)) {
                load(lang);
            }
            // If still not available, use default
            if (lang == null || !translations.containsKey(lang)) {
                lang = DEFAULT_LANG;
            }
        }

        Map<String, String> langMap = translations.get(lang);
        if (langMap == null) {
            // Last resort: try to get from default
            langMap = translations.get(DEFAULT_LANG);
            if (langMap == null) {
                return key; // No translations available at all
            }
        }

        // Get the value, falling back to default language, then to key itself
        String val = langMap.get(key);
        if (val == null) {
            Map<String, String> defaultMap = translations.get(DEFAULT_LANG);
            val = (defaultMap != null) ? defaultMap.getOrDefault(key, key) : key;
        }

        try {
            return String.format(val, args);
        } catch (Exception e) {
            return val;
        }
    }

    /**
     * Get a translated string for the given player.
     */
    public static String get(Player player, String key, Object... args) {
        return get(player.getUuid(), key, args);
    }

    /**
     * Check if a language is available.
     */
    public static boolean isLanguageAvailable(String lang) {
        if (!initialized)
            init();
        return translations.containsKey(lang);
    }

    /**
     * Get list of available language codes.
     */
    public static String[] getAvailableLanguages() {
        return LANGUAGES;
    }
}
