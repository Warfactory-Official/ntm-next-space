// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-FileCopyrightText: 2025 George Paton
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.dim.mapgen;

import com.hbmspace.dim.BiomeGenBaseCelestial;
import com.hbmspace.dim.legacy.LegacyWorld;
import com.hbmspace.dim.legacy.MapGenBase;
import com.hbmspace.dim.legacy.NoiseGeneratorPerlin;
import java.util.Random;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

public class MapGenPlateau extends MapGenBase {

    public Block stoneBlock;
    public Block surfrock;
    public Block fillblock;

    public final NoiseGeneratorPerlin plateauNoise;
    public double noiseScale = 0.05;
    public int maxPlateauAddition = 12;
    public int stepHeight = 6;
    public int topsoilThickness = 2;

    public @Nullable BiomeGenBaseCelestial applyToBiome;

    public MapGenPlateau(long seed) {
        this.plateauNoise = new NoiseGeneratorPerlin(new Random(seed), 4);
    }

    @Override
    public void generate(LegacyWorld world, int chunkX, int chunkZ, BlockState[] blocks) {
        int[][] plateauTops = new int[16][16];
        int[][] baseHeights = new int[16][16];
        for (int localX = 0; localX < 16; localX++) {
            for (int localZ = 0; localZ < 16; localZ++) {
                int baseHeight = getSurfaceHeight(blocks, localX, localZ);
                baseHeights[localX][localZ] = baseHeight;
                double noiseVal =
                        plateauNoise.getValue(
                                (chunkX * 16 + localX) * noiseScale,
                                (chunkZ * 16 + localZ) * noiseScale);
                int plateauAddition = (int) (((noiseVal + 1) / 2.0) * maxPlateauAddition);
                plateauAddition = (plateauAddition / stepHeight) * stepHeight;
                plateauTops[localX][localZ] = baseHeight + plateauAddition;
            }
        }

        for (int localX = 0; localX < 16; localX++) {
            for (int localZ = 0; localZ < 16; localZ++) {
                if (applyToBiome != null) {
                    BiomeGenBaseCelestial biome =
                            world.getBiomeGenForCoords(localX + chunkX * 16, localZ + chunkZ * 16);
                    if (biome != applyToBiome) continue;
                }
                int baseHeight = baseHeights[localX][localZ];
                int plateauTop = plateauTops[localX][localZ];
                for (int y = baseHeight + 1; y < 256; y++) {
                    int index = (localX * 16 + localZ) * 256 + y;
                    if (y < plateauTop - topsoilThickness) {
                        blocks[index] = stoneBlock.defaultBlockState();
                    } else if (y < plateauTop) {
                        boolean sameLeft =
                                (localX - 1 < 0) || (plateauTops[localX - 1][localZ] == plateauTop);
                        boolean sameRight =
                                (localX + 1 >= 16)
                                        || (plateauTops[localX + 1][localZ] == plateauTop);
                        boolean sameFront =
                                (localZ - 1 < 0) || (plateauTops[localX][localZ - 1] == plateauTop);
                        boolean sameBack =
                                (localZ + 1 >= 16)
                                        || (plateauTops[localX][localZ + 1] == plateauTop);

                        if (y == plateauTop - 1 && sameLeft && sameRight && sameFront && sameBack) {
                            blocks[index] = fillblock.defaultBlockState();
                        } else {
                            blocks[index] = surfrock.defaultBlockState();
                        }
                    } else {
                        blocks[index] = null;
                    }
                }
            }
        }
    }

    private int getSurfaceHeight(BlockState[] blocks, int localX, int localZ) {
        int baseHeight = 0;
        for (int y = 255; y >= 0; y--) {
            int index = (localX * 16 + localZ) * 256 + y;
            if (blocks[index] != null && !blocks[index].isAir()) {
                baseHeight = y;
                break;
            }
        }
        return baseHeight;
    }
}
