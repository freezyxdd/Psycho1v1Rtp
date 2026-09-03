package me.studio;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.Locale;

public final class LanguageManager {

    private static final String ENGLISH = "en";
    private static final String PORTUGUESE_BR = "pt_BR";

    private final Main1v1 plugin;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    private FileConfiguration english;
    private FileConfiguration selected;
    private String language = ENGLISH;

    public LanguageManager(Main1v1 plugin) {
        this.plugin = plugin;
        ensureLanguageFiles();
        reload();
    }

    public void reload() {
        this.english = load(ENGLISH);
        this.language = normalize(plugin.getConfig().getString("language", ENGLISH));

        File selectedFile = fileFor(language);
        if (!selectedFile.exists()) {
            plugin.getLogger().warning("Language '" + language + "' was not found. Falling back to English.");
            this.language = ENGLISH;
            this.selected = english;
            return;
        }

        this.selected = YamlConfiguration.loadConfiguration(selectedFile);
    }

    public Component component(String key) {
        String raw = raw(key);

        try {
            return miniMessage.deserialize(raw);
        } catch (Exception ex) {
            plugin.getLogger().warning("Invalid MiniMessage format at messages." + key + ". Using plain text fallback.");
            return Component.text(raw);
        }
    }

    public String getLanguage() {
        return language;
    }

    private String raw(String key) {
        String path = "messages." + key;
        String value = selected == null ? null : selected.getString(path);

        if (value == null && english != null) {
            value = english.getString(path);
        }

        if (value == null) {
            plugin.getLogger().warning("Missing language message: " + path);
            return "<red>Missing message: " + path + "</red>";
        }

        return value;
    }

    private void ensureLanguageFiles() {
        saveIfMissing("languages/en.yml");
        saveIfMissing("languages/pt_BR.yml");
    }

    private void saveIfMissing(String resourcePath) {
        File target = new File(plugin.getDataFolder(), resourcePath);
        if (!target.exists()) {
            plugin.saveResource(resourcePath, false);
        }
    }

    private FileConfiguration load(String languageName) {
        return YamlConfiguration.loadConfiguration(fileFor(languageName));
    }

    private File fileFor(String languageName) {
        return new File(plugin.getDataFolder(), "languages/" + languageName + ".yml");
    }

    private String normalize(String configured) {
        if (configured == null || configured.isBlank()) {
            return ENGLISH;
        }

        String value = configured.trim().replace('-', '_').toLowerCase(Locale.ROOT);

        return switch (value) {
            case "pt", "ptbr", "pt_br" -> PORTUGUESE_BR;
            case "en", "en_us", "english" -> ENGLISH;
            default -> configured.trim();
        };
    }
}
