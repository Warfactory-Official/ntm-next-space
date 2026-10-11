// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.dim.eve.biome;

import com.hbmspace.blocks.TerrainBlocksSpace;
import com.hbmspace.dim.legacy.LegacyWorld;
import java.util.Random;
import net.minecraft.world.level.block.state.BlockState;

public class BiomeGenEvePlains extends BiomeGenBaseEve {

    BiomeGenEvePlains() {
        super("eve_plains", 117);
        this.setBiomeName("Eve Plains");

        this.setHeight(new Height(0.125F, 0.05F));

        this.topBlock = TerrainBlocksSpace.EVE_SILT.get();
        this.fillerBlock = TerrainBlocksSpace.EVE_SILT.get();
    }

    @Override
    public void genTerrainBlocks(
            LegacyWorld world, Random rand, BlockState[] blocks, int x, int z, double noise) {
        genHighlandBlocks(world, rand, blocks, x, z, noise);
    }
}
