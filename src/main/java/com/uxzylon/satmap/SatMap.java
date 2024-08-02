package com.uxzylon.satmap;

import com.uxzylon.satmap.Commands.*;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;

import static com.uxzylon.satmap.Text.sendMessage;

public final class SatMap extends JavaPlugin {

    public static String pluginName = "SatMap";
    public static SatMap plugin;
    public static CommandSender console;
    public static RGBBlockColor rgbBlockColor;
    public static HashMap<Player, Map> playerSatMaps = new HashMap<>();

    @Override
    public void onEnable() {
        plugin = this;
        console = this.getServer().getConsoleSender();

        createConfig();

        getCommand("satmap").setExecutor(new satMapCommand());

        rgbBlockColor = new RGBBlockColor();

        sendMessage(console, Text.pluginEnabled);
    }

    private void createConfig() {
        getConfig().options().copyDefaults(true);

        getConfig().addDefault("forbidden-blocks", new String[] {
            "CHAIN_COMMAND_BLOCK",
            "REPEATING_COMMAND_BLOCK",
            "COMMAND_BLOCK",
            "SPAWNER",
            "TRIAL_SPAWNER",
            "SHULKER_BOX",
            "WHITE_SHULKER_BOX",
            "LIGHT_GRAY_SHULKER_BOX",
            "GRAY_SHULKER_BOX",
            "BLACK_SHULKER_BOX",
            "BROWN_SHULKER_BOX",
            "RED_SHULKER_BOX",
            "ORANGE_SHULKER_BOX",
            "YELLOW_SHULKER_BOX",
            "LIME_SHULKER_BOX",
            "GREEN_SHULKER_BOX",
            "CYAN_SHULKER_BOX",
            "LIGHT_BLUE_SHULKER_BOX",
            "BLUE_SHULKER_BOX",
            "PURPLE_SHULKER_BOX",
            "MAGENTA_SHULKER_BOX",
            "PINK_SHULKER_BOX",
            "JIGSAW",
            "STRUCTURE_BLOCK",
            "VAULT",
            "MANGROVE_ROOTS"
        });

        for (Text text : Text.values()) {
            text.addDefault();
        }

        saveConfig();
    }
}
