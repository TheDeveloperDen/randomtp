package net.developerden.randomtp;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;

public class HooksModule extends AbstractModule {
    @Provides
    public Economy getEconomy() {
        final var registration = Bukkit.getServicesManager().getRegistration(Economy.class);
        if (registration == null) {
            throw new IllegalStateException("Economy plugin not found");
        }
        return registration.getProvider();
    }
}
