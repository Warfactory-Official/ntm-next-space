// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.dim.dres.biome;

import com.hbmspace.dim.legacy.LegacyWorld;
import java.util.Random;
import net.minecraft.world.level.block.state.BlockState;

public class BiomeGenDresPlains extends BiomeGenBaseDres {

    BiomeGenDresPlains() {
        super("dres_plains", 112);
        this.setBiomeName("Dresian Plains");

        this.setHeight(new Height(0.625F, 0.04F));
    }

    @Override
    public void genTerrainBlocks(
            LegacyWorld world, Random rand, BlockState[] blocks, int x, int z, double noise) {
        genDresTerrain(world, rand, blocks, x, z, noise, 8.0D, 0.50D, 82, 80, 8, 9);
    }
}
