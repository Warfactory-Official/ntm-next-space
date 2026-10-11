// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.dim.dres.biome;

import com.hbmspace.dim.legacy.LegacyWorld;
import java.util.Random;
import net.minecraft.world.level.block.state.BlockState;

public class BiomeGenDresCanyon extends BiomeGenBaseDres {

    BiomeGenDresCanyon() {
        super("dres_large_basins", 111);
        this.setBiomeName("Dres Large Basins");

        this.setHeight(new Height(-1F, 0.34F));
    }

    @Override
    public void genTerrainBlocks(
            LegacyWorld world, Random rand, BlockState[] blocks, int x, int z, double noise) {
        genDresTerrain(world, rand, blocks, x, z, noise, 6.0D, 0.85D, 62, 68, 8, 7);
    }
}
