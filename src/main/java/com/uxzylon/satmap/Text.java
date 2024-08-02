package com.uxzylon.satmap;

import org.bukkit.command.CommandSender;

import static com.uxzylon.satmap.SatMap.plugin;
import static com.uxzylon.satmap.SatMap.pluginName;

public enum Text {
    title("§6============ §a%s §6============"),
    pluginReloaded("§aPlugin reloaded"),
    pluginEnabled("§aEnabled!"),
    noPermission("§cYou don't have permission to do that"),
    playerOnly("§cThis command can't be run from console"),
    loadDescription("Load a satellite map"),
    placeDescription("Place a satellite map"),
    reloadDescription("Reload the plugin"),
    noSatMapLoaded("§cNo satellite map loaded"),
    satMapPlaced("§aSatellite map placed"),
    downloadingSatMap("§aDownloading satellite map"),
    downloadingClientJar("§aDownloading client jar: %s"),
    downloadedClientJar("§aClient jar downloaded: %s"),
    placingMap("§aPlacing satellite map ..."),
    satMapSaved("§aSatellite map saved: %s"),
    satMapDownloaded("§aSatellite map downloaded"),
    errorDownloadingSatMap("§cError downloading satellite map"),
    errorDownloadingSatMapDetails("§cError downloading satellite map: %s"),
    errorCreateDataFolder("§cError creating data folder"),
    errorSavingSatMap("§cError saving satellite map: %s"),
    failOpenClientJar("§cFailed to open client jar: %s"),
    failCreateClientJar("§cFailed to create client jar: %s"),
    failDownloadClientJar("§cFailed to download client jar: %s"),
    failDownloadVersionManifest("§cFailed to download version manifest: %s"),
    failDownloadVersionJson("§cFailed to download version json: %s"),
    versionNotFound("§cVersion not found: %s"),
    discoveredTextures("§aDiscovered %d block textures!"),
    updatingAvailableSides("§aUpdating available sides ..."),
    fillingBlockDataMap("§aFilling block data map ..."),
    invalidMaterial("§cInvalid material: %s");

    private final String defaultText;

    Text(String text) {
        this.defaultText = text;
    }

    public String getText() {
        String text = plugin.getConfig().getString("Texts." + this.name());
        if (text == null) {
            text = "";
        }
        return "§6[§a" + pluginName + "§6] §r" + text;
    }

    public void addDefault() {
        plugin.getConfig().addDefault("Texts." + this.name(), this.defaultText);
    }

    public static void sendMessage(CommandSender sender, String text) {
        sender.sendMessage(text);
    }

    public static void sendMessage(CommandSender sender, Text text) {
        sender.sendMessage(text.getText());
    }

    public static void sendMessage(CommandSender send, Text text, Object... args) {
        send.sendMessage(String.format(text.getText(), args));
    }

    public static void sendMessage(CommandSender[] senders, String text) {
        for (CommandSender sender : senders) {
            sendMessage(sender, text);
        }
    }

    public static void sendMessage(CommandSender[] senders, Text text) {
        for (CommandSender sender : senders) {
            sendMessage(sender, text);
        }
    }

    public static void sendMessage(CommandSender[] senders, Text text, Object... args) {
        for (CommandSender sender : senders) {
            sendMessage(sender, text, args);
        }
    }
}
