package me.studio;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public final class PlayerListener implements Listener {

    private final Fila1v1 fila;

    public PlayerListener(Fila1v1 fila) {
        this.fila = fila;
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        fila.leave(e.getPlayer());
    }
}
