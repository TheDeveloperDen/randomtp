package net.developerden.randomtp;

import me.bristermitten.mittenlib.MittenLib;
import me.bristermitten.mittenlib.minimessage.MiniMessageModule;
import me.bristermitten.mittenlib.watcher.FileWatcherModule;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public class RandomTP extends JavaPlugin {
    @Override
    public void onEnable() {

        var injector = MittenLib.withDefaults(this)
                .addModule(new FileWatcherModule())
                .addConfigModules(RandomTPConfig.CONFIG)
                .addModule(new MiniMessageModule())
                .build();


        final RandomTPCommand command = injector.getInstance(RandomTPCommand.class);
        final PluginCommand rtp = getCommand("randomtp");
        if (rtp == null) {
            getSLF4JLogger().error("what");
            return;
        }
        rtp.setExecutor(command);
        rtp.setTabCompleter(command);
    }
}
