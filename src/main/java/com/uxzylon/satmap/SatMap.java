package com.uxzylon.satmap;

import com.uxzylon.satmap.Commands.*;
import org.bukkit.plugin.java.JavaPlugin;

public final class SatMap extends JavaPlugin {

    public static SatMap plugin;

    @Override
    public void onEnable() {
        plugin = this;

        getCommand("satmap").setExecutor(new satMapCommand());

        plugin.getLogger().info("Enabled!");
    }
}
