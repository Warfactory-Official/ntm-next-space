// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-FileCopyrightText: 2025 George Paton
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.dim.eve.biome;

import com.hbmspace.blocks.TerrainBlocksSpace;
import com.hbmspace.dim.legacy.LegacyWorld;
import java.util.Random;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class BiomeGenEveSeismicPlains extends BiomeGenBaseEve {

    BiomeGenEveSeismicPlains() {
        super("eve_seismic_plains", 114);
        this.setBiomeName("Eve Seismic Plains");

        this.setHeight(new Height(0.270F, 0.3F));

        this.topBlock = TerrainBlocksSpace.EVE_SILT.get();
        this.fillerBlock = TerrainBlocksSpace.EVE_SILT.get();
    }

    @Override
    public void genTerrainBlocks(
            LegacyWorld world, Random rand, BlockState[] blocks, int x, int z, double noise) {
        BlockState block = this.topBlock.defaultBlockState();
        BlockState block1 = this.fillerBlock.defaultBlockState();
        int k = -1;
        int l = (int) (noise / 6.0D + 6.0D + rand.nextDouble() * 0.85D);
        int bx = x & 15;
        int bz = z & 15;
        int s = blocks.length / 256;

        for (int by = 255; by >= 0; --by) {
            int i = (bz * 16 + bx) * s + by;

            if (by <= rand.nextInt(5)) {
                blocks[i] = Blocks.BEDROCK.defaultBlockState();
            } else {
                BlockState block2 = blocks[i];

                if (block2 != null && !block2.isAir()) {
                    if (block2.getBlock() == eveRock) {
                        if (k == -1) {
                            if (l <= 0) {
                                block = null;
                                block1 = eveRock.defaultBlockState();
                            } else if (by >= 59 && by <= 64) {
                                block = this.topBlock.defaultBlockState();
                                block1 = this.fillerBlock.defaultBlockState();
                            }

                            if (by < 63 && (block == null || block.isAir())) {
                                block = this.topBlock.defaultBlockState();
                            }

                            k = l;

                            if (by >= 62) {
                                blocks[i] =
                                        columnRandom(world, x, by, z) > 0.4
                                                ? block
                                                : eveRock.defaultBlockState();
                            } else if (by < 56 - l) {
                                block = null;
                                block1 = eveRock.defaultBlockState();
                                blocks[i] = basalt.defaultBlockState();
                            } else {
                                blocks[i] = block1;
                            }
                        } else if (k > 0) {
                            --k;
                            blocks[i] = block1;

                            if (k == 0 && block1.getBlock() == Blocks.SAND) {
                                k = rand.nextInt(4) + Math.max(0, by - 63);
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
