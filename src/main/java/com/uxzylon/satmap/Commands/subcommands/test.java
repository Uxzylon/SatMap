package com.uxzylon.satmap.Commands.subcommands;

import com.uxzylon.satmap.Commands.SubCommand;
import org.bukkit.entity.Player;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Collections;
import java.util.List;

import static com.uxzylon.satmap.SatMap.plugin;

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
        // download satellite image from google maps and save it as an image file for now

        File dataFolder = plugin.getDataFolder();
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }

        int zoom = 18;
        double lat1 = 50.739818238642584;
        double lon1 = 2.6905795080946464;
        int widthMeters = 500;
        int heightMeters = 500;
        double wantedMetersPerPixel = 1.0;

        int tileSize = 256;
        int numTiles = 1 << zoom;

        double metersPerPixel = 156543.03392 * Math.cos(lat1 * Math.PI / 180) / Math.pow(2, zoom);
        double metersPerTile = tileSize * metersPerPixel;

        int tileWidth = (int) Math.ceil(widthMeters / metersPerTile);
        int tileHeight = (int) Math.ceil(heightMeters / metersPerTile);

        double pointX = (tileSize / 2.0 + lon1 * tileSize / 360.0) * numTiles / tileSize;
        double siny = Math.sin(lat1 * Math.PI / 180.0);
        double pointY = (tileSize / 2.0 + 0.5 * Math.log((1 + siny) / (1 - siny)) * (-tileSize / (2 * Math.PI))) * numTiles / tileSize;

        pointX -= tileWidth / 2.0;
        pointY -= tileHeight / 2.0;

        BufferedImage finalImage = new BufferedImage(tileWidth * tileSize, tileHeight * tileSize, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = finalImage.createGraphics();

        try {
            for (int x = 0; x < tileWidth; x++) {
                for (int y = 0; y < tileHeight; y++) {
                    String url = "https://mt0.google.com/vt/lyrs=s&?x=" + (int) Math.round(pointX + x) + "&y=" + (int) Math.round(pointY + y) + "&z=" + zoom;
                    URLConnection connection = new URL(url).openConnection();
                    connection.setRequestProperty("User-Agent", "Mozilla/5.0");
                    connection.connect();

                    BufferedImage tileImage = ImageIO.read(connection.getInputStream());
                    g2d.drawImage(tileImage, x * tileSize, y * tileSize, null);
                }
            }
            g2d.dispose();

            int newWidth = (int) Math.round(widthMeters * wantedMetersPerPixel);
            int newHeight = (int) Math.round(heightMeters * wantedMetersPerPixel);

            // Calculate the scale factors
            double scaleX = (double) newWidth / finalImage.getWidth();
            double scaleY = (double) newHeight / finalImage.getHeight();

            AffineTransform transform = AffineTransform.getScaleInstance(scaleX, scaleY);
            AffineTransformOp op = new AffineTransformOp(transform, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);

            finalImage = op.filter(finalImage, null);

            File outputFile = new File(dataFolder, "satellite_final.png");
            ImageIO.write(finalImage, "PNG", outputFile);

            plugin.getLogger().info("Final satellite image created: " + outputFile.getAbsolutePath());
        } catch (IOException e) {
            plugin.getLogger().severe("Error while downloading satellite image: " + e.getMessage());
        }
    }
}
