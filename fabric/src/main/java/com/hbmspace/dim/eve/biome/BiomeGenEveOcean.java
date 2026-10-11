// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.dim.eve.biome;

import com.hbmspace.blocks.TerrainBlocksSpace;
import com.hbmspace.dim.legacy.LegacyWorld;
import java.util.Random;
import net.minecraft.world.level.block.state.BlockState;

public class BiomeGenEveOcean extends BiomeGenBaseEve {

    BiomeGenEveOcean() {
        super("eve_ocean", 115);
        this.setBiomeName("Explodium Ocean");

        this.setHeight(new Height(-0.6F, 0.01F));

        this.topBlock = TerrainBlocksSpace.EVE_SILT.get();
        this.fillerBlock = TerrainBlocksSpace.EVE_SILT.get();
    }

    @Override
    public void genTerrainBlocks(
            LegacyWorld world, Random rand, BlockState[] blocks, int x, int z, double noise) {
        genBedrockFloor(rand, blocks, x, z);
    }
}
