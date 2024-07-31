package com.uxzylon.satmap;

import com.bergerkiller.bukkit.common.map.MapDisplay;
import com.bergerkiller.bukkit.common.map.MapTexture;
import org.bukkit.entity.Player;

import java.awt.image.BufferedImage;

import static com.uxzylon.satmap.SatMap.playerSatMaps;

public class SatDisplay extends MapDisplay {
    @Override
    public void onAttached() {
        Player player = this.getOwners().get(0);

        Map map = playerSatMaps.get(player);
        if (map == null) {
            return;
        }

        BufferedImage image = map.getResizedMap(this.getWidth(), this.getHeight());

        MapTexture texture = MapTexture.fromImage(image);
        this.getLayer().draw(texture, 0, 0);
    }
}
