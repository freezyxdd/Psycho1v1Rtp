package me.studio;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public final class Comando1v1 implements CommandExecutor {

    private final Main1v1 plugin;
    private final Fila1v1 fila;

    public Comando1v1(Main1v1 plugin, Fila1v1 fila) {
        this.plugin = plugin;
        this.fila = fila;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        LanguageManager messages = plugin.getLanguageManager();

        if (!(sender instanceof Player player)) {
            sender.sendMessage(messages.component("only-player"));
            return true;
        }

        UUID uuid = player.getUniqueId();

        if (fila.isQueued(uuid)) {
            fila.leave(player);
            player.sendActionBar(messages.component("cancelled"));
            SomCore.playConfigured(player, plugin, "sounds.cancelled");
            return true;
        }

        boolean requireArmor = plugin.getConfig().getBoolean("settings.require-full-armor", true);
        if (requireArmor && !ArmorChecks.hasFullArmorEquipped(player)) {
            player.sendActionBar(messages.component("no-armor"));
            SomCore.playConfigured(player, plugin, "sounds.error");
            return true;
        }

        fila.join(player);
        SomCore.playConfigured(player, plugin, "sounds.searching");
        return true;
    }
}
