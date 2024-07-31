package com.uxzylon.satmap;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.codec.digest.DigestUtils;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import com.uxzylon.satmap.Texture.Side;

import java.io.*;
import java.net.URL;
import java.util.HashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static com.uxzylon.satmap.Texture.getBlockOrientationType;
import static com.uxzylon.satmap.SatMap.plugin;

public class RGBBlockColor {

    public String[] textures;
    public HashMap<String, BlockData> blockDataMap = new HashMap<>();

    public RGBBlockColor() {
        File clientJar = downloadClientJar();
        mapTexturesToBlockData(clientJar);
    }

    private void mapTexturesToBlockData(File clientJar) {
        try (ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(clientJar))) {
            ZipEntry zipEntry;
            while ((zipEntry = zipInputStream.getNextEntry()) != null) {
                if (zipEntry.getName().startsWith("assets/minecraft/textures/block/") && zipEntry.getName().endsWith(".png")) {
                    String texture = zipEntry.getName().replace("assets/minecraft/textures/block/", "").replace(".png", "");
                    if (texture.contains("/")) {
                        continue;
                    }
                    if (textures == null) {
                        textures = new String[]{texture};
                    } else {
                        String[] newTextures = new String[textures.length + 1];
                        System.arraycopy(textures, 0, newTextures, 0, textures.length);
                        newTextures[textures.length] = texture;
                        textures = newTextures;
                    }
                }
            }
            plugin.getLogger().info("Discovered " + textures.length + " block textures!");
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to open client jar: " + e.getMessage());
        }
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
                plugin.getLogger().warning("Failed to open client jar: " + e.getMessage());
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
            plugin.getLogger().warning("Failed to download version manifest: " + e.getMessage());
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
            plugin.getLogger().warning("Version not found: " + version);
            return;
        }

        // download version json
        JsonObject versionJson = null;
        try {
            InputStream versionJsonStream = new URL(versionUrl).openStream();
            versionJson = JsonParser.parseReader(new InputStreamReader(versionJsonStream)).getAsJsonObject();
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to download version json: " + e.getMessage());
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
            plugin.getLogger().warning("Failed to create client jar file: " + e.getMessage());
            return;
        }

        plugin.getLogger().info("Downloading client jar: " + clientJarUrl);
        try {
            BufferedInputStream in = new BufferedInputStream(new URL(clientJarUrl).openStream());
            FileOutputStream fileOutputStream = new FileOutputStream(clientJar);
            byte[] dataBuffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
                fileOutputStream.write(dataBuffer, 0, bytesRead);
            }
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to download client jar: " + e.getMessage());
            return;
        }

        try {
            InputStream clientJarStream = new URL(clientJarUrl).openStream();
            String sha1 = DigestUtils.sha1Hex(clientJarStream);
            if (sha1.equals(clientJarSha1)) {
                plugin.getLogger().info("Client jar downloaded: " + clientJar.getAbsolutePath());
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to download client jar: " + e.getMessage());
        }
    }

    public void generateColorMap(BlockFace playerFacing) {
        blockDataMap.clear();

        HashMap<String, Material> materialMap = new HashMap<>();
        HashMap<Material, Texture.BlockOrientation> blockOrientation = new HashMap<>();
        HashMap<Material, String[]> textureSides = new HashMap<>();

        String variantRegex = "_front|_side|_top|_bottom|_back";

        for (String texture : textures) {
            String blockName = texture.replaceAll(variantRegex, "");
            Material material = Material.getMaterial(blockName.toUpperCase());
            if (material == null) {
                plugin.getLogger().info("Material not found: " + texture);
                continue;
            }
            if (!material.isBlock() || material.isAir() || !material.isOccluding()) {
                // plugin.getLogger().info("Material is not suitable: " + texture);
                continue;
            }
            materialMap.put(texture, material);

            Texture.BlockOrientation orientation = getBlockOrientationType(material);
            blockOrientation.put(material, orientation);

            String blockSide = texture.replace(blockName + "_", "");
            if (blockSide.equals(blockName)) {
                blockSide = "";
            }

            textureSides.computeIfAbsent(material, k -> new String[0]);
            String[] sides = textureSides.get(material);
            String[] newSides = new String[sides.length + 1];
            System.arraycopy(sides, 0, newSides, 0, sides.length);
            newSides[sides.length] = blockSide;
            textureSides.put(material, newSides);
        }

        for (String texture : textures) {
            Material material = materialMap.get(texture);
            if (material == null) {
                continue;
            }
            Texture.BlockOrientation orientation = blockOrientation.get(material);
            String[] textureSidesArray = textureSides.get(material);
            String blockName = texture.replaceAll(variantRegex, "");
            String blockSide = texture.replaceAll(blockName, "").replaceAll("_", "");

            BlockData blockData;
            if (textureSidesArray.length == 1) {
                blockData = material.createBlockData("");
            } else {
                blockData = createBlockDataForOrientation(new Texture(blockSide, textureSidesArray, material, orientation), playerFacing);
            }

            if (blockData == null) {
                plugin.getLogger().info("Block data " + texture + " not visible from player facing " + playerFacing);
                continue;
            }

            blockDataMap.put(texture, blockData);
        }
    }
    
    private BlockData createBlockDataForOrientation(Texture texture, BlockFace playerFacing) {
        return switch (texture.orientation) {
            case AXIS -> createBlockDataForAxis(texture, playerFacing);
            case FACING, FACING_NO_VERTICAL -> createBlockDataForFacing(texture, playerFacing);
            default -> createBlockDataForNonRotatable(texture, playerFacing);
        };
    }
    
    private BlockData createBlockDataForAxis(Texture texture, BlockFace playerFacing) {
        return switch (playerFacing) {
            case NORTH, SOUTH -> switch (texture.currentSide) {
                case TOP, BOTTOM -> texture.material.createBlockData("[axis=z]");
                case NONE, SIDE -> texture.material.createBlockData("[axis=y]");
                default -> null;
            };
            case EAST, WEST -> switch (texture.currentSide) {
                case TOP, BOTTOM -> texture.material.createBlockData("[axis=x]");
                case NONE, SIDE -> texture.material.createBlockData("[axis=y]");
                default -> null;
            };
            case UP, DOWN -> switch (texture.currentSide) {
                case TOP, BOTTOM -> texture.material.createBlockData("[axis=y]");
                case NONE, SIDE -> texture.material.createBlockData("[axis=x]");
                default -> null;
            };
            default -> null;
        };
    }
    
    private BlockData createBlockDataForFacing(Texture texture, BlockFace playerFacing) {
        return switch (playerFacing) {
            case NORTH -> switch (texture.currentSide) {
                case FRONT -> texture.material.createBlockData("[facing=south]");
                case SIDE -> texture.material.createBlockData("[facing=west]");
                case BACK -> texture.material.createBlockData("[facing=north]");
                case TOP -> texture.orientation == Texture.BlockOrientation.FACING ? texture.material.createBlockData("[facing=down]") : null;
                case BOTTOM -> texture.orientation == Texture.BlockOrientation.FACING ? texture.material.createBlockData("[facing=up]") : null;
                default -> null;
            };
            case EAST -> switch (texture.currentSide) {
                case FRONT -> texture.material.createBlockData("[facing=west]");
                case SIDE -> texture.material.createBlockData("[facing=north]");
                case BACK -> texture.material.createBlockData("[facing=east]");
                case TOP -> texture.orientation == Texture.BlockOrientation.FACING ? texture.material.createBlockData("[facing=down]") : null;
                case BOTTOM -> texture.orientation == Texture.BlockOrientation.FACING ? texture.material.createBlockData("[facing=up]") : null;
                default -> null;
            };
            case SOUTH -> switch (texture.currentSide) {
                case FRONT -> texture.material.createBlockData("[facing=north]");
                case SIDE -> texture.material.createBlockData("[facing=east]");
                case BACK -> texture.material.createBlockData("[facing=south]");
                case TOP -> texture.orientation == Texture.BlockOrientation.FACING ? texture.material.createBlockData("[facing=down]") : null;
                case BOTTOM -> texture.orientation == Texture.BlockOrientation.FACING ? texture.material.createBlockData("[facing=up]") : null;
                default -> null;
            };
            case WEST -> switch (texture.currentSide) {
                case FRONT -> texture.material.createBlockData("[facing=east]");
                case SIDE -> texture.material.createBlockData("[facing=south]");
                case BACK -> texture.material.createBlockData("[facing=west]");
                case TOP -> texture.orientation == Texture.BlockOrientation.FACING ? texture.material.createBlockData("[facing=down]") : null;
                case BOTTOM -> texture.orientation == Texture.BlockOrientation.FACING ? texture.material.createBlockData("[facing=up]") : null;
                default -> null;
            };
            case UP -> switch (texture.currentSide) {
                case BOTTOM -> texture.material.createBlockData("");
                case TOP, FRONT -> texture.orientation == Texture.BlockOrientation.FACING ? texture.material.createBlockData("[facing=down]") : null;
                case NONE -> texture.orientation == Texture.BlockOrientation.FACING ? texture.material.createBlockData("[facing=north]") : null;
                case BACK -> texture.orientation == Texture.BlockOrientation.FACING ? texture.material.createBlockData("[facing=up]") : null;
                default -> null;
            };
            case DOWN -> switch (texture.currentSide) {
                case TOP -> texture.material.createBlockData("");
                case BOTTOM, FRONT -> texture.orientation == Texture.BlockOrientation.FACING ? texture.material.createBlockData("[facing=up]") : null;
                case NONE -> texture.orientation == Texture.BlockOrientation.FACING ? texture.material.createBlockData("[facing=north]") : null;
                case BACK -> texture.orientation == Texture.BlockOrientation.FACING ? texture.material.createBlockData("[facing=down]") : null;
                default -> null;
            };
            default -> null;
        };
    }

    private BlockData createBlockDataForNonRotatable(Texture texture, BlockFace playerFacing) {
        BlockData blockData = null;
        if (texture.equals(Side.FRONT) && texture.contains(Side.SIDE) && texture.contains(Side.TOP) && !texture.contains(Side.BOTTOM)) {
            blockData = texture.createEmptyBlockData(playerFacing == BlockFace.SOUTH || playerFacing == BlockFace.EAST);
        } else if (texture.equals(Side.FRONT) && texture.contains(Side.SIDE) && texture.contains(Side.TOP) && texture.contains(Side.BOTTOM)) {
            blockData = texture.createEmptyBlockData(playerFacing == BlockFace.SOUTH || playerFacing == BlockFace.NORTH);
        } else if (texture.equals(Side.SIDE) && texture.contains(Side.TOP) && texture.contains(Side.BOTTOM) && texture.contains(Side.FRONT)) {
            blockData = texture.createEmptyBlockData(playerFacing == BlockFace.WEST || playerFacing == BlockFace.EAST);
        } else if (texture.equals(Side.SIDE) && texture.contains(Side.TOP) && !texture.contains(Side.BOTTOM) && texture.contains(Side.FRONT)) {
            blockData = texture.createEmptyBlockData(playerFacing == BlockFace.WEST || playerFacing == BlockFace.NORTH);
        } else if (texture.equals(Side.SIDE) && texture.contains(Side.TOP) && !texture.contains(Side.BOTTOM) && !texture.contains(Side.FRONT)) {
            blockData = texture.createEmptyBlockData(playerFacing != BlockFace.DOWN);
        } else if (texture.equals(Side.SIDE) && texture.contains(Side.BOTTOM) && !texture.contains(Side.TOP)) {
            blockData = texture.createEmptyBlockData(playerFacing != BlockFace.UP);
        } else if (texture.equals(Side.SIDE) && !texture.contains(Side.TOP)) {
            blockData = texture.createEmptyBlockData(playerFacing != BlockFace.DOWN && playerFacing != BlockFace.UP);
        } else if (texture.equals(Side.NONE)) {
            blockData = texture.createEmptyBlockData(playerFacing != BlockFace.DOWN && playerFacing != BlockFace.UP);
        } else if (texture.equals(Side.TOP) && texture.contains(Side.SIDE) && !texture.contains(Side.BOTTOM)) {
            blockData = texture.createEmptyBlockData(playerFacing == BlockFace.DOWN);
        } else if (texture.equals(Side.TOP) && !texture.contains(Side.BOTTOM)) {
            blockData = texture.createEmptyBlockData(playerFacing == BlockFace.DOWN || playerFacing == BlockFace.UP);
        } else if (texture.equals(Side.BOTTOM) && !texture.contains(Side.TOP)) {
            blockData = texture.createEmptyBlockData(playerFacing == BlockFace.UP || playerFacing == BlockFace.DOWN);
        } else if (texture.equals(Side.TOP)) {
            blockData = texture.createEmptyBlockData(playerFacing == BlockFace.DOWN);
        } else if (texture.equals(Side.BOTTOM)) {
            blockData = texture.createEmptyBlockData(playerFacing == BlockFace.UP);
        }
        return blockData;
    }
}
