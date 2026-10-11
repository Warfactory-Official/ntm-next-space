// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.dim.duna.biome;

import com.hbmspace.blocks.ModBlocksSpace;
import com.hbmspace.blocks.TerrainBlocksSpace;
import com.hbmspace.dim.legacy.LegacyWorld;
import java.util.Random;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class BiomeGenDunaLowlands extends BiomeGenBaseDuna {

    BiomeGenDunaLowlands() {
        super("duna_lowlands", 121);
        this.setBiomeName("Dunaian Lowland Plains");
        this.setHeight(new Height(-0.6F, 0.01F));
    }

    @Override
    public void genTerrainBlocks(
            LegacyWorld world, Random rand, BlockState[] blocks, int x, int z, double noise) {
        Block rock = TerrainBlocksSpace.DUNA_ROCK.get();
        BlockState cobble = ModBlocksSpace.DUNA_COBBLE.get().defaultBlockState();
        BlockState sands = ModBlocksSpace.DUNA_SANDS.get().defaultBlockState();
        BlockState block = this.topBlock.defaultBlockState();
        BlockState block1 = this.fillerBlock.defaultBlockState();
        int k = -1;
        int l = (int) (noise / 6.0D + 6.0D + rand.nextDouble() * 0.85D);
        int i1 = x & 15;
        int j1 = z & 15;
        int k1 = blocks.length / 256;

        for (int l1 = 255; l1 >= 0; --l1) {
            int i2 = (j1 * 16 + i1) * k1 + l1;

            if (l1 <= rand.nextInt(5)) {
                blocks[i2] = Blocks.BEDROCK.defaultBlockState();
            } else {
                BlockState block2 = blocks[i2];

                if (block2 != null && !block2.isAir()) {
                    if (block2.getBlock() == rock) {
                        if (k == -1) {
                            if (l <= 0) {
                                block = null;
                                block1 = rock.defaultBlockState();
                            } else if (l1 >= 59 && l1 <= 64) {
                                block = this.topBlock.defaultBlockState();
                                block1 = this.fillerBlock.defaultBlockState();
                            }

                            if (l1 < 63 && (block == null || block.isAir())) {
                                block = this.topBlock.defaultBlockState();
                            }

                            k = l;

                            if (l1 >= 62) {
                                blocks[i2] = block;
                            } else {
                                block = null;
                                block1 = this.topBlock.defaultBlockState();
                                float randValue = rand.nextFloat();
                                if (l1 < 55 && randValue > 0.75) {
                                    blocks[i2] = cobble;
                                } else if (l1 < 54 && randValue > 0.5) {
                                    blocks[i2] = cobble;
                                } else if (randValue > 0.4) {
                                    blocks[i2] = rock.defaultBlockState();
                                } else {
                                    blocks[i2] = sands;
                                }
                            }
                        } else if (k > 0) {
                            --k;
                            blocks[i2] = block1;

                            if (k == 0 && block1.getBlock() == Blocks.SAND) {
                                k = rand.nextInt(4) + Math.max(0, l1 - 63);
                                block1 = Blocks.SANDSTONE.defaultBlockState();
                            }
                        }
                    }
                } else {
                    k = -1;
                }
            }
        }
    }
}
