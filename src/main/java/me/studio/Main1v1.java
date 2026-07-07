package me.studio;

import org.bukkit.plugin.java.JavaPlugin;

public final class Main1v1 extends JavaPlugin {

    private Fila1v1 fila;

    @Override
    public void onEnable() {


        try {
            saveDefaultConfig();
        } catch (Exception e) {
            getLogger().warning("Could not save default config.yml (missing inside jar).");
            getLogger().warning("Fix: put config.yml in src/main/resources/config.yml and rebuild.");
        }

        this.fila = new Fila1v1(this);


        Comando1v1 cmd1v1 = new Comando1v1(this, fila);
        getCommand("1v1").setExecutor(cmd1v1);
        getCommand("rtpqueue").setExecutor(cmd1v1);


        AdminPs1v1 admin = new AdminPs1v1(this);
        getCommand("ps1v1").setExecutor(admin);
        getCommand("ps1v1").setTabCompleter(admin);


        getServer().getPluginManager().registerEvents(new PlayerListener(fila), this);

        getLogger().info("Plugin 1v1 started!");
    }

    public Fila1v1 getFila() {
        return fila;
    }
}
