package net.developerden.randomtp;

import me.bristermitten.mittenlib.MittenLib;
import me.bristermitten.mittenlib.minimessage.MiniMessageModule;
import me.bristermitten.mittenlib.watcher.FileWatcherModule;
import org.bukkit.plugin.java.JavaPlugin;

public class RandomTP extends JavaPlugin {
    @Override
    public void onEnable() {
        var injector = MittenLib.withDefaults(this)
                .addModule(new FileWatcherModule())
                .addConfigModules(RandomTPConfig.CONFIG)
                .addModule(new MiniMessageModule())
                .build();


    }
}
