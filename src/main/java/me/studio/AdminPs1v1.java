package me.studio;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.List;

public final class AdminPs1v1 implements CommandExecutor, TabCompleter {

    private final Main1v1 plugin;

    public AdminPs1v1(Main1v1 plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        LanguageManager messages = plugin.getLanguageManager();

        if (!sender.hasPermission("ps1v1.admin")) {
            sender.sendMessage(messages.component("no-permission"));
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage(messages.component("admin-usage"));
            return true;
        }

        if (args[0].equalsIgnoreCase("reload")) {
            plugin.reloadConfig();
            plugin.getLanguageManager().reload();
            sender.sendMessage(plugin.getLanguageManager().component("admin-reload-success"));
            return true;
        }

        sender.sendMessage(messages.component("admin-unknown"));
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("ps1v1.admin")) {
            return List.of();
        }

        if (args.length == 1) {
            List<String> list = new ArrayList<>();
            String start = args[0].toLowerCase();

            if ("reload".startsWith(start)) {
                list.add("reload");
            }
            return list;
        }

        return List.of();
    }
}
