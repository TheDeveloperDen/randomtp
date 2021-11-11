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
        if (!sender.hasPermission("devden.randomtp")) {
            lang.send(sender, MessageConfig::noPermission);
            return true;
        }
        final boolean teleportingSelf = args.length == 0;
        if (!(sender instanceof Player) && (teleportingSelf || !sender.hasPermission("devden.randomtp.other"))) {
            lang.send(sender, MessageConfig::notAPlayer);
            return true;
        }

        Player target = teleportingSelf ? (Player) sender : Bukkit.getPlayer(args[0]);
        if (target == null) {
            lang.send(sender, MessageConfig::unknownPlayer, Maps.of(PLAYER, args[0]));
            return true;
        }
        if (teleportingSelf && !economy.has(target, configProvider.get().tpCost())) {
            lang.send(target, MessageConfig::tpFailedMoney, Maps.of(PLAYER, target.getName()));
            return true;
        }

        if (teleportingSelf) {
            economy.withdrawPlayer(target, configProvider.get().tpCost());
        }

        lang.send(sender, MessageConfig::tpStarting, Maps.of(PLAYER, target.getName()));
        teleporter.teleport(target)
                .exceptionally(e -> {
                    lang.send(target, MessageConfig::tpFailed, Maps.of(PLAYER, target.getName()));
                    economy.depositPlayer(target, configProvider.get().tpCost());
                    if (!(e instanceof Teleporter.AllTeleportAttemptsFailedException)) {
                        e.printStackTrace(); // it's an actually serious error and we should log it
                    }
                    return null;
                })
                .thenAccept(location -> lang.send(target, MessageConfig::tpSuccess,
                        Maps.of("{location}", location.getX() + ", " + location.getY() + ", " + location.getZ())));

        return true;
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
