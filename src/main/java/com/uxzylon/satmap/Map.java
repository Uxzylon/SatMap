package com.uxzylon.satmap;

import org.bukkit.Chunk;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

import static com.uxzylon.satmap.Commands.subcommands.Load.displayMapPreview;
import static com.uxzylon.satmap.SatMap.*;
import static com.uxzylon.satmap.Text.sendMessage;

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

    private Block getTargetBlock(Block block, int x, int y, double xRotation, double yRotation, double zRotation) {
        double xRotationRadians = Math.toRadians(xRotation);
        double yRotationRadians = Math.toRadians(yRotation);
        double zRotationRadians = Math.toRadians(zRotation);

        double cosXRotation = Math.cos(xRotationRadians);
        double sinXRotation = Math.sin(xRotationRadians);
        double cosYRotation = Math.cos(yRotationRadians);
        double sinYRotation = Math.sin(yRotationRadians);
        double cosZRotation = Math.cos(zRotationRadians);
        double sinZRotation = Math.sin(zRotationRadians);

        // Adjust coordinates to start from the bottom left corner
        double x1 = x;
        double y1 = y;
        double z1 = 0;

        // Apply mirroring transformation (mirror along the y-axis)
        double xMirror = -x1;
        double yMirror = y1;

        // Apply x-axis rotation transformation
        double y2 = cosXRotation * yMirror - sinXRotation * z1;
        double z2 = sinXRotation * yMirror + cosXRotation * z1;

        // Apply y-axis rotation transformation
        double x3 = cosYRotation * xMirror + sinYRotation * z2;
        double z3 = -sinYRotation * xMirror + cosYRotation * z2;

        // Apply z-axis rotation transformation
        double x4 = cosZRotation * x3 - sinZRotation * y2;
        double y4 = sinZRotation * x3 + cosZRotation * y2;

        return block.getRelative((int) Math.round(x4), (int) Math.round(y4), (int) Math.round(z3));
    }

    public void placeMap(Block block, Player player, double xRotation, double yRotation, double zRotation) {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            BlockFace blockFace = blockFaceFromRotationFacing(xRotation, yRotation, zRotation);
            sendMessage(player, Text.placingMap);
            for (int x = 0; x < satMap.getWidth(); x++) {
                for (int y = satMap.getHeight() - 1; y >= 0; y--) {
                    Color color = new Color(satMap.getRGB(x, y));
                    BlockData blockData = rgbBlockColor.getBlockDataFromColor(color, blockFace);
                    if (blockData != null) {
                        int finalX = x;
                        int finalY = satMap.getHeight() - 1 - y;
                        plugin.getServer().getScheduler().runTask(plugin, () -> {
                            Block targetBlock = getTargetBlock(block, finalX, finalY, xRotation, yRotation, zRotation);
                            Chunk chunk = targetBlock.getChunk();
                            if (!chunk.isLoaded()) {
                                chunk.load();
                            }
                            targetBlock.setBlockData(blockData, false);
                        });
                    }
                }
            }
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                sendMessage(player, Text.satMapPlaced);
            });
        });
    }

    private BlockFace blockFaceFromRotationFacing(double xRotation, double yRotation, double zRotation) {
        // Convert rotations to radians
        double xRad = Math.toRadians(xRotation);
        double yRad = Math.toRadians(yRotation);
        double zRad = Math.toRadians(zRotation);

        // Calculate direction vector
        double x = Math.cos(xRad) * Math.cos(yRad);
        double y = Math.sin(xRad);
        double z = Math.cos(xRad) * Math.sin(yRad);

        // Adjust for the effect of xRotation on zRotation
        if (Math.abs(xRotation) == 90) {
            z = Math.sin(zRad);
        }

        // Determine the BlockFace based on the direction vector
        if (Math.abs(x) > Math.abs(y) && Math.abs(x) > Math.abs(z)) {
            return x > 0 ? BlockFace.SOUTH : BlockFace.NORTH;
        } else if (Math.abs(y) > Math.abs(x) && Math.abs(y) > Math.abs(z)) {
            return y > 0 ? BlockFace.DOWN : BlockFace.UP;
        } else {
            return z > 0 ? BlockFace.EAST : BlockFace.WEST;
        }
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

    public void generateMap(Player player) {
        sendMessage(player, Text.downloadingSatMap);

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

        double finalPointX = pointX;
        double finalPointY = pointY;
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            BufferedImage finalImage = new BufferedImage(tileWidth * tileSize, tileHeight * tileSize, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = finalImage.createGraphics();

            try {
                for (int x = 0; x < tileWidth; x++) {
                    for (int y = 0; y < tileHeight; y++) {
                        String url = "https://mt0.google.com/vt/lyrs=s&?x=" + (int) Math.round(finalPointX + x) + "&y=" + (int) Math.round(finalPointY + y) + "&z=" + zoom;
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

                BufferedImage scaledImage = op.filter(finalImage, null);

                plugin.getServer().getScheduler().runTask(plugin, () -> {
                    satMap = scaledImage;
                    sendMessage(player, Text.satMapDownloaded);
                    playerSatMaps.put(player, this);
                    displayMapPreview(player, this);
                });

            } catch (IOException e) {
                sendMessage(player, Text.errorDownloadingSatMap);
                sendMessage(console, Text.errorDownloadingSatMapDetails, e.getMessage());
            }
        });
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
            sendMessage(console, Text.errorCreateDataFolder);
            return false;
        }
        File outputFile = new File(dataFolder, filename);
        try {
            ImageIO.write(satMap, "PNG", outputFile);
        } catch (IOException e) {
            sendMessage(console, Text.errorSavingSatMap, e.getMessage());
            return false;
        }

        sendMessage(console, Text.satMapSaved, outputFile.getAbsolutePath());
        return true;
    }
}
