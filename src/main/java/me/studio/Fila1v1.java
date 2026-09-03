package me.studio;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
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

        if (!queued.add(id)) {
            return;
        }

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
        while (true) {
            Player p1 = pollNextOnlinePlayer();
            if (p1 == null) {
                return;
            }

            Player p2 = pollNextOnlinePlayer();
            if (p2 == null) {
                requeuePlayer(p1, null, false);
                return;
            }

            startMatch(p1, p2);
        }
    }

    private Player pollNextOnlinePlayer() {
        while (true) {
            UUID id = queue.poll();
            if (id == null) {
                return null;
            }

            if (!queued.remove(id)) {
                continue;
            }

            stopSearchingActionbar(id);

            Player player = Bukkit.getPlayer(id);
            if (player != null) {
                return player;
            }
        }
    }

    private void startMatch(Player p1, Player p2) {
        World world = selectMatchWorld();

        if (world == null) {
            plugin.getLogger().warning("No valid 1v1 world found in config.yml.");
            notifyPlayer(p1, "no-valid-world", true);
            notifyPlayer(p2, "no-valid-world", true);
            return;
        }

        sendFoundFeedback(p1);
        sendFoundFeedback(p2);

        int radius = Math.max(0, plugin.getConfig().getInt("settings.teleport-radius", 5000));
        boolean faceToFace = plugin.getConfig().getBoolean("settings.face-to-face.enabled", true);
        double distance = plugin.getConfig().getDouble("settings.face-to-face.distance", 10.0);
        if (distance <= 0.0) {
            distance = 10.0;
        }

        UUID id1 = p1.getUniqueId();
        UUID id2 = p2.getUniqueId();

        CompletableFuture<Location[]> future = TeleporteCore.createPairAsync(
                plugin,
                world,
                radius,
                faceToFace,
                distance
        );

        future.whenComplete((pair, throwable) -> {
            if (throwable != null) {
                plugin.getLogger().warning("Failed to create teleport pair: " + throwable.getMessage());
            }

            try {
                plugin.getServer().getGlobalRegionScheduler().execute(plugin, () -> {
                    if (throwable != null || pair == null || pair.length < 2 || pair[0] == null || pair[1] == null) {
                        requeueIfOnline(id1, "teleport-failed", false);
                        requeueIfOnline(id2, "teleport-failed", false);
                        return;
                    }

                    Player online1 = Bukkit.getPlayer(id1);
                    Player online2 = Bukkit.getPlayer(id2);

                    if (online1 == null && online2 == null) {
                        return;
                    }

                    if (online1 == null) {
                        requeuePlayer(online2, "opponent-unavailable", true);
                        return;
                    }

                    if (online2 == null) {
                        requeuePlayer(online1, "opponent-unavailable", true);
                        return;
                    }

                    teleportPlayer(online1, pair[0]);
                    teleportPlayer(online2, pair[1]);
                });
            } catch (Throwable schedulingError) {
                plugin.getLogger().warning("Could not finish 1v1 match scheduling: " + schedulingError.getMessage());
            }
        });
    }

    private void teleportPlayer(Player player, Location destination) {
        UUID id = player.getUniqueId();
        String playerName = player.getName();

        player.getScheduler().execute(
                plugin,
                () -> player.teleportAsync(destination).whenComplete((success, throwable) -> {
                    if (throwable != null || !Boolean.TRUE.equals(success)) {
                        plugin.getLogger().warning("Failed to teleport " + playerName + ".");
                        plugin.getServer().getGlobalRegionScheduler().execute(
                                plugin,
                                () -> requeueIfOnline(id, "teleport-failed", false)
                        );
                    }
                }),
                () -> plugin.getServer().getGlobalRegionScheduler().execute(
                        plugin,
                        () -> requeueIfOnline(id, "teleport-failed", false)
                ),
                1L
        );
    }

    private void requeueIfOnline(UUID id, String messageKey, boolean triggerMatch) {
        Player player = Bukkit.getPlayer(id);
        if (player != null) {
            requeuePlayer(player, messageKey, triggerMatch);
        }
    }

    private void requeuePlayer(Player player, String messageKey, boolean triggerMatch) {
        UUID id = player.getUniqueId();

        if (!queued.add(id)) {
            return;
        }

        queue.add(id);
        startSearchingActionbar(player);

        if (messageKey != null) {
            notifyPlayer(player, messageKey, true);
        }

        if (triggerMatch) {
            plugin.getServer().getGlobalRegionScheduler().execute(plugin, this::tryStartMatch);
        }
    }

    private void sendFoundFeedback(Player player) {
        player.getScheduler().execute(
                plugin,
                () -> {
                    player.sendActionBar(plugin.getLanguageManager().component("found"));
                    SomCore.playConfigured(player, plugin, "sounds.found");
                },
                null,
                1L
        );
    }

    private void notifyPlayer(Player player, String messageKey, boolean errorSound) {
        player.getScheduler().execute(
                plugin,
                () -> {
                    player.sendMessage(plugin.getLanguageManager().component(messageKey));
                    if (errorSound) {
                        SomCore.playConfigured(player, plugin, "sounds.error");
                    }
                },
                null,
                1L
        );
    }

    private World selectMatchWorld() {
        boolean randomWorld = plugin.getConfig().getBoolean(
                "settings.world-selection.random-world",
                false
        );

        if (!randomWorld) {
            String defaultWorldPath = "settings.world-selection.default-world";
            String legacyDefaultWorldPath = "settings.world-selection.default-word";
            String defaultWorldName;

            if (plugin.getConfig().isSet(defaultWorldPath)) {
                defaultWorldName = plugin.getConfig().getString(defaultWorldPath);
            } else if (plugin.getConfig().isSet(legacyDefaultWorldPath)) {
                defaultWorldName = plugin.getConfig().getString(legacyDefaultWorldPath);
                plugin.getLogger().warning(
                        "Deprecated config key 'settings.world-selection.default-word' detected. " +
                                "Please rename it to 'settings.world-selection.default-world'."
                );
            } else {
                defaultWorldName = plugin.getConfig().getString(defaultWorldPath, "world");
            }

            if (defaultWorldName == null || defaultWorldName.isBlank()) {
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
                plugin.getLogger().warning("Configured 1v1 world is not loaded: " + worldName);
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

        ScheduledTask task = player.getScheduler().runAtFixedRate(
                plugin,
                scheduledTask -> {
                    if (!isQueued(id)) {
                        scheduledTask.cancel();
                        actionbarTasks.remove(id);
                        return;
                    }

                    player.sendActionBar(plugin.getLanguageManager().component("searching"));
                },
                () -> actionbarTasks.remove(id),
                1L,
                20L
        );

        if (task != null) {
            actionbarTasks.put(id, task);
        } else {
            queued.remove(id);
            queue.remove(id);
        }
    }

    private void stopSearchingActionbar(UUID id) {
        ScheduledTask task = actionbarTasks.remove(id);
        if (task != null) {
            task.cancel();
        }
    }
}
