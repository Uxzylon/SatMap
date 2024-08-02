package com.uxzylon.satmap.Commands.subcommands;

import com.bergerkiller.bukkit.common.map.MapDisplay;
import com.bergerkiller.bukkit.common.utils.ItemUtil;
import com.uxzylon.satmap.Commands.SubCommand;
import com.uxzylon.satmap.Map;
import com.uxzylon.satmap.SatDisplay;
import com.uxzylon.satmap.Text;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;
import java.util.List;

import static com.uxzylon.satmap.SatMap.playerSatMaps;

public class Load extends SubCommand {

    @Override
    public String getName() {
        return "load";
    }

    @Override
    public String getDescription() {
        return Text.loadDescription.getText();
    }

    @Override
    public String getSyntax() {
        return "/satmap load <lat> <lon> <width> <height> <ratio>";
    }

    @Override
    public String permission() {
        return "satmap.command.load";
    }

    @Override
    public boolean canRunConsole() {
        return false;
    }

    @Override
    public int getMinArgs() {
        return 6;
    }

    @Override
    public List<String> getSubcommandArguments(Player player, String[] args) {
        return switch (args.length) {
            case 2 -> Collections.singletonList("<lat>");
            case 3 -> Collections.singletonList("<lon>");
            case 4 -> Collections.singletonList("<width>");
            case 5 -> Collections.singletonList("<height>");
            case 6 -> Collections.singletonList("<ratio>");
            default -> Collections.emptyList();
        };
    }

    @Override
    public void perform(Player player, String[] args) {
//        double lat1 = 50.000000000000001;
//        double lon1 = 2.6900000000000001;
//        int widthMeters = 500;
//        int heightMeters = 500;
//        double wantedMetersPerPixel = 1.0;

        double lat = Double.parseDouble(args[1]);
        double lon = Double.parseDouble(args[2]);
        int width = Integer.parseInt(args[3]);
        int height = Integer.parseInt(args[4]);
        double ratio = Double.parseDouble(args[5]);

        Map map = new Map(lat, lon, width, height, ratio);

        if (playerSatMaps.containsKey(player)) {
            Map playerMap = playerSatMaps.get(player);
            if (playerMap.equals(map)) {
                map = playerMap;
            }
        }

        if (map.isMapGenerated()) {
            displayMapPreview(player, map);
        } else {
            map.generateMap(player);
        }
    }

    public static void displayMapPreview(Player player, Map map) {
        ItemStack item = MapDisplay.createMapItem(SatDisplay.class);
        ItemUtil.setDisplayName(item, map.toString());
        player.getInventory().setItemInMainHand(item);
    }
}
