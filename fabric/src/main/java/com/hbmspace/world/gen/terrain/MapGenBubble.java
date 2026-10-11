// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-FileCopyrightText: 2025 George Paton
// SPDX-FileCopyrightText: 2026 HbmMods (The Bobcat)
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.world.gen.terrain;

import com.hbm.blocks.ModBlocks;
import com.hbm.world.LocationIsValidSpawn;
import com.hbmspace.dim.BiomeGenBaseCelestial;
import com.hbmspace.dim.legacy.LegacyWorld;
import com.hbmspace.dim.legacy.MapGenBase;
import java.util.function.Predicate;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

public class MapGenBubble extends MapGenBase {

    private final int frequency;
    private int minSize = 8;
    private int maxSize = 64;

    public int minY = 15;
    public int rangeY = 25;

    public boolean fuzzy;

    public BlockState block;
    public Block replace = Blocks.STONE;

    public @Nullable Predicate<BiomeGenBaseCelestial> canSpawn;

    public MapGenBubble(int frequency) {
        this.frequency = frequency;
    }

    public void setSize(int minSize, int maxSize) {
        this.minSize = minSize;
        this.maxSize = maxSize;

        this.range = (maxSize / 8) + 1;
    }

    @Override
    protected void recursiveGenerate(
            LegacyWorld world,
            int offsetX,
            int offsetZ,
            int chunkX,
            int chunkZ,
            BlockState[] blocks) {
        int effecFreq = frequency;
        BiomeGenBaseCelestial biome = world.getBiomeGenForCoords(offsetX * 16, offsetZ * 16);
        if (biome.temperature >= 2 && biome.rainfall < 0.1) effecFreq /= 3;
        if (effecFreq <= 0) effecFreq = 1;

        if (rand.nextInt(effecFreq) == effecFreq - 1
                && (canSpawn == null || canSpawn.test(biome))) {
            int xCoord = (chunkX - offsetX) * 16 + rand.nextInt(16);
            int zCoord = (chunkZ - offsetZ) * 16 + rand.nextInt(16);

            int yCoord = rand.nextInt(rangeY) + minY;

            double radius = rand.nextInt(maxSize - minSize) + minSize;
            double radiusSqr = (radius * radius) / 2;

            int yMin = Math.max(1, Mth.floor(yCoord - radius));
            int yMax = Math.min(127, Mth.ceil(yCoord + radius));

            for (int bx = 15; bx >= 0; bx--) {
                for (int bz = 15; bz >= 0; bz--) {
                    for (int by = yMin; by < yMax; by++) {
                        int index = (bx * 16 + bz) * 256 + by;

                        if (is(blocks[index], replace)) {
                            int x = xCoord + bx;
                            int z = zCoord + bz;
                            int y = yCoord - by;

                            double rSqr = x * x + z * z + y * y * 3;
                            if (fuzzy) rSqr -= rand.nextDouble() * radiusSqr / 3;
                            if (rSqr < radiusSqr) {
                                blocks[index] = block;
                            }
                        }
                    }
                }
            }

            if (rand.nextInt(1) == 0) {
                addSurfaceSpot(xCoord, zCoord, blocks);
            }
        }
    }

    protected void addSurfaceSpot(int xCoord, int zCoord, BlockState[] blocks) {
        int spotCount = 150;
        int spotWidth = 7;

        for (int i = 0; i < spotCount; i++) {
            int offX = (int) (rand.nextGaussian() * spotWidth);
            int offZ = (int) (rand.nextGaussian() * spotWidth);
            int rx = offX - xCoord;
            int rz = offZ - zCoord;

            if (rx >= 0 && rx < 16 && rz >= 0 && rz < 16) {
                for (int y = 127; y >= 4; y--) {
                    int index = (rx * 16 + rz) * 256 + y;

                    if (blocks[index] != null && blocks[index].isSolidRender()) {
                        for (int oy = 1; oy > -3; oy--) {
                            int subIndex = index + oy;
                            BlockState sub = blocks[subIndex];

                            int distSq = offX * offX + offZ * offZ;
                            boolean inner = distSq < (spotWidth / 2) * (spotWidth / 2);

                            if (sub != null
                                    && (sub.is(Blocks.GRASS_BLOCK)
                                            || LocationIsValidSpawn.isDirtFamily(sub))) {
                                blocks[subIndex] =
                                        (inner ? ModBlocks.DIRT_OILY : ModBlocks.DIRT_DEAD)
                                                .get()
                                                .defaultBlockState();

                                if (!inner && oy == 0 && rand.nextInt(20) == 0) {
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
                            }
                        }

                        break;
                    }
                }
            }
        }

        BlockState cracked = ModBlocks.STONE_CRACKED.get().defaultBlockState();
        for (int i = 1; i < 6; i++) {
            Direction dir = Direction.from3DDataValue(i);
            int x = dir.getStepX() - xCoord;
            int z = dir.getStepZ() - zCoord;

            if (x >= 0 && x < 16 && z >= 0 && z < 16) {
                int solids = 0;

                for (int y = 127; y >= 0; y--) {
                    int index = (x * 16 + z) * 256 + y;
                    if (blocks[index] == null) continue;

                    if (!blocks[index].getFluidState().isEmpty()) break;

                    if (blocks[index].isSolidRender()) {
                        solids++;

                        if (i > 1) {
                            blocks[index] = cracked;
                            if (solids >= 4) break;
                        } else {
                            if (solids < 3) blocks[index] = null;
                            if (solids == 3)
                                blocks[index] = ModBlocks.OIL_SPILL.get().defaultBlockState();
                            if (solids > 3 && solids < 7) blocks[index] = cracked;
                            if (solids >= 7) break;
                        }
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
