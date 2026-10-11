// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-FileCopyrightText: 2024 George Paton
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.dim.mapgen;

import com.hbmspace.dim.BiomeGenBaseCelestial;
import com.hbmspace.dim.legacy.MapGenCaves;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class MapGenVanillaCaves extends MapGenCaves {

    private final Block stoneBlock;

    private Block lavaBlock = Blocks.LAVA;

    public MapGenVanillaCaves(Block stoneBlock) {
        this.stoneBlock = stoneBlock;
    }

    public MapGenVanillaCaves withLava(Block lavaBlock) {
        this.lavaBlock = lavaBlock;
        return this;
    }

    @Override
    protected void digBlock(
            BlockState[] data,
            int index,
            int x,
            int y,
            int z,
            int chunkX,
            int chunkZ,
            boolean foundTop) {
        BiomeGenBaseCelestial biome =
                worldObj.getBiomeGenForCoords(x + chunkX * 16, z + chunkZ * 16);
        BlockState block = data[index];

        if (is(block, stoneBlock) || is(block, biome.fillerBlock) || is(block, biome.topBlock)) {
            if (y < 10) {
                data[index] = lavaBlock.defaultBlockState();
            } else {
                data[index] = null;

                if (foundTop && is(data[index - 1], biome.fillerBlock)) {
                    data[index - 1] = biome.topBlock.defaultBlockState();
                }
            }
        }
    }
}
