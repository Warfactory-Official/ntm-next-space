// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-FileCopyrightText: 2025 George Paton
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.dim;

import com.hbm.blocks.ModBlocks;
import com.hbmspace.blocks.BlockKelp;
import com.hbmspace.blocks.BlockWaterPlant;
import com.hbmspace.blocks.FloraBlocksSpace;
import com.hbmspace.blocks.TerrainBlocksSpace;
import com.hbmspace.blocks.fluid.GenericFluidBlock;
import com.hbmspace.blocks.generic.BlockTallPlantWater;
import com.hbmspace.dim.legacy.LegacyDecorWorld;
import com.hbmspace.dim.legacy.feature.LegacyFeature;
import java.util.Random;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class WorldGenWaterCoral implements LegacyFeature {
    private final int seaLevel;

    public WorldGenWaterCoral(int seaLevel) {
        this.seaLevel = seaLevel;
    }

    @Override
    public boolean generate(LegacyDecorWorld world, Random rand, int x, int y, int z) {
        boolean flag = false;

        for (int l = 0; l < 16; ++l) {
            int px = x + rand.nextInt(8) - rand.nextInt(8);
            int py = y + rand.nextInt(4) - rand.nextInt(4);
            int pz = z + rand.nextInt(8) - rand.nextInt(8);

            if (!world.blockExists(px, py, pz)) continue;

            if (py < seaLevel - 1
                    && isWaterMaterial(world.getBlock(px, py, pz))
                    && world.is(px, py - 1, pz, TerrainBlocksSpace.LAYTHE_SILT.get())) {
                int kind =
                        (rand.nextInt(FloraBlocksSpace.Coral.values().length) + 8)
                                % FloraBlocksSpace.Coral.values().length;
                BlockState block =
                        FloraBlocksSpace.LAYTHE_CORAL_BLOCK[kind].get().defaultBlockState();
                BlockState coral = FloraBlocksSpace.LAYTHE_CORAL[kind].get().defaultBlockState();

                world.setBlock(px, py - 1, pz, block, Block.UPDATE_CLIENTS);

                int oy = 0;
                int ox = 0;
                int oz = 0;
                while (oy < 8) {
                    if (py + oy > seaLevel - 3) break;
                    if (rand.nextBoolean()) break;

                    if (rand.nextBoolean()) {
                        if (rand.nextBoolean()) {
                            ox += rand.nextBoolean() ? 1 : -1;
                        } else {
                            oz += rand.nextBoolean() ? 1 : -1;
                        }
                    }

                    int cx = px + ox;
                    int cy = py + oy;
                    int cz = pz + oz;
                    if (world.blockExists(cx, cy, cz))
                        world.setBlock(cx, cy, cz, block, Block.UPDATE_CLIENTS);
                    if (rand.nextBoolean() && world.blockExists(cx + 1, cy, cz)) {
                        world.setBlock(cx + 1, cy, cz, coral, Block.UPDATE_CLIENTS);
                    }
                    if (rand.nextBoolean() && world.blockExists(cx - 1, cy, cz)) {
                        world.setBlock(cx - 1, cy, cz, coral, Block.UPDATE_CLIENTS);
                    }
                    if (rand.nextBoolean() && world.blockExists(cx, cy, cz + 1)) {
                        world.setBlock(cx, cy, cz + 1, coral, Block.UPDATE_CLIENTS);
                    }
                    if (rand.nextBoolean() && world.blockExists(cx, cy, cz - 1)) {
                        world.setBlock(cx, cy, cz - 1, coral, Block.UPDATE_CLIENTS);
                    }

                    oy++;
                }

                if (world.blockExists(px + ox, py + oy, pz + oz)) {
                    world.setBlock(px + ox, py + oy, pz + oz, coral, Block.UPDATE_CLIENTS);
                }

                flag = true;
            }
        }

        return flag;
    }

    public static boolean isWaterMaterial(BlockState state) {
        Block block = state.getBlock();
        return state.is(Blocks.WATER)
                || block instanceof BlockWaterPlant
                || block instanceof BlockKelp
                || block instanceof BlockTallPlantWater
                || block instanceof GenericFluidBlock
                || block == ModBlocks.SULFURIC_ACID_BLOCK.get()
                || block == ModBlocks.SCHRABIDIC_BLOCK.get();
    }
}
