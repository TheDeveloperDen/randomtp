package net.developerden.randomtp;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import redempt.redclaims.claim.ClaimMap;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;

public class Teleporter {
    private final Provider<RandomTPConfig> config;

    @Inject
    public Teleporter(Provider<RandomTPConfig> config) {
        this.config = config;
    }

    public CompletableFuture<Location> teleport(@NotNull Player player) {
        return findFreeLocation(player.getWorld(), 0)
                .thenCompose(location ->
                        player.teleportAsync(location)
                                .thenApply(v -> location));
    }

    private CompletableFuture<Location> findFreeLocation(World world, int attempts) {
        if (attempts >= 50) {
            return CompletableFuture.failedFuture(new AllTeleportAttemptsFailedException());
        }
        final ThreadLocalRandom current = ThreadLocalRandom.current();
        final RandomTPConfig tpConfig = this.config.get();
        final int x = current.nextInt(tpConfig.minX(), tpConfig.maxX());
        final int z = current.nextInt(tpConfig.minZ(), tpConfig.maxZ());
        return world.getChunkAtAsync(x >> 4, z >> 4)
                .thenApply(c -> c.getChunkSnapshot().getHighestBlockYAt(x & 15, z & 15))
                .thenApply(y -> new Location(world, x, y, z))
                .thenCompose(location -> {
                    if (ClaimMap.getClaim(location) != null) {
                        return CompletableFuture.completedFuture(location);
                    }
                    return findFreeLocation(world, attempts + 1);
                });

    }

    public static class AllTeleportAttemptsFailedException extends RuntimeException {
    }
}
