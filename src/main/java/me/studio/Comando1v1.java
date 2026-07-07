package me.studio;

import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class Comando1v1 implements CommandExecutor {

    private final Main1v1 plugin;
    private final Fila1v1 fila;

    public Comando1v1(Main1v1 plugin, Fila1v1 fila) {
        this.plugin = plugin;
        this.fila = fila;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player player)) {
            sender.sendMessage(color(plugin.getConfig().getString("messages.only-player",
                    "§7Only players can use this command.")));
            return true;
        }

        UUID uuid = player.getUniqueId();


        if (fila.isQueued(uuid)) {
            fila.leave(player);

            player.sendActionBar(Component.text(color(plugin.getConfig().getString("messages.cancelled",
                    "§7You left the 1v1 queue."))));
            SomCore.playConfigured(player, plugin, "sounds.cancelled");
            return true;
        }


        boolean requireArmor = plugin.getConfig().getBoolean("settings.require-full-armor", true);
        if (requireArmor && !ArmorChecks.hasFullArmorEquipped(player)) {
            player.sendActionBar(Component.text(color(plugin.getConfig().getString("messages.no-armor",
                    "§7You must wear a helmet, chestplate, leggings and boots."))));
            SomCore.playConfigured(player, plugin, "sounds.error");
            return true;
        }


        fila.join(player);

        SomCore.playConfigured(player, plugin, "sounds.searching");

        return true;
    }

    private String color(String s) {
        return s == null ? "" : s;
    }
}
