package me.studio;

import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public final class SomCore {

    private SomCore() {}

    public static void playConfigured(Player player, Plugin plugin, String path) {
        try {
            String soundName = plugin.getConfig().getString(path + ".sound", "ui.button.click");
            double volume = plugin.getConfig().getDouble(path + ".volume", 1.0);
            double pitch = plugin.getConfig().getDouble(path + ".pitch", 1.0);

            if (soundName == null || soundName.isBlank()) return;

            NamespacedKey key = NamespacedKey.minecraft(soundName.toLowerCase());
            Sound sound = Registry.SOUNDS.get(key);

            if (sound == null) {
                plugin.getLogger().warning("Invalid sound: " + soundName + " at " + path);
                return;
            }

            player.playSound(player.getLocation(), sound, (float) volume, (float) pitch);
        } catch (Exception e) {
            plugin.getLogger().warning("Error playing sound at: " + path);
        }
    }
}
