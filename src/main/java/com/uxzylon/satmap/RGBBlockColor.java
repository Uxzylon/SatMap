package com.uxzylon.satmap;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.codec.digest.DigestUtils;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static com.uxzylon.satmap.SatMap.*;
import static com.uxzylon.satmap.Text.sendMessage;

public class RGBBlockColor {
    public List<Texture> textures;
    public List<Material> forbiddenMaterials = new ArrayList<>();

    public RGBBlockColor() {
        updateForbiddenMaterials();
        parseTextures(downloadClientJar());
    }

    public void updateForbiddenMaterials() {
        forbiddenMaterials.clear();
        for (String materialName : plugin.getConfig().getStringList("forbidden-blocks")) {
            try {
                Material material = Material.valueOf(materialName);
                forbiddenMaterials.add(material);
            } catch (IllegalArgumentException e) {
                sendMessage(console, Text.invalidMaterial, materialName);
            }
        }
    }

    private void parseTextures(File clientJar) {
        List<Texture> textures = new ArrayList<>();
        try (ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(clientJar))) {
            ZipEntry zipEntry;
            while ((zipEntry = zipInputStream.getNextEntry()) != null) {
                if (zipEntry.getName().startsWith("assets/minecraft/textures/block/") && zipEntry.getName().endsWith(".png")) {
                    String textureName = zipEntry.getName().replace("assets/minecraft/textures/block/", "").replace(".png", "");
                    if (textureName.contains("/")) {
                        continue;
                    }
                    // get the texture image
                    BufferedImage textureImage = ImageIO.read(zipInputStream);
                    if (textureImage == null) {
                        continue;
                    }

                    Texture newTexture = new Texture(textureName, textureImage);
                    if (newTexture.isValid()) {
                        textures.add(newTexture);
                    }
                }
            }
        } catch (IOException e) {
            sendMessage(console, Text.failOpenClientJar, e.getMessage());
        }

        sendMessage(console, Text.discoveredTextures, textures.size());

        this.textures = textures;
        processTextures();
    }

    private File downloadClientJar() {
        // grab server version
        String version = plugin.getServer().getVersion();
        version = version.substring(version.indexOf("MC: ") + 4, version.length() - 1);  // 1.21

        boolean needDownload = true;
        File clientJar = new File(plugin.getDataFolder(), version + ".jar");
        if (clientJar.exists()) {
            // check if it can be opened
            try (ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(clientJar))) {
                ZipEntry zipEntry;
                while ((zipEntry = zipInputStream.getNextEntry()) != null) {
                    if (zipEntry.getName().equals("assets/minecraft/textures/block/stone.png")) {
                        needDownload = false;
                        break;
                    }
                }
            } catch (IOException e) {
                sendMessage(console, Text.failOpenClientJar, e.getMessage());
            }
        }

        if (needDownload) {
            downloadClientJar(version);
        }

        return clientJar;
    }

    private void downloadClientJar(String version) {
        // download version_manifest.json
        String versionManifestUrl = "https://launchermeta.mojang.com/mc/game/version_manifest.json";
        JsonObject versionManifest = null;
        try {
            InputStream versionManifestStream = new URL(versionManifestUrl).openStream();
            versionManifest = JsonParser.parseReader(new InputStreamReader(versionManifestStream)).getAsJsonObject();
        } catch (IOException e) {
            sendMessage(console, Text.failDownloadVersionManifest, e.getMessage());
        }

        if (versionManifest == null) {
            return;
        }

        // get version url
        String versionUrl = null;
        for (int i = 0; i < versionManifest.getAsJsonArray("versions").size(); i++) {
            JsonObject versionObject = versionManifest.getAsJsonArray("versions").get(i).getAsJsonObject();
            if (versionObject.get("id").getAsString().equals(version)) {
                versionUrl = versionObject.get("url").getAsString();
                break;
            }
        }

        if (versionUrl == null) {
            sendMessage(console, Text.versionNotFound, version);
            return;
        }

        // download version json
        JsonObject versionJson = null;
        try {
            InputStream versionJsonStream = new URL(versionUrl).openStream();
            versionJson = JsonParser.parseReader(new InputStreamReader(versionJsonStream)).getAsJsonObject();
        } catch (IOException e) {
            sendMessage(console, Text.failDownloadVersionJson, e.getMessage());
        }

        if (versionJson == null) {
            return;
        }

        // get client jar url
        String clientJarUrl = versionJson.getAsJsonObject("downloads").getAsJsonObject("client").get("url").getAsString();
        String clientJarSha1 = versionJson.getAsJsonObject("downloads").getAsJsonObject("client").get("sha1").getAsString();

        // download client jar
        File clientJar = new File(plugin.getDataFolder(), version + ".jar");
        if (clientJar.exists()) {
            clientJar.delete();
        }

        try {
            clientJar.createNewFile();
        } catch (IOException e) {
            sendMessage(console, Text.failCreateClientJar, e.getMessage());
            return;
        }

        sendMessage(console, Text.downloadingClientJar, clientJarUrl);
        try {
            BufferedInputStream in = new BufferedInputStream(new URL(clientJarUrl).openStream());
            FileOutputStream fileOutputStream = new FileOutputStream(clientJar);
            byte[] dataBuffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
                fileOutputStream.write(dataBuffer, 0, bytesRead);
            }
        } catch (IOException e) {
            sendMessage(console, Text.failDownloadClientJar, e.getMessage());
            return;
        }

        try {
            InputStream clientJarStream = new URL(clientJarUrl).openStream();
            String sha1 = DigestUtils.sha1Hex(clientJarStream);
            if (sha1.equals(clientJarSha1)) {
                sendMessage(console, Text.downloadedClientJar, clientJar.getAbsolutePath());
            }
        } catch (IOException e) {
            sendMessage(console, Text.failDownloadClientJar, e.getMessage());
        }
    }

    private void updateTexturesAvailableSides() {
        HashMap<Material, Texture.Side[]> textureSides = new HashMap<>();
        for (Texture texture : textures) {
            textureSides.putIfAbsent(texture.material, new Texture.Side[0]);
            Texture.Side side = texture.side;
            Texture.Side[] sides = textureSides.get(texture.material);
            Texture.Side[] newSides = new Texture.Side[sides.length + 1];
            System.arraycopy(sides, 0, newSides, 0, sides.length);
            newSides[sides.length] = side;
            textureSides.put(texture.material, newSides);
        }

        for (Texture texture : textures) {
            texture.addAvailableSides(textureSides.get(texture.material));
        }
    }

    private void fillTexturesBlockDataMap() {
        textures.forEach(Texture::fillBlockDataMap);
    }

    private void processTextures() {
        sendMessage(console, Text.updatingAvailableSides);
        updateTexturesAvailableSides();
        sendMessage(console, Text.fillingBlockDataMap);
        fillTexturesBlockDataMap();
    }

    public BlockData getBlockDataFromColor(Color color, BlockFace direction) {
        Texture closestTexture = null;
        double closestDistance = Double.MAX_VALUE;
        for (Texture texture : textures) {
            double distance = texture.colorDistance(color);
            if (distance < closestDistance && texture.blockDataMap.get(direction) != null && !forbiddenMaterials.contains(texture.material)) {
                closestTexture = texture;
                closestDistance = distance;
            }
        }
        if (closestTexture == null) {
            return null;
        }
        return closestTexture.blockDataMap.get(direction);
    }
}
