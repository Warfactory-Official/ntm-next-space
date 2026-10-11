// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-FileCopyrightText: 2024 George Paton
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.dim.eve.genlayer;

import com.hbmspace.blocks.TerrainBlocksSpace;
import com.hbmspace.dim.legacy.LegacyDecorWorld;
import com.hbmspace.dim.legacy.feature.LegacyFeature;
import java.util.Random;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class WorldGenEveSpike implements LegacyFeature {

    @Override
    public boolean generate(LegacyDecorWorld world, Random rand, int x, int y, int z) {
        Block silt = TerrainBlocksSpace.EVE_SILT.get();
        Block rock = TerrainBlocksSpace.EVE_ROCK.get();
        BlockState rockState = rock.defaultBlockState();

        while (world.isAirBlock(x, y, z) && y > 2) {
            --y;
        }

        if (!world.is(x, y, z, silt)) return false;

        y += rand.nextInt(4);
        int l = rand.nextInt(4) + 10;
        int i1 = l / 4 + rand.nextInt(2);

        if (i1 > 1 && rand.nextInt(2) == 0) {
            y += 10 + rand.nextInt(30);
        }

        int j1;
        int k1;
        int l1;

        for (j1 = 0; j1 < l; ++j1) {
            float f = (1.0F - (float) j1 / (float) l) * (float) i1;
            k1 = Mth.ceil(f);

            for (l1 = -k1; l1 <= k1; ++l1) {
                float f1 = (float) Mth.abs(l1) - 0.25F;

                for (int i2 = -k1; i2 <= k1; ++i2) {
                    float f2 = (float) Mth.abs(i2) - 0.25F;

                    if ((l1 == 0 && i2 == 0 || f1 * f1 + f2 * f2 <= f * f)
                            && (l1 != -k1 && l1 != k1 && i2 != -k1 && i2 != k1
                                    || rand.nextFloat() <= 0.75F)) {
                        if (replaceable(world.getBlock(x + l1, y + j1, z + i2), silt, rock)) {
                            world.setBlock(x + l1, y + j1, z + i2, rockState, Block.UPDATE_CLIENTS);
                        }

                        if (j1 != 0
                                && k1 > 1
                                && replaceable(
                                        world.getBlock(x + l1, y - j1, z + i2), silt, rock)) {
                            world.setBlock(x + l1, y - j1, z + i2, rockState, Block.UPDATE_CLIENTS);
                        }
                    }
                }
            }
        }

        j1 = i1 - 1;

        if (j1 < 0) {
            j1 = 0;
        } else if (j1 > 1) {
            j1 = 1;
        }

        for (int j2 = -j1; j2 <= j1; ++j2) {
            k1 = -j1;

            while (k1 <= j1) {
                l1 = y - 1;
                int k2 = 50;

                if (Math.abs(j2) == 1 && Math.abs(k1) == 1) {
                    k2 = rand.nextInt(5);
                }

                while (l1 > 50 && replaceable(world.getBlock(x + j2, l1, z + k1), silt, rock)) {
                    world.setBlock(x + j2, l1, z + k1, rockState, Block.UPDATE_CLIENTS);
                    --l1;
                    --k2;

                    if (k2 <= 0) {
                        l1 -= rand.nextInt(5) + 1;
                        k2 = rand.nextInt(5);
                    }
                }

                ++k1;
            }
        }

        return true;
    }

    private static boolean replaceable(BlockState state, Block silt, Block rock) {
        return state.isAir() || state.is(silt) || state.is(rock);
    }
}
