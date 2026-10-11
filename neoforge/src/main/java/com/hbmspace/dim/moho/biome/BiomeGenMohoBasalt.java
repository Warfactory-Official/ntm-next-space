// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-FileCopyrightText: 2024-2025 George Paton
// SPDX-FileCopyrightText: 2023 JamesH_2
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.dim.moho.biome;

import com.hbmspace.blocks.ModBlocksSpace;
import com.hbmspace.dim.legacy.LegacyWorld;
import java.util.Random;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class BiomeGenMohoBasalt extends BiomeGenBaseMoho {

    BiomeGenMohoBasalt() {
        super("moho_basalt_deltas", 100);
        this.setBiomeName("Moho Basalt Deltas");

        this.setHeight(new Height(0, 0.224F));

        this.topBlock = basalt;
        this.fillerBlock = basalt;
    }

    @Override
    public void genTerrainBlocks(
            LegacyWorld world, Random rand, BlockState[] blocks, int x, int z, double noise) {
        Block stone = ModBlocksSpace.MOHO_STONE.get();
        BlockState block = this.topBlock.defaultBlockState();
        BlockState block1 = this.fillerBlock.defaultBlockState();
        int k = -1;
        int l = (int) (noise / 8.0D + 8.0D + rand.nextDouble() * 0.50D);
        int bx = x & 15;
        int bz = z & 15;
        int s = blocks.length / 256;

        for (int by = 255; by >= 0; --by) {
            int i = (bz * 16 + bx) * s + by;

            boolean contour =
                    (noise > -0.25D && noise < 1.0D)
                            || (noise > 1.5D && noise < 2.0D)
                            || (noise > 2.5D && noise < 3.0D);
            double offset = contour ? 0 : Math.pow(rand.nextDouble(), 4) * 8;

            if (by <= rand.nextInt(5)) {
                blocks[i] = Blocks.BEDROCK.defaultBlockState();
            } else {
                BlockState block2 = blocks[i];

                if (block2 != null && !block2.isAir()) {
                    if (block2.getBlock() == stone) {
                        if (k == -1) {
                            if (l <= 0) {
                                block = null;
                                block1 = stone.defaultBlockState();
                            } else if (by >= 59 && by <= 64) {
                                block = this.topBlock.defaultBlockState();
                                block1 = this.fillerBlock.defaultBlockState();
                            }

                            if (by < 63 && (block == null || block.isAir())) {
                                block = this.topBlock.defaultBlockState();
                            }

                            k = l;

                            if (by >= 62) {
                                blocks[i] = block;
                            } else if (by < 56 - l) {
                                block = null;
                                block1 = stone.defaultBlockState();
                                blocks[i] = basalt.defaultBlockState();
                            } else {
                                blocks[i] = block1;
                            }

                            for (int oy = 0; oy < offset; oy++) {
                                blocks[i + oy] = this.topBlock.defaultBlockState();
                            }
                        } else if (k > 0) {
                            --k;
                            blocks[i] = block1;
                        }
                    }
                } else {
                    k = -1;
                }
            }
        }
    }
}
