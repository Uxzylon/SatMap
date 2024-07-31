package com.uxzylon.satmap;

import com.uxzylon.satmap.Commands.*;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.awt.image.BufferedImage;
import java.util.HashMap;

public final class SatMap extends JavaPlugin {

    public static SatMap plugin;
    public static RGBBlockColor rgbBlockColor;
    public static HashMap<Player, Map> playerSatMaps = new HashMap<>();

    @Override
    public void onEnable() {
        plugin = this;

        getCommand("satmap").setExecutor(new satMapCommand());

        plugin.getLogger().info("Enabled!");

        rgbBlockColor = new RGBBlockColor();
    }
}
