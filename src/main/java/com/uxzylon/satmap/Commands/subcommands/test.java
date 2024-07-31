package com.uxzylon.satmap.Commands.subcommands;

import com.bergerkiller.bukkit.common.map.MapDisplay;
import com.bergerkiller.bukkit.common.utils.ItemUtil;
import com.uxzylon.satmap.Commands.SubCommand;
import com.uxzylon.satmap.Map;
import com.uxzylon.satmap.SatDisplay;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;
import java.util.List;

import static com.uxzylon.satmap.SatMap.*;

public class test extends SubCommand {

    @Override
    public String getName() {
        return "test";
    }

    @Override
    public String getDescription() {
        return "test";
    }

    @Override
    public String getSyntax() {
        return "/satmap test";
    }

    @Override
    public String permission() {
        return "satmap.command.test";
    }

    @Override
    public boolean canRunConsole() {
        return false;
    }

    @Override
    public List<String> getSubcommandArguments(Player player, String[] args) {
        return Collections.emptyList();
    }

    @Override
    public void perform(Player player, String[] args) {
        double lat1 = 50.739818238642584;
        double lon1 = 2.6905795080946464;
        int widthMeters = 500;
        int heightMeters = 500;
        double wantedMetersPerPixel = 1.0;

        Map map = new Map(lat1, lon1, widthMeters, heightMeters, wantedMetersPerPixel);

        if (playerSatMaps.containsKey(player)) {
            Map playerMap = playerSatMaps.get(player);
            if (playerMap.equals(map)) {
                map = playerMap;
            }
        }

        if (!map.isMapGenerated()) {
            map.generateMap();
            playerSatMaps.put(player, map);
        }

        ItemStack item = MapDisplay.createMapItem(SatDisplay.class);
        ItemUtil.setDisplayName(item, map.toString());
        player.getInventory().setItemInMainHand(item);
    }
}
