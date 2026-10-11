// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-FileCopyrightText: 2026 George Paton
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.dim.mapgen;

import com.hbmspace.blocks.TerrainBlocksSpace;
import com.hbmspace.dim.BiomeGenBaseCelestial;
import com.hbmspace.dim.legacy.MapGenCaves;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class MapGenRoots extends MapGenCaves {

    public MapGenRoots() {
        branchWidth = 0.25F;
        waterStopsTunnels = false;
        pitchDivisor = 16.0F;
        widthScale = 0.5F;
        wideScale = 2.5F;
        tunnelHeightFactor = 2.25D;
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

        if (is(data[index], Blocks.STONE)) {
            if (y < 10) {
                data[index] = Blocks.OBSIDIAN.defaultBlockState();
            } else {
                data[index] = TerrainBlocksSpace.LAYTHE_ROOT.get().defaultBlockState();

                if (foundTop && is(data[index + 1], biome.fillerBlock)) {
                    data[index + 1] = Blocks.GRASS_BLOCK.defaultBlockState();
                }
            }
        }
    }
}
