package net.developerden.randomtp;

import me.bristermitten.mittenlib.collections.Maps;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.inject.Inject;
import java.util.List;

public class RandomTPCommand implements TabExecutor {
    private final Lang lang;
    private final Teleporter teleporter;

    @Inject
    public RandomTPCommand(Lang lang, Teleporter teleporter) {
        this.lang = lang;
        this.teleporter = teleporter;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("devden.randomtp")) {
            lang.send(sender, MessageConfig::noPermission);
            return true;
        }
        if (!(sender instanceof Player) && args.length == 0) {
            lang.send(sender, MessageConfig::notAPlayer);
            return true;
        }

        Player target = args.length == 0 ? (Player) sender : Bukkit.getPlayer(args[0]);
        if (target == null) {
            lang.send(sender, MessageConfig::unknownPlayer, Maps.of("{player}", args[0]));
            return true;
        }

        lang.send(sender, MessageConfig::tpStarting, Maps.of("{player}", target.getName()));
        teleporter.teleport(target)
                .exceptionally(e -> {
                    lang.send(target, MessageConfig::tpFailed, Maps.of("{player}", target.getName()));
                    e.printStackTrace();
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
