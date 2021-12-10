package net.developerden.randomtp;

import me.bristermitten.mittenlib.collections.Maps;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.List;

public class RandomTPCommand implements TabExecutor {
    public static final String PLAYER = "{player}";
    private final Lang lang;
    private final Economy economy;
    private final Teleporter teleporter;
    private final Provider<RandomTPConfig> configProvider;

    @Inject
    public RandomTPCommand(Lang lang, Economy economy, Teleporter teleporter, Provider<RandomTPConfig> configProvider) {
        this.lang = lang;
        this.economy = economy;
        this.teleporter = teleporter;
        this.configProvider = configProvider;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        runCommand(sender, args);
        return true;
    }

    private void runCommand(@NotNull CommandSender sender, @NotNull String[] args) {
        if (!sender.hasPermission("devden.randomtp")) {
            lang.send(sender, RandomTPConfig.MessageConfig::noPermission);
            return;
        }
        final boolean teleportingSelf = args.length == 0;
        if (!(sender instanceof Player) && (teleportingSelf || !sender.hasPermission("devden.randomtp.other"))) {
            lang.send(sender, RandomTPConfig.MessageConfig::notAPlayer);
            return;
        }

        Player target = teleportingSelf ? (Player) sender : Bukkit.getPlayer(args[0]);
        if (target == null) {
            lang.send(sender, RandomTPConfig.MessageConfig::unknownPlayer, Maps.of(PLAYER, args[0]));
            return;
        }
        if (!teleporter.canTeleport(target)) {
            lang.send(sender, RandomTPConfig.MessageConfig::tpFailedAlreadyTeleporting, Maps.of(PLAYER, target.getName()));
            return;
        }
        final int cost = configProvider.get().tpCost();
        if (teleportingSelf && !economy.has(target, cost)) {
            lang.send(target, RandomTPConfig.MessageConfig::tpFailedMoney, Maps.of(PLAYER, target.getName(), "{price}", cost));
            return;
        }

        if (teleportingSelf) {
            economy.withdrawPlayer(target, cost);
        }

        lang.send(sender, RandomTPConfig.MessageConfig::tpStarting, Maps.of(PLAYER, target.getName()));
        teleporter.teleport(target)
                .exceptionally(e -> {
                    lang.send(target, RandomTPConfig.MessageConfig::tpFailed, Maps.of(PLAYER, target.getName()));
                    economy.depositPlayer(target, cost);
                    if (e instanceof Teleporter.AlreadyTeleportingException) {
                        lang.send(target, RandomTPConfig.MessageConfig::tpFailedAlreadyTeleporting, Maps.of(PLAYER, target.getName()));
                    } else if (!(e instanceof Teleporter.AllTeleportAttemptsFailedException)) {
                        e.printStackTrace(); // it's an actually serious error and we should log it
                    }
                    return null;
                })
                .thenAccept(location -> lang.send(target, RandomTPConfig.MessageConfig::tpSuccess,
                        Maps.of("{location}", location.getX() + ", " + location.getY() + ", " + location.getZ())));
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (sender.hasPermission("devden.randomtp.other") && args.length == 0) {
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .toList();
        }
        return null;
    }
}
