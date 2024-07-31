package com.uxzylon.satmap;

import org.bukkit.Material;
import org.bukkit.block.data.BlockData;

public class Texture {
    Side currentSide;
    Side[] availableSides;
    Material material;
    BlockOrientation orientation;

    public Texture(String currentSide, String[] availableSides, Material material, BlockOrientation orientation) {
        if (currentSide.isEmpty()) {
            this.currentSide = Side.NONE;
        } else {
            this.currentSide = getSide(currentSide);
        }

        this.availableSides = new Side[availableSides.length];
        for (int i = 0; i < availableSides.length; i++) {
            this.availableSides[i] = getSide(availableSides[i]);
        }

        this.material = material;

        this.orientation = orientation;
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
        return currentSide.equals(side);
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
