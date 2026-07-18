package me.studio;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public final class Fila1v1 {

    private final Main1v1 plugin;

    private final ConcurrentLinkedQueue<UUID> queue = new ConcurrentLinkedQueue<>();
    private final Set<UUID> queued = ConcurrentHashMap.newKeySet();


    private final Map<UUID, ScheduledTask> actionbarTasks = new ConcurrentHashMap<>();

    public Fila1v1(Main1v1 plugin) {
        this.plugin = plugin;
    }

    public boolean isQueued(UUID id) {
        return queued.contains(id);
    }

    public void join(Player player) {
        UUID id = player.getUniqueId();

        if (!queued.add(id)) return;
        queue.add(id);

        startSearchingActionbar(player);


        plugin.getServer().getGlobalRegionScheduler().execute(plugin, this::tryStartMatch);
    }

    public void leave(Player player) {
        UUID id = player.getUniqueId();

        stopSearchingActionbar(id);

        queued.remove(id);
        queue.remove(id);
    }

    private void tryStartMatch() {
        if (queue.size() < 2) return;

        UUID id1 = queue.poll();
        UUID id2 = queue.poll();

        if (id1 == null || id2 == null) return;

        queued.remove(id1);
        queued.remove(id2);


        stopSearchingActionbar(id1);
        stopSearchingActionbar(id2);

        Player p1 = Bukkit.getPlayer(id1);
        Player p2 = Bukkit.getPlayer(id2);

        if (p1 == null || p2 == null || !p1.isOnline() || !p2.isOnline()) return;

        startMatch(p1, p2);
    }

    private void startMatch(Player p1, Player p2) {

        // Verifição do Mundo
        World world = selectMatchWorld();

        if (world == null) {
            plugin.getLogger().warning(
                    "No valid 1v1 world found in config.yml!"
            );
            return;
        }

        // Mensagem mostrada quando a partida é encontrada
        String foundMsgRaw = plugin.getConfig().getString(
                "messages.found",
                "§71v1 Found! Teleporting..."
        );

        String foundMsg = foundMsgRaw == null ? "" : foundMsgRaw;

        // Envia a mensagem e toca o som para o primeiro jogador
        p1.getScheduler().execute(plugin, () -> {
            p1.sendActionBar(Component.text(foundMsg));
            SomCore.playConfigured(p1, plugin, "sounds.found");
        }, null, 1L);

        // Envia a mensagem e toca o som para o segundo jogador
        p2.getScheduler().execute(plugin, () -> {
            p2.sendActionBar(Component.text(foundMsg));
            SomCore.playConfigured(p2, plugin, "sounds.found");
        }, null, 1L);

        // Lê as configurações do teleporte
        int radius = plugin.getConfig().getInt(
                "settings.teleport-radius",
                5000
        );

        boolean faceToFace = plugin.getConfig().getBoolean(
                "settings.face-to-face.enabled",
                true
        );

        double distance = plugin.getConfig().getDouble(
                "settings.face-to-face.distance",
                10.0
        );

        // Procura dois locais seguros no mundo escolhido
        CompletableFuture<Location[]> future =
                TeleporteCore.createPairAsync(
                        world,
                        radius,
                        faceToFace,
                        distance
                );

        future.thenAccept(pair -> {
            if (pair == null || pair.length < 2) {
                return;
            }

            // Teleporta os dois jogadores
            p1.getScheduler().execute(
                    plugin,
                    () -> p1.teleportAsync(pair[0]),
                    null,
                    1L
            );

            p2.getScheduler().execute(
                    plugin,
                    () -> p2.teleportAsync(pair[1]),
                    null,
                    1L
            );

        }).exceptionally(ex -> {
            plugin.getLogger().warning(
                    "Failed to create teleport pair: " + ex.getMessage()
            );

            ex.printStackTrace();
            return null;
        });
    }


    private World selectMatchWorld() {
        boolean randomWorld = plugin.getConfig().getBoolean(
                "settings.world-selection.random-world",
                false
        );

        if (!randomWorld) {
            String defaultWorldName = plugin.getConfig().getString(
                    "settings.world-selection.default-world",
                    "world"
            );

            if (defaultWorldName == null) {
                return null;
            }

            return Bukkit.getWorld(defaultWorldName);
        }

        List<String> configuredWorlds = plugin.getConfig().getStringList(
                "settings.world-selection.worlds"
        );

        List<World> validWorlds = new ArrayList<>();

        for (String worldName : configuredWorlds) {
            World configuredWorld = Bukkit.getWorld(worldName);

            if (configuredWorld != null) {
                validWorlds.add(configuredWorld);
            } else {
                plugin.getLogger().warning(
                        "Configured 1v1 world is not loaded: " + worldName
                );
            }
        }

        if (validWorlds.isEmpty()) {
            return null;
        }

        int randomIndex = ThreadLocalRandom.current().nextInt(validWorlds.size());

        return validWorlds.get(randomIndex);
    }

    private void startSearchingActionbar(Player player) {
        UUID id = player.getUniqueId();


        stopSearchingActionbar(id);

        String msgRaw = plugin.getConfig().getString("messages.searching",
                "§7Searching for an opponent. Type /1v1 again to leave");
        String msg = msgRaw == null ? "" : msgRaw;

        player.sendActionBar(Component.text(msg));

        ScheduledTask task = player.getScheduler().runAtFixedRate(plugin, (ScheduledTask t) -> {

            if (!player.isOnline() || !isQueued(id)) {
                t.cancel();
                actionbarTasks.remove(id);
                return;
            }

            player.sendActionBar(Component.text(msg));

        }, null, 1L, 20L);

        actionbarTasks.put(id, task);
    }

    private void stopSearchingActionbar(UUID id) {
        ScheduledTask task = actionbarTasks.remove(id);
        if (task != null) {
            task.cancel();
        }
    }
}
