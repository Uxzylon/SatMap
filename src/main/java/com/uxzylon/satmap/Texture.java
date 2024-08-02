package com.uxzylon.satmap;

import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.List;

import static com.uxzylon.satmap.SatMap.plugin;

public class Texture {
    public static String variantRegex = "_front|_side|_top|_bottom|_back";
    public static BlockFace[] directions = {BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST, BlockFace.UP, BlockFace.DOWN};

    Side side;
    Material material;
    BlockOrientation orientation;
    String name;
    Color color;

    Side[] availableSides;
    HashMap<BlockFace, BlockData> blockDataMap = new HashMap<>();

    public Texture(String textureName, BufferedImage textureImage) {
        this.name = textureName;
        this.color = getAverageColor(textureImage);

        String blockName = textureName.replaceAll(variantRegex, "");
        material = Material.getMaterial(blockName.toUpperCase());
        if (material == null) {
            // plugin.getLogger().info("Material not found: " + textureName);
            return;
        }
        if (!material.isBlock() || material.isAir() || !material.isOccluding()) {
            // plugin.getLogger().info("Material is not suitable: " + texture);
            return;
        }

        orientation = getBlockOrientationType(material);

        String blockSide = textureName.replace(blockName + "_", "");
        if (blockSide.equals(blockName)) {
            side = Side.NONE;
        } else {
            side = getSide(blockSide);
        }


    }

    public boolean isValid() {
        return material != null && side != null && !side.equals(Side.INVALID);
    }

    private Color getAverageColor(BufferedImage textureImage) {
        int width = textureImage.getWidth();
        int height = textureImage.getHeight();
        int totalPixels = width * height;
        int red = 0;
        int green = 0;
        int blue = 0;
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                Color pixelColor = new Color(textureImage.getRGB(x, y));
                red += pixelColor.getRed();
                green += pixelColor.getGreen();
                blue += pixelColor.getBlue();
            }
        }
        return new Color(red / totalPixels, green / totalPixels, blue / totalPixels);
    }

    public double colorDistance(Color color) {
        return Math.sqrt(Math.pow(this.color.getRed() - color.getRed(), 2) +
                Math.pow(this.color.getGreen() - color.getGreen(), 2) +
                Math.pow(this.color.getBlue() - color.getBlue(), 2));
    }

    public void addAvailableSides(Side[] sides) {
        availableSides = new Side[sides.length - 1];
        int i = 0;
        for (Side s : sides) {
            if (!s.equals(side)) {
                availableSides[i] = s;
                i++;
            }
        }
    }

    public void fillBlockDataMap() {
        for (BlockFace direction : directions) {
            BlockData blockData = getBlockData(direction);
            if (blockData != null) {
                blockDataMap.put(direction, blockData);
            }
        }
    }

    private BlockData getBlockData(BlockFace direction) {
        BlockData blockData;
        if (availableSides.length == 0) {
            blockData = createEmptyBlockData();
        } else {
            blockData = switch (orientation) {
                case AXIS -> createBlockDataForAxis(direction);
                case FACING, FACING_NO_VERTICAL -> createBlockDataForFacing(direction);
                default -> createBlockDataForNonRotatable(direction);
            };
        }

        // if (blockData == null) {
            // plugin.getLogger().info("Block data " + name + " not visible from player facing " + direction);
        // }

        return blockData;
    }

    private BlockData createBlockDataForAxis(BlockFace direction) {
        return switch (direction) {
            case NORTH, SOUTH -> switch (side) {
                case TOP, BOTTOM -> material.createBlockData("[axis=z]");
                case NONE, SIDE -> material.createBlockData("[axis=y]");
                default -> null;
            };
            case EAST, WEST -> switch (side) {
                case TOP, BOTTOM -> material.createBlockData("[axis=x]");
                case NONE, SIDE -> material.createBlockData("[axis=y]");
                default -> null;
            };
            case UP, DOWN -> switch (side) {
                case TOP, BOTTOM -> material.createBlockData("[axis=y]");
                case NONE, SIDE -> material.createBlockData("[axis=x]");
                default -> null;
            };
            default -> null;
        };
    }

    private BlockData createBlockDataForFacing(BlockFace direction) {
        return switch (direction) {
            case NORTH -> switch (side) {
                case FRONT -> material.createBlockData("[facing=south]");
                case SIDE -> material.createBlockData("[facing=west]");
                case BACK -> material.createBlockData("[facing=north]");
                case TOP -> orientation == Texture.BlockOrientation.FACING ? material.createBlockData("[facing=down]") : null;
                case BOTTOM -> orientation == Texture.BlockOrientation.FACING ? material.createBlockData("[facing=up]") : null;
                default -> null;
            };
            case EAST -> switch (side) {
                case FRONT -> material.createBlockData("[facing=west]");
                case SIDE -> material.createBlockData("[facing=north]");
                case BACK -> material.createBlockData("[facing=east]");
                case TOP -> orientation == Texture.BlockOrientation.FACING ? material.createBlockData("[facing=down]") : null;
                case BOTTOM -> orientation == Texture.BlockOrientation.FACING ? material.createBlockData("[facing=up]") : null;
                default -> null;
            };
            case SOUTH -> switch (side) {
                case FRONT -> material.createBlockData("[facing=north]");
                case SIDE -> material.createBlockData("[facing=east]");
                case BACK -> material.createBlockData("[facing=south]");
                case TOP -> orientation == Texture.BlockOrientation.FACING ? material.createBlockData("[facing=down]") : null;
                case BOTTOM -> orientation == Texture.BlockOrientation.FACING ? material.createBlockData("[facing=up]") : null;
                default -> null;
            };
            case WEST -> switch (side) {
                case FRONT -> material.createBlockData("[facing=east]");
                case SIDE -> material.createBlockData("[facing=south]");
                case BACK -> material.createBlockData("[facing=west]");
                case TOP -> orientation == Texture.BlockOrientation.FACING ? material.createBlockData("[facing=down]") : null;
                case BOTTOM -> orientation == Texture.BlockOrientation.FACING ? material.createBlockData("[facing=up]") : null;
                default -> null;
            };
            case UP -> switch (side) {
                case BOTTOM -> material.createBlockData("");
                case TOP, FRONT -> orientation == Texture.BlockOrientation.FACING ? material.createBlockData("[facing=down]") : null;
                case NONE -> orientation == Texture.BlockOrientation.FACING ? material.createBlockData("[facing=north]") : null;
                case BACK -> orientation == Texture.BlockOrientation.FACING ? material.createBlockData("[facing=up]") : null;
                default -> null;
            };
            case DOWN -> switch (side) {
                case TOP -> material.createBlockData("");
                case BOTTOM, FRONT -> orientation == Texture.BlockOrientation.FACING ? material.createBlockData("[facing=up]") : null;
                case NONE -> orientation == Texture.BlockOrientation.FACING ? material.createBlockData("[facing=north]") : null;
                case BACK -> orientation == Texture.BlockOrientation.FACING ? material.createBlockData("[facing=down]") : null;
                default -> null;
            };
            default -> null;
        };
    }

    private BlockData createBlockDataForNonRotatable(BlockFace direction) {
        BlockData blockData = null;
        if (equals(Side.FRONT) && contains(Side.SIDE) && contains(Side.TOP) && !contains(Side.BOTTOM)) {
            blockData = createEmptyBlockData(direction == BlockFace.SOUTH || direction == BlockFace.EAST);
        } else if (equals(Side.FRONT) && contains(Side.SIDE) && contains(Side.TOP) && contains(Side.BOTTOM)) {
            blockData = createEmptyBlockData(direction == BlockFace.SOUTH || direction == BlockFace.NORTH);
        } else if (equals(Side.SIDE) && contains(Side.TOP) && contains(Side.BOTTOM) && contains(Side.FRONT)) {
            blockData = createEmptyBlockData(direction == BlockFace.WEST || direction == BlockFace.EAST);
        } else if (equals(Side.SIDE) && contains(Side.TOP) && !contains(Side.BOTTOM) && contains(Side.FRONT)) {
            blockData = createEmptyBlockData(direction == BlockFace.WEST || direction == BlockFace.NORTH);
        } else if (equals(Side.SIDE) && contains(Side.TOP) && !contains(Side.BOTTOM) && !contains(Side.FRONT)) {
            blockData = createEmptyBlockData(direction != BlockFace.DOWN);
        } else if (equals(Side.SIDE) && contains(Side.BOTTOM) && !contains(Side.TOP)) {
            blockData = createEmptyBlockData(direction != BlockFace.UP);
        } else if (equals(Side.SIDE) && !contains(Side.TOP)) {
            blockData = createEmptyBlockData(direction != BlockFace.DOWN && direction != BlockFace.UP);
        } else if (equals(Side.NONE) && contains(Side.SIDE)) {
            blockData = createEmptyBlockData(direction == BlockFace.DOWN);
        } else if (equals(Side.NONE)) {
            blockData = createEmptyBlockData(direction != BlockFace.DOWN && direction != BlockFace.UP);
        } else if (equals(Side.TOP) && contains(Side.SIDE) && !contains(Side.BOTTOM)) {
            blockData = createEmptyBlockData(direction == BlockFace.DOWN);
        } else if (equals(Side.TOP) && !contains(Side.BOTTOM)) {
            blockData = createEmptyBlockData(direction == BlockFace.DOWN || direction == BlockFace.UP);
        } else if (equals(Side.BOTTOM) && !contains(Side.TOP)) {
            blockData = createEmptyBlockData(direction == BlockFace.UP || direction == BlockFace.DOWN);
        } else if (equals(Side.TOP)) {
            blockData = createEmptyBlockData(direction == BlockFace.DOWN);
        } else if (equals(Side.BOTTOM)) {
            blockData = createEmptyBlockData(direction == BlockFace.UP);
        }
        return blockData;
    }

    public boolean contains(Side side) {
        for (Side s : availableSides) {
            if (s.equals(side)) {
                return true;
            }
        }
        return false;
    }

    public boolean equals(Side side) {
        return this.side.equals(side);
    }

    public enum Side {
        TOP, BOTTOM, SIDE, FRONT, BACK, NONE, INVALID
    }

    private Side getSide(String side) {
        if (side.isEmpty()) {
            return Side.NONE;
        }
        try {
            return Side.valueOf(side.toUpperCase());
        } catch (IllegalArgumentException e) {
            return Side.INVALID;
        }
    }

    public enum BlockOrientation {
        AXIS,
        FACING,
        FACING_NO_VERTICAL,
        NONE
    }

    public static BlockOrientation getBlockOrientationType(Material material) {
        boolean axis = false;
        boolean facing = false;
        boolean facingVertical = false;

        try {
            material.createBlockData("[axis=y]");
            axis = true;
        } catch (IllegalArgumentException e) {
            // do nothing
        }

        try {
            material.createBlockData("[facing=west]");
            facing = true;
            material.createBlockData("[facing=up]");
            facingVertical = true;
        } catch (IllegalArgumentException e) {
            // do nothing
        }

        if (axis) {
            return BlockOrientation.AXIS;
        } else if (facing && facingVertical) {
            return BlockOrientation.FACING;
        } else if (facing) {
            return BlockOrientation.FACING_NO_VERTICAL;
        } else {
            return BlockOrientation.NONE;
        }
    }

    public BlockData createEmptyBlockData() {
        return material.createBlockData("");
    }

    public BlockData createEmptyBlockData(boolean condition) {
        if (condition) {
            return createEmptyBlockData();
        } else {
            return null;
        }
    }
}
