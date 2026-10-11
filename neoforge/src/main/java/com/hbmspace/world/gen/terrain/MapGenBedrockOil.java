// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-FileCopyrightText: 2025-2026 George Paton
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.world.gen.terrain;

import com.hbm.blocks.ModBlocks;
import com.hbm.world.LocationIsValidSpawn;
import com.hbmspace.blocks.FloraBlocksSpace;
import com.hbmspace.blocks.TerrainBlocksSpace;
import com.hbmspace.dim.legacy.LegacyWorld;
import com.hbmspace.dim.legacy.MapGenBase;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class MapGenBedrockOil extends MapGenBase {

    private final int frequency;

    public BlockState block;
    public Block replace = Blocks.STONE;

    public int spotWidth = 5;
    public int spotCount = 50;
    public boolean addWillows = true;

    public MapGenBedrockOil(int frequency) {
        this.frequency = frequency;
        this.range = 4;
    }

    @Override
    protected void recursiveGenerate(
            LegacyWorld world,
            int offsetX,
            int offsetZ,
            int chunkX,
            int chunkZ,
            BlockState[] blocks) {
        if (rand.nextInt(frequency) != frequency - 2) return;

        int xCoord = (chunkX - offsetX) * 16 + rand.nextInt(16);
        int zCoord = (chunkZ - offsetZ) * 16 + rand.nextInt(16);

        for (int bx = 15; bx >= 0; bx--) {
            for (int bz = 15; bz >= 0; bz--) {
                for (int y = 0; y < 5; y++) {
                    int index = (bx * 16 + bz) * 256 + y;

                    if (is(blocks[index], replace) || is(blocks[index], Blocks.BEDROCK)) {
                        int x = xCoord + bx;
                        int z = zCoord + bz;

                        if (Math.abs(x) < 5
                                && Math.abs(z) < 5
                                && Math.abs(x) + Math.abs(y) + Math.abs(z) <= 6) {
                            blocks[index] = block;
                        }
                    }
                }
            }
        }

        for (int i = 0; i < spotCount; i++) {
            int rx = (int) (rand.nextGaussian() * spotWidth) - xCoord;
            int rz = (int) (rand.nextGaussian() * spotWidth) - zCoord;

            if (rx >= 0 && rx < 16 && rz >= 0 && rz < 16) {
                for (int y = 127; y >= 4; y--) {
                    int index = (rx * 16 + rz) * 256 + y;

                    if (blocks[index] != null && blocks[index].isSolidRender()) {
                        for (int oy = 1; oy > -3; oy--) {
                            int subIndex = index + oy;
                            BlockState sub = blocks[subIndex];

                            if (sub != null
                                    && (sub.is(Blocks.GRASS_BLOCK)
                                            || LocationIsValidSpawn.isDirtFamily(sub))) {
                                blocks[subIndex] =
                                        (rand.nextInt(10) == 0
                                                        ? ModBlocks.DIRT_OILY
                                                        : ModBlocks.DIRT_DEAD)
                                                .get()
                                                .defaultBlockState();

                                if (addWillows && oy == 0 && rand.nextInt(50) == 0) {
                                    blocks[subIndex + 1] =
                                            ModBlocks.PLANT_FLOWER_CD0.get().defaultBlockState();
                                }

                                if (oy == 0 && rand.nextInt(20) == 0) {
                                    blocks[subIndex + 1] = deadPlant(rand.nextInt(5));
                                }

                                break;
                            } else if (sub != null && sub.is(Blocks.RED_SAND)) {
                                blocks[subIndex] =
                                        ModBlocks.SAND_DIRTY_RED.get().defaultBlockState();
                                break;
                            } else if (sub != null
                                    && (sub.is(Blocks.SAND)
                                            || sub.is(ModBlocks.ORE_OIL_SAND.get()))) {
                                blocks[subIndex] = ModBlocks.SAND_DIRTY.get().defaultBlockState();
                                break;
                            } else if (sub != null && sub.is(Blocks.STONE)) {
                                blocks[subIndex] =
                                        ModBlocks.STONE_CRACKED.get().defaultBlockState();
                                break;
                            } else if (sub != null
                                    && (sub.is(TerrainBlocksSpace.RUBBER_SILT.get())
                                            || sub.is(FloraBlocksSpace.RUBBER_GRASS.get())
                                            || sub.is(TerrainBlocksSpace.VINYL_SAND.get()))) {
                                blocks[subIndex] =
                                        ModBlocks.SELLAFIELD_SLAKED.get().defaultBlockState();
                                break;
                            }
                        }

                        break;
                    }
                }
            }
        }
    }

    private static BlockState deadPlant(int type) {
        return (switch (type) {
                    case 1 -> ModBlocks.PLANT_DEAD_GRASS;
                    case 2 -> ModBlocks.PLANT_DEAD_FLOWER;
                    case 3 -> ModBlocks.PLANT_DEAD_BIGFLOWER;
                    case 4 -> ModBlocks.PLANT_DEAD_FERN;
                    default -> ModBlocks.PLANT_DEAD;
                })
                .get()
                .defaultBlockState();
    }
}
