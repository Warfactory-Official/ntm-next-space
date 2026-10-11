// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-FileCopyrightText: 2025 George Paton
// SPDX-FileCopyrightText: 2024 JamesH_2
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.dim;

import com.hbmspace.blocks.FloraBlocksSpace.RubberPlant;
import com.hbmspace.blocks.FloraBlocksSpace.RubberTall;
import com.hbmspace.blocks.FloraBlocksSpace;
import com.hbmspace.blocks.TerrainBlocksSpace;
import com.hbmspace.dim.legacy.LegacyDecorWorld;
import com.hbmspace.dim.legacy.feature.LegacyFeature;
import com.hbmspace.dim.tekto.biome.BiomeGenBaseTekto;
import java.util.Random;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class WorldGenRubberPlant implements LegacyFeature {

    @Override
    public boolean generate(LegacyDecorWorld world, Random rand, int x, int y, int z) {
        boolean flag = false;
        BiomeGenBaseCelestial currentBiome = world.getBiomeGenForCoords(x, z);
        Block grass = FloraBlocksSpace.RUBBER_GRASS.get();
        Block sand = TerrainBlocksSpace.VINYL_SAND.get();

        for (int l = 0; l < 64; ++l) {
            int px = x + rand.nextInt(8) - rand.nextInt(8);
            int py = y + rand.nextInt(4) - rand.nextInt(4);
            int pz = z + rand.nextInt(8) - rand.nextInt(8);

            if (world.is(px, py - 1, pz, grass)) {
                world.setBlock(
                        px,
                        py,
                        pz,
                        plant(RubberPlant.values()[rand.nextInt(3)]),
                        Block.UPDATE_CLIENTS);
                flag = true;
            } else if (world.is(px, py - 1, pz, sand)) {
                if (rand.nextInt(64) == 0) {
                    world.setBlock(
                            px,
                            world.getTopSolidOrLiquidBlock(px, pz),
                            pz,
                            tall(RubberTall.CACT),
                            Block.UPDATE_CLIENTS);
                }
            }

            if (currentBiome == BiomeGenBaseTekto.forest && world.is(px, py - 1, pz, grass)) {
                RubberTall bush = rand.nextInt(2) == 0 ? RubberTall.BUSH : RubberTall.FLOWER;
                RubberPlant other = RubberPlant.values()[rand.nextInt(3)];

                if (rand.nextInt(4) == 0) {
                    world.setBlock(px, py, pz, tall(bush), Block.UPDATE_CLIENTS);
                } else {
                    world.setBlock(px, py, pz, plant(other), Block.UPDATE_CLIENTS);
                }
                if (rand.nextInt(10) == 0) {
                    world.setBlock(px, py, pz, tall(RubberTall.CACT), Block.UPDATE_CLIENTS);
                }
                flag = true;
            }
        }

        return flag;
    }

    private static BlockState plant(RubberPlant type) {
        return FloraBlocksSpace.RUBBER_PLANT[type.ordinal()].get().defaultBlockState();
    }

    private static BlockState tall(RubberTall type) {
        return FloraBlocksSpace.RUBBER_TALL[type.ordinal()].get().defaultBlockState();
    }
}
