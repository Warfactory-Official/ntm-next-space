// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.dim.legacy.feature;

import com.hbmspace.dim.BiomeGenBaseCelestial;
import com.hbmspace.dim.legacy.LegacyDecorWorld;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class WorldGenLakes {

    private final BlockState lake;

    public WorldGenLakes(BlockState lake) {
        this.lake = lake;
    }

    public boolean generate(LegacyDecorWorld world, Random rand, int x, int y, int z) {
        x -= 8;

        for (z -= 8; y > 5 && world.isAirBlock(x, y, z); --y) {}

        if (y <= 4) {
            return false;
        }

        y -= 4;
        boolean[] aboolean = new boolean[2048];
        int l = rand.nextInt(4) + 4;
        int i1;

        for (i1 = 0; i1 < l; ++i1) {
            double d0 = rand.nextDouble() * 6.0D + 3.0D;
            double d1 = rand.nextDouble() * 4.0D + 2.0D;
            double d2 = rand.nextDouble() * 6.0D + 3.0D;
            double d3 = rand.nextDouble() * (16.0D - d0 - 2.0D) + 1.0D + d0 / 2.0D;
            double d4 = rand.nextDouble() * (8.0D - d1 - 4.0D) + 2.0D + d1 / 2.0D;
            double d5 = rand.nextDouble() * (16.0D - d2 - 2.0D) + 1.0D + d2 / 2.0D;

            for (int k1 = 1; k1 < 15; ++k1) {
                for (int l1 = 1; l1 < 15; ++l1) {
                    for (int i2 = 1; i2 < 7; ++i2) {
                        double d6 = ((double) k1 - d3) / (d0 / 2.0D);
                        double d7 = ((double) i2 - d4) / (d1 / 2.0D);
                        double d8 = ((double) l1 - d5) / (d2 / 2.0D);
                        double d9 = d6 * d6 + d7 * d7 + d8 * d8;

                        if (d9 < 1.0D) {
                            aboolean[(k1 * 16 + l1) * 8 + i2] = true;
                        }
                    }
                }
            }
        }

        int j1;
        int j2;

        for (i1 = 0; i1 < 16; ++i1) {
            for (j2 = 0; j2 < 16; ++j2) {
                for (j1 = 0; j1 < 8; ++j1) {
                    if (edge(aboolean, i1, j2, j1)) {
                        BlockState state = world.getBlock(x + i1, y + j1, z + j2);

                        if (j1 >= 4 && !state.getFluidState().isEmpty()) {
                            return false;
                        }

                        if (j1 < 4 && !state.isSolid() && !state.is(lake.getBlock())) {
                            return false;
                        }
                    }
                }
            }
        }

        for (i1 = 0; i1 < 16; ++i1) {
            for (j2 = 0; j2 < 16; ++j2) {
                for (j1 = 0; j1 < 8; ++j1) {
                    if (aboolean[(i1 * 16 + j2) * 8 + j1]) {
                        world.setBlock(
                                x + i1,
                                y + j1,
                                z + j2,
                                j1 >= 4 ? Blocks.AIR.defaultBlockState() : lake,
                                Block.UPDATE_CLIENTS);
                    }
                }
            }
        }

        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (i1 = 0; i1 < 16; ++i1) {
            for (j2 = 0; j2 < 16; ++j2) {
                for (j1 = 4; j1 < 8; ++j1) {
                    if (aboolean[(i1 * 16 + j2) * 8 + j1]
                            && world.is(x + i1, y + j1 - 1, z + j2, Blocks.DIRT)
                            && world.level()
                                            .getBrightness(
                                                    LightLayer.SKY, pos.set(x + i1, y + j1, z + j2))
                                    > 0) {
                        BiomeGenBaseCelestial biome = world.getBiomeGenForCoords(x + i1, z + j2);
                        Block top =
                                biome.topBlock == Blocks.MYCELIUM
                                        ? Blocks.MYCELIUM
                                        : Blocks.GRASS_BLOCK;
                        world.setBlock(
                                x + i1,
                                y + j1 - 1,
                                z + j2,
                                top.defaultBlockState(),
                                Block.UPDATE_CLIENTS);
                    }
                }
            }
        }

        if (lake.getFluidState().is(FluidTags.LAVA)) {
            for (i1 = 0; i1 < 16; ++i1) {
                for (j2 = 0; j2 < 16; ++j2) {
                    for (j1 = 0; j1 < 8; ++j1) {
                        if (edge(aboolean, i1, j2, j1)
                                && (j1 < 4 || rand.nextInt(2) != 0)
                                && world.getBlock(x + i1, y + j1, z + j2).isSolid()) {
                            world.setBlock(
                                    x + i1,
                                    y + j1,
                                    z + j2,
                                    Blocks.STONE.defaultBlockState(),
                                    Block.UPDATE_CLIENTS);
                        }
                    }
                }
            }
        }

        if (lake.is(Blocks.WATER)) {
            for (i1 = 0; i1 < 16; ++i1) {
                for (j2 = 0; j2 < 16; ++j2) {
                    pos.set(x + i1, y + 4, z + j2);
                    if (world.level()
                            .getBiome(pos)
                            .value()
                            .shouldFreeze(world.level(), pos, false)) {
                        world.setBlock(
                                x + i1,
                                y + 4,
                                z + j2,
                                Blocks.ICE.defaultBlockState(),
                                Block.UPDATE_CLIENTS);
                    }
                }
            }
        }

        return true;
    }

    private static boolean edge(boolean[] lake, int i1, int j2, int j1) {
        return !lake[(i1 * 16 + j2) * 8 + j1]
                && (i1 < 15 && lake[((i1 + 1) * 16 + j2) * 8 + j1]
                        || i1 > 0 && lake[((i1 - 1) * 16 + j2) * 8 + j1]
                        || j2 < 15 && lake[(i1 * 16 + j2 + 1) * 8 + j1]
                        || j2 > 0 && lake[(i1 * 16 + (j2 - 1)) * 8 + j1]
                        || j1 < 7 && lake[(i1 * 16 + j2) * 8 + j1 + 1]
                        || j1 > 0 && lake[(i1 * 16 + j2) * 8 + (j1 - 1)]);
    }
}
