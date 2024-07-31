package com.uxzylon.satmap;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

import static com.uxzylon.satmap.SatMap.plugin;

public class Map {
    private final double lat;
    private final double lon;
    private final int widthMeters;
    private final int heightMeters;
    private final double wantedMetersPerPixel;
    public BufferedImage satMap;

    public Map(double lat, double lon, int widthMeters, int heightMeters, double wantedMetersPerPixel) {
        this.lat = lat;
        this.lon = lon;
        this.widthMeters = widthMeters;
        this.heightMeters = heightMeters;
        this.wantedMetersPerPixel = wantedMetersPerPixel;
    }

    public boolean equals(Map map) {
        return this.lat == map.lat && this.lon == map.lon && this.widthMeters == map.widthMeters && this.heightMeters == map.heightMeters && this.wantedMetersPerPixel == map.wantedMetersPerPixel;
    }

    public String toString() {
        return lat + " " + lon + " " + widthMeters + "m " + heightMeters + "m " + wantedMetersPerPixel;
    }

    public boolean isMapGenerated() {
        return satMap != null;
    }

    public void generateMap() {
        int tileSize = 256;
        int zoom = 18;

        int numTiles = 1 << zoom;

        double metersPerPixel = 156543.03392 * Math.cos(lat * Math.PI / 180) / Math.pow(2, zoom);
        double metersPerTile = tileSize * metersPerPixel;

        int tileWidth = (int) Math.ceil(widthMeters / metersPerTile);
        int tileHeight = (int) Math.ceil(heightMeters / metersPerTile);

        double pointX = (tileSize / 2.0 + lon * tileSize / 360.0) * numTiles / tileSize;
        double siny = Math.sin(lat * Math.PI / 180.0);
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

            satMap = op.filter(finalImage, null);

        } catch (IOException e) {
            plugin.getLogger().severe("Error while downloading satellite image: " + e.getMessage());
        }
    }

    public BufferedImage getResizedMap(int width, int height) {
        double scaleX = (double) width / satMap.getWidth();
        double scaleY = (double) height / satMap.getHeight();
        AffineTransform transform = AffineTransform.getScaleInstance(scaleX, scaleY);
        AffineTransformOp op = new AffineTransformOp(transform, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
        return op.filter(satMap, null);
    }

    public boolean saveMap(String filename) {
        File dataFolder = plugin.getDataFolder();
        if (!dataFolder.exists() && !dataFolder.mkdir()) {
            plugin.getLogger().severe("Error while creating data folder.");
            return false;
        }
        File outputFile = new File(dataFolder, filename);
        try {
            ImageIO.write(satMap, "PNG", outputFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Error while saving satellite image: " + e.getMessage());
            return false;
        }

        plugin.getLogger().info("Satellite image saved: " + outputFile.getAbsolutePath());
        return true;
    }
}
