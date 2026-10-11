// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.dim.eve.biome;

import com.hbmspace.dim.legacy.LegacyWorld;
import java.util.Random;
import net.minecraft.world.level.block.state.BlockState;

public class BiomeGenEveMountains extends BiomeGenBaseEve {

    BiomeGenEveMountains() {
        super("eve_mountains", 116);
        this.setBiomeName("Eve Mountains");

        this.setHeight(new Height(0.525F, 0.4F));
    }

    @Override
    public void genTerrainBlocks(
            LegacyWorld world, Random rand, BlockState[] blocks, int x, int z, double noise) {
        genHighlandBlocks(world, rand, blocks, x, z, noise);
    }
}
