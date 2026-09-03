package me.studio;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;

import java.util.EnumSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;

public final class TeleporteCore {

    private static final int MAX_TRIES = 40;
    private static final int NETHER_MAX_Y = 125;

    private static final Set<Material> UNSAFE_GROUND = EnumSet.of(
            Material.WATER,
            Material.LAVA,
            Material.MAGMA_BLOCK,
            Material.CACTUS,
            Material.CAMPFIRE,
            Material.SOUL_CAMPFIRE,
            Material.SWEET_BERRY_BUSH,
            Material.FIRE,
            Material.SOUL_FIRE
    );

    private TeleporteCore() {
    }

    public static CompletableFuture<Location[]> createPairAsync(
            Main1v1 plugin,
            World world,
            int radius,
            boolean faceToFace,
            double distance
    ) {
        int safeRadius = Math.max(0, radius);
        double safeDistance = Math.max(2.0, distance);

        return findSafeBaseAsync(plugin, world, safeRadius).thenCompose(base -> {
            if (base == null) {
                return CompletableFuture.completedFuture(null);
            }

            ThreadLocalRandom random = ThreadLocalRandom.current();
            Vector dir = new Vector(random.nextDouble(-1.0, 1.0), 0, random.nextDouble(-1.0, 1.0));
            if (dir.lengthSquared() < 0.0001) {
                dir = new Vector(1, 0, 0);
            }
            dir.normalize();

            double half = safeDistance / 2.0;
            Location a = base.clone().add(dir.clone().multiply(half));
            Location b = base.clone().add(dir.clone().multiply(-half));

            CompletableFuture<Location> fa = safeAtAsync(plugin, world, a.getBlockX(), a.getBlockZ());
            CompletableFuture<Location> fb = safeAtAsync(plugin, world, b.getBlockX(), b.getBlockZ());

            return fa.thenCombine(fb, (aa, bb) -> {
                if (aa == null || bb == null) {
                    return null;
                }

                if (faceToFace) {
                    lookAt(aa, bb);
                    lookAt(bb, aa);
                }

                return new Location[]{aa, bb};
            });
        });
    }

    private static CompletableFuture<Location> findSafeBaseAsync(Main1v1 plugin, World world, int radius) {
        Location spawn = world.getSpawnLocation();
        return tryFindAsync(plugin, world, radius, spawn.getBlockX(), spawn.getBlockZ(), 1);
    }

    private static CompletableFuture<Location> tryFindAsync(
            Main1v1 plugin,
            World world,
            int radius,
            int spawnX,
            int spawnZ,
            int attempt
    ) {
        if (attempt > MAX_TRIES) {
            return safeAtAsync(plugin, world, spawnX, spawnZ);
        }

        ThreadLocalRandom random = ThreadLocalRandom.current();
        int x = spawnX + random.nextInt(-radius, radius + 1);
        int z = spawnZ + random.nextInt(-radius, radius + 1);

        return safeAtAsync(plugin, world, x, z).thenCompose(location -> {
            if (location != null) {
                return CompletableFuture.completedFuture(location);
            }

            return tryFindAsync(plugin, world, radius, spawnX, spawnZ, attempt + 1);
        });
    }

    private static CompletableFuture<Location> safeAtAsync(Main1v1 plugin, World world, int x, int z) {
        return world.getChunkAtAsync(x >> 4, z >> 4, true).thenCompose(chunk -> {
            CompletableFuture<Location> result = new CompletableFuture<>();
            Location regionLocation = new Location(world, x + 0.5, world.getMinHeight() + 1, z + 0.5);

            try {
                plugin.getServer().getRegionScheduler().execute(plugin, regionLocation, () -> {
                    try {
                        result.complete(safeAt(world, x, z));
                    } catch (Throwable throwable) {
                        result.completeExceptionally(throwable);
                    }
                });
            } catch (Throwable throwable) {
                result.completeExceptionally(throwable);
            }

            return result;
        });
    }

    private static Location safeAt(World world, int x, int z) {
        int maxSafeGroundY = world.getMaxHeight() - 3;
        int startY;

        if (world.getEnvironment() == World.Environment.NETHER) {
            startY = Math.min(NETHER_MAX_Y, maxSafeGroundY);
        } else {
            startY = Math.min(world.getHighestBlockYAt(x, z), maxSafeGroundY);
        }

        for (int y = startY; y >= world.getMinHeight(); y--) {
            Block ground = world.getBlockAt(x, y, z);
            Block feet = world.getBlockAt(x, y + 1, z);
            Block head = world.getBlockAt(x, y + 2, z);

            Material groundType = ground.getType();

            if (!groundType.isSolid()) {
                continue;
            }

            if (UNSAFE_GROUND.contains(groundType)) {
                continue;
            }

            if (!feet.isPassable() || !head.isPassable()) {
                continue;
            }

            if (feet.isLiquid() || head.isLiquid()) {
                continue;
            }

            return new Location(world, x + 0.5, y + 1, z + 0.5);
        }

        return null;
    }

    private static void lookAt(Location from, Location to) {
        Vector dir = to.toVector().subtract(from.toVector());

        double dx = dir.getX();
        double dz = dir.getZ();
        double dy = dir.getY();

        float yaw = (float) Math.toDegrees(Math.atan2(-dx, dz));
        double xz = Math.sqrt(dx * dx + dz * dz);
        float pitch = (float) Math.toDegrees(Math.atan2(-dy, xz));

        from.setYaw(yaw);
        from.setPitch(pitch);
    }
}
