package com.uxzylon.satmap.Commands.subcommands;

import com.uxzylon.satmap.Commands.SubCommand;
import com.uxzylon.satmap.Map;
import com.uxzylon.satmap.Text;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

import static com.uxzylon.satmap.SatMap.*;
import static com.uxzylon.satmap.Text.sendMessage;

public class Place extends SubCommand {

    @Override
    public String getName() {
        return "place";
    }

    @Override
    public String getDescription() {
        return Text.placeDescription.getText();
    }

    @Override
    public String getSyntax() {
        return "/satmap place <x_rotation> <y_rotation> <z_rotation>";
    }

    @Override
    public String permission() {
        return "satmap.command.place";
    }

    @Override
    public boolean canRunConsole() {
        return false;
    }

    @Override
    public int getMinArgs() {
        return 4;
    }

    @Override
    public List<String> getSubcommandArguments(Player player, String[] args) {
        if (args.length == 2) {
            return Collections.singletonList("90");
        } else if (args.length == 3) {
            return Collections.singletonList("0");
        } else if (args.length == 4) {
            return Collections.singletonList("0");
        }
        return Collections.emptyList();
    }

    @Override
    public void perform(Player player, String[] args) {
        Location location = player.getLocation();

        double xRotation = Double.parseDouble(args[1]);
        double yRotation = Double.parseDouble(args[2]);
        double zRotation = Double.parseDouble(args[3]);

        Map map = playerSatMaps.get(player);

        if (map == null) {
            sendMessage(player, Text.noSatMapLoaded);
            return;
        }

        map.placeMap(location.getBlock(), player, xRotation, yRotation, zRotation);
    }
}
