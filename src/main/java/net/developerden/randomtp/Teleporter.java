package net.developerden.randomtp;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import redempt.redclaims.claim.ClaimMap;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

public class Teleporter {
    private final Provider<RandomTPConfig> config;
    private final Set<UUID> teleporting = ConcurrentHashMap.newKeySet();

    @Inject
    public Teleporter(Provider<RandomTPConfig> config) {
        this.config = config;
    }

    public CompletableFuture<Location> teleport(@NotNull Player player) {
        if (teleporting.contains(player.getUniqueId())) {
            return CompletableFuture.failedFuture(new AlreadyTeleportingException());
        }
        teleporting.add(player.getUniqueId());
        return findFreeLocation(player.getWorld(), 0)
                .thenCompose(location -> player.teleportAsync(location)
                        .thenApply(v -> {
                            teleporting.remove(player.getUniqueId());
                            return location;
                        }));
    }

    public boolean canTeleport(@NotNull Player player) {
        return !teleporting.contains(player.getUniqueId());
    }

    private CompletableFuture<Location> findFreeLocation(World world, int attempts) {
        if (attempts >= 50) {
            return CompletableFuture.failedFuture(new AllTeleportAttemptsFailedException());
        }
        final ThreadLocalRandom current = ThreadLocalRandom.current();
        final RandomTPConfig tpConfig = this.config.get();
        final int x = current.nextInt(tpConfig.minX(), tpConfig.maxX());
        final int z = current.nextInt(tpConfig.minZ(), tpConfig.maxZ());
        return world.getChunkAtAsyncUrgently(x >> 4, z >> 4)
                .thenApply(c -> {
                    final var chunkSnapshot = c.getChunkSnapshot();
                    int y = chunkSnapshot.getHighestBlockYAt(x & 15, z & 15) - 1; // subtract 1
                    // so block checks and stuff work

                    final Material blockType = chunkSnapshot.getBlockType(x & 15, y, z & 15);
                    if (!blockType.isBlock() || blockType == Material.WATER || blockType == Material.LAVA) {
                        return null;
                    }
                    return new Location(world, x, y + 1.0, z);
                })
                .thenCompose(location -> {
                    if (ClaimMap.getClaim(location) == null) {
                        return CompletableFuture.completedFuture(location);
                    }
                    return findFreeLocation(world, attempts + 1);
                });

    }

    public static class AllTeleportAttemptsFailedException extends RuntimeException {
    }

    public static class AlreadyTeleportingException extends RuntimeException {

    }
}
