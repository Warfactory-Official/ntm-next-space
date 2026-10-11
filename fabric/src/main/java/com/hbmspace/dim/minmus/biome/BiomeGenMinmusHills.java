// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-FileCopyrightText: 2024 JamesH_2
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.dim.minmus.biome;

import com.hbmspace.blocks.ModBlocksSpace;
import com.hbmspace.dim.legacy.LegacyWorld;
import java.util.Random;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class BiomeGenMinmusHills extends BiomeGenBaseMinmus {

    BiomeGenMinmusHills() {
        super("minmus_hills", 124);
        this.setBiomeName("Minmus Hills");

        this.setHeight(new Height(0.325F, 0.08F));
        this.topBlock = ModBlocksSpace.MINMUS_REGOLITH.get();
        this.fillerBlock = ModBlocksSpace.MINMUS_REGOLITH.get();
    }

    @Override
    public void genTerrainBlocks(
            LegacyWorld world, Random rand, BlockState[] blocks, int x, int z, double noise) {
        Block stone = ModBlocksSpace.MINMUS_STONE.get();
        BlockState block = this.topBlock.defaultBlockState();
        BlockState block1 = this.fillerBlock.defaultBlockState();
        int k = -1;
        int l = (int) (noise / 3.0D + 3.0D + rand.nextDouble() * 0.25D);
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
                    if (block2.getBlock() == stone) {
                        if (k == -1) {
                            if (l <= 0) {
                                block = null;
                                block1 = stone.defaultBlockState();
                            } else if (l1 >= 59 && l1 <= 74) {
                                block = this.topBlock.defaultBlockState();
                                block1 = this.fillerBlock.defaultBlockState();
                            }

                            if (l1 < 63 && (block == null || block.isAir())) {
                                block = this.topBlock.defaultBlockState();
                            }

                            k = l;

                            if (l1 >= 62) {
                                blocks[i2] = block;
                            } else if (l1 < 56 - l) {
                                block = null;
                                block1 = stone.defaultBlockState();
                                blocks[i2] = Blocks.GRAVEL.defaultBlockState();
                            } else {
                                blocks[i2] = block1;
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
