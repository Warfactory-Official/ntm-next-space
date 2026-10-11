// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-FileCopyrightText: 2024-2025 George Paton
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.dim;

import com.hbmspace.blocks.FloraBlocksSpace;
import com.hbmspace.blocks.TerrainBlocksSpace;
import com.hbmspace.dim.legacy.LegacyDecorWorld;
import com.hbmspace.dim.legacy.feature.LegacyFeature;
import java.util.Random;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;

public class WorldGenWaterPlant implements LegacyFeature {
    private final int seaLevel;

    public WorldGenWaterPlant(int seaLevel) {
        this.seaLevel = seaLevel;
    }

    @Override
    public boolean generate(LegacyDecorWorld world, Random rand, int x, int y, int z) {
        boolean flag = false;

        for (int l = 0; l < 128; ++l) {
            int px = x + rand.nextInt(8) - rand.nextInt(8);
            int py = y + rand.nextInt(4) - rand.nextInt(4);
            int pz = z + rand.nextInt(8) - rand.nextInt(8);

            if (py < seaLevel - 1
                    && isStillWater(world.getBlock(px, py, pz))
                    && world.is(px, py - 1, pz, TerrainBlocksSpace.LAYTHE_SILT.get())) {
                int type = rand.nextInt(7);

                switch (type) {
                    case 0, 1, 2 ->
                            world.setBlock(
                                    px,
                                    py,
                                    pz,
                                    FloraBlocksSpace.LAYTHE_SEAGRASS.get().defaultBlockState(),
                                    Block.UPDATE_CLIENTS);
                    case 3 ->
                            world.setBlock(
                                    px,
                                    py,
                                    pz,
                                    FloraBlocksSpace.LAYTHE_GLOWGRASS.get().defaultBlockState(),
                                    Block.UPDATE_CLIENTS);
                    case 4, 5 -> {
                        if (py < seaLevel - 2) {
                            BlockState tall =
                                    FloraBlocksSpace.PLANT_TALL_LAYTHE.get().defaultBlockState();
                            world.setBlock(
                                    px,
                                    py,
                                    pz,
                                    tall.setValue(DoublePlantBlock.HALF, DoubleBlockHalf.LOWER),
                                    Block.UPDATE_CLIENTS);
                            world.setBlock(
                                    px,
                                    py + 1,
                                    pz,
                                    tall.setValue(DoublePlantBlock.HALF, DoubleBlockHalf.UPPER),
                                    Block.UPDATE_CLIENTS);
                        }
                    }
                    default -> {
                        if (py < seaLevel - 4) {
                            int height = 2 + rand.nextInt(Math.min(8, seaLevel - py - 2));
                            BlockState kelp =
                                    FloraBlocksSpace.LAYTHE_KELP.get().defaultBlockState();
                            for (int h = 0; h < height; ++h) {
                                world.setBlock(px, py + h, pz, kelp, Block.UPDATE_CLIENTS);
                            }
                        }
                    }
                }

                flag = true;
            }
        }

        return flag;
    }

    static boolean isStillWater(BlockState state) {
        return state.is(Blocks.WATER) && state.getFluidState().isSource();
    }
}
