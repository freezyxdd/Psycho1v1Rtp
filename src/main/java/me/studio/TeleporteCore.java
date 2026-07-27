package me.studio;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;

import java.util.EnumSet;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public final class TeleporteCore {

    private static final Random RNG = new Random();
    private static final int MAX_TRIES = 40;
    private static final int NETHER_MAX_Y = 125;

    private static final Set<Material> UNSAFE_GROUND = EnumSet.of(
            Material.WATER, Material.LAVA,
            Material.MAGMA_BLOCK, Material.CACTUS, Material.CAMPFIRE, Material.SOUL_CAMPFIRE,
            Material.SWEET_BERRY_BUSH, Material.FIRE, Material.SOUL_FIRE
    );

    private TeleporteCore() {}

    public static CompletableFuture<Location[]> createPairAsync(World world, int radius, boolean faceToFace, double distance) {
        return findSafeBaseAsync(world, radius).thenCompose(base -> {
            if (base == null) return CompletableFuture.completedFuture(null);

            if (!faceToFace) {
                return CompletableFuture.completedFuture(new Location[]{ base.clone(), base.clone() });
            }

            Vector dir = new Vector(RNG.nextDouble() * 2 - 1, 0, RNG.nextDouble() * 2 - 1);
            if (dir.lengthSquared() < 0.0001) dir = new Vector(1, 0, 0);
            dir.normalize();

            double half = distance / 2.0;

            Location a = base.clone().add(dir.clone().multiply(half));
            Location b = base.clone().add(dir.clone().multiply(-half));

            CompletableFuture<Location> fa = snapToSafeAsync(world, a);
            CompletableFuture<Location> fb = snapToSafeAsync(world, b);

            return fa.thenCombine(fb, (aa, bb) -> {
                if (aa == null || bb == null) return null;
                lookAt(aa, bb);
                lookAt(bb, aa);
                return new Location[]{ aa, bb };
            });
        });
    }

    private static CompletableFuture<Location> findSafeBaseAsync(World world, int radius) {
        return tryFindAsync(world, radius, 1);
    }

    private static CompletableFuture<Location> tryFindAsync(World world, int radius, int attempt) {
        if (attempt > MAX_TRIES) {
            Location spawn = world.getSpawnLocation().clone();
            spawn.setX(spawn.getBlockX() + 0.5);
            spawn.setZ(spawn.getBlockZ() + 0.5);
            return CompletableFuture.completedFuture(spawn);
        }

        Location spawn = world.getSpawnLocation();

        int x = spawn.getBlockX() + RNG.nextInt(radius * 2 + 1) - radius;
        int z = spawn.getBlockZ() + RNG.nextInt(radius * 2 + 1) - radius;

        return world.getChunkAtAsync(x >> 4, z >> 4, true).thenCompose(chunk -> {
            Location loc = safeAt(world, x, z);
            if (loc != null) return CompletableFuture.completedFuture(loc);
            return tryFindAsync(world, radius, attempt + 1);
        });
    }

    private static CompletableFuture<Location> snapToSafeAsync(World world, Location near) {
        int x = near.getBlockX();
        int z = near.getBlockZ();
        return world.getChunkAtAsync(x >> 4, z >> 4, true).thenApply(chunk -> safeAt(world, x, z));
    }

    private static Location safeAt(World world, int x, int z) {
        int startY;

        if (world.getEnvironment() == World.Environment.NETHER) {
            // Começa abaixo do teto de bedrock do Nether
            startY = Math.min(NETHER_MAX_Y, world.getMaxHeight() - 3);
        } else {
            // Nos outros mundos, começa pelo bloco mais alto
            startY = world.getHighestBlockYAt(x, z);
        }

        for (int y = startY; y >= world.getMinHeight(); y--) {
            Block ground = world.getBlockAt(x, y, z);
            Block feet = world.getBlockAt(x, y + 1, z);
            Block head = world.getBlockAt(x, y + 2, z);

            Material groundType = ground.getType();

            // O jogador precisa ficar sobre um bloco sólido
            if (!groundType.isSolid()) {
                continue;
            }

            // Evita blocos perigosos
            if (UNSAFE_GROUND.contains(groundType)) {
                continue;
            }

            // Precisa haver espaço para os pés e para a cabeça
            if (!feet.isPassable() || !head.isPassable()) {
                continue;
            }

            // Evita nascer dentro de líquidos
            if (feet.isLiquid() || head.isLiquid()) {
                continue;
            }

            return new Location(
                    world,
                    x + 0.5,
                    y + 1,
                    z + 0.5
            );
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
