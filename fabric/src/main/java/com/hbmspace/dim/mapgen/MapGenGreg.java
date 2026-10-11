// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-FileCopyrightText: 2024 George Paton
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.dim.mapgen;

import com.hbmspace.dim.BiomeGenBaseCelestial;
import com.hbmspace.dim.legacy.LegacyMath;
import com.hbmspace.dim.legacy.LegacyWorld;
import com.hbmspace.dim.legacy.MapGenCaves;
import com.hbmspace.dim.legacy.NoiseGeneratorOctaves;
import com.hbmspace.dim.noise.NoiseCaveGenerator;
import java.util.Random;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class MapGenGreg extends MapGenCaves {

    private static final float[] BIOME_WEIGHT_TABLE = new float[25];

    static {
        for (int j = -2; j <= 2; ++j) {
            for (int k = -2; k <= 2; ++k) {
                BIOME_WEIGHT_TABLE[j + 2 + (k + 2) * 5] =
                        10.0F / (float) Math.sqrt((float) (j * j + k * k) + 0.2F);
            }
        }
    }

    public static final class Noise {
        final NoiseGeneratorOctaves lower;
        final NoiseGeneratorOctaves upper;
        final NoiseGeneratorOctaves interpolation;
        final NoiseGeneratorOctaves depth;
        final NoiseCaveGenerator caves;

        public Noise(long seed) {
            Random rand = new Random(seed);
            this.lower = new NoiseGeneratorOctaves(rand, 16);
            this.upper = new NoiseGeneratorOctaves(rand, 16);
            this.interpolation = new NoiseGeneratorOctaves(rand, 8);
            this.depth = new NoiseGeneratorOctaves(rand, 16);
            this.caves = new NoiseCaveGenerator(rand);
        }
    }

    public Block stoneBlock = Blocks.STONE;

    private final Noise noise;

    public MapGenGreg(Noise noise) {
        this.noise = noise;
    }

    @Override
    public void generate(LegacyWorld world, int chunkX, int chunkZ, BlockState[] blocks) {
        super.generate(world, chunkX, chunkZ, blocks);
        this.generateNoiseCaves(chunkX, chunkZ, blocks);
    }

    private void generateNoiseCaves(int chunkX, int chunkZ, BlockState[] blocks) {
        double[] caveNoise = generateNoiseCavesNoise(chunkX, chunkZ);

        for (int noiseX = 0; noiseX < 4; ++noiseX) {
            int ix0 = noiseX * 5;
            int ix1 = (noiseX + 1) * 5;

            for (int noiseZ = 0; noiseZ < 4; ++noiseZ) {
                int ix0z0 = (ix0 + noiseZ) * 33;
                int ix0z1 = (ix0 + noiseZ + 1) * 33;
                int ix1z0 = (ix1 + noiseZ) * 33;
                int ix1z1 = (ix1 + noiseZ + 1) * 33;

                for (int noiseY = 0; noiseY < 32; ++noiseY) {
                    double x0z0 = caveNoise[ix0z0 + noiseY];
                    double x0z1 = caveNoise[ix0z1 + noiseY];
                    double x1z0 = caveNoise[ix1z0 + noiseY];
                    double x1z1 = caveNoise[ix1z1 + noiseY];
                    double x0z0Add = (caveNoise[ix0z0 + noiseY + 1] - x0z0) * 0.125D;
                    double x0z1Add = (caveNoise[ix0z1 + noiseY + 1] - x0z1) * 0.125D;
                    double x1z0Add = (caveNoise[ix1z0 + noiseY + 1] - x1z0) * 0.125D;
                    double x1z1Add = (caveNoise[ix1z1 + noiseY + 1] - x1z1) * 0.125D;

                    for (int pieceY = 0; pieceY < 8; ++pieceY) {
                        double z0 = x0z0;
                        double z1 = x0z1;
                        double z0Add = (x1z0 - x0z0) * 0.25D;
                        double z1Add = (x1z1 - x0z1) * 0.25D;

                        for (int pieceX = 0; pieceX < 4; ++pieceX) {
                            int index =
                                    pieceX + noiseX * 4 << 12
                                            | noiseZ * 4 << 8
                                            | noiseY * 8 + pieceY;
                            short idAdd = 256;
                            index -= idAdd;
                            double densityAdd = (z1 - z0) * 0.25D;
                            double density = z0 - densityAdd;

                            for (int pieceZ = 0; pieceZ < 4; ++pieceZ) {
                                index += idAdd;
                                if ((density += densityAdd) < 0) {
                                    int y = noiseY * 8 + pieceY;
                                    if (y > 0) {
                                        if (!is(blocks[index], Blocks.BEDROCK)) {
                                            blocks[index] = null;
                                        }
                                    }
                                }
                            }

                            z0 += z0Add;
                            z1 += z1Add;
                        }

                        x0z0 += x0z0Add;
                        x0z1 += x0z1Add;
                        x1z0 += x1z0Add;
                        x1z1 += x1z1Add;
                    }
                }
            }
        }
    }

    private double[] generateNoiseCavesNoise(int chunkX, int chunkZ) {
        int cx = chunkX * 4, cz = chunkZ * 4;
        double[] depthNoises =
                this.noise.depth.generateNoiseOctaves(null, cx, cz, 5, 5, 200.0D, 200.0D, 0.5D);
        double[] interpolationNoises =
                this.noise.interpolation.generateNoiseOctaves(
                        null,
                        cx,
                        0,
                        cz,
                        5,
                        33,
                        5,
                        8.555150000000001D,
                        4.277575000000001D,
                        8.555150000000001D);
        double[] lowerInterpolatedNoises =
                this.noise.lower.generateNoiseOctaves(
                        null, cx, 0, cz, 5, 33, 5, 684.412D, 684.412D, 684.412D);
        double[] upperInterpolatedNoises =
                this.noise.upper.generateNoiseOctaves(
                        null, cx, 0, cz, 5, 33, 5, 684.412D, 684.412D, 684.412D);
        BiomeGenBaseCelestial[] biomes =
                this.worldObj.getBiomesForGeneration(cx - 2, cz - 2, 10, 10);
        double[] caveNoise = new double[825];
        int i = 0, j = 0;

        for (int x = 0; x < 5; ++x) {
            for (int z = 0; z < 5; ++z) {
                float scale = 0.0F;
                float depth = 0.0F;
                float weight = 0.0F;
                double lowestScaledDepth = 0;

                BiomeGenBaseCelestial biome0 = biomes[x + 2 + (z + 2) * 10];
                for (int x1 = -2; x1 <= 2; ++x1) {
                    for (int z1 = -2; z1 <= 2; ++z1) {
                        BiomeGenBaseCelestial biome = biomes[x + x1 + 2 + (z + z1 + 2) * 10];
                        float depthHere = biome.rootHeight;
                        float scaleHere = biome.heightVariation;

                        float weightHere =
                                BIOME_WEIGHT_TABLE[x1 + 2 + (z1 + 2) * 5] / (depthHere + 2.0F);

                        if (biome.rootHeight > biome0.rootHeight) {
                            weightHere /= 2.0F;
                        }

                        scale += scaleHere * weightHere;
                        depth += depthHere * weightHere;
                        weight += weightHere;
                        lowestScaledDepth = Math.min(lowestScaledDepth, biome.rootHeight);
                    }
                }
                scale /= weight;
                depth /= weight;
                scale = scale * 0.9F + 0.1F;
                depth = (depth * 4.0F - 1.0F) / 8.0F;
                double depthNoise = depthNoises[j] / 8000;

                if (depthNoise < 0.0D) {
                    depthNoise = -depthNoise * 0.3D;
                }

                depthNoise = depthNoise * 3.0D - 2.0D;

                if (depthNoise < 0.0D) {
                    depthNoise /= 2.0D;

                    if (depthNoise < -1.0D) {
                        depthNoise = -1.0D;
                    }

                    depthNoise /= 1.4D;
                    depthNoise /= 2.0D;
                } else {
                    if (depthNoise > 1.0D) {
                        depthNoise = 1.0D;
                    }

                    depthNoise /= 8.0D;
                }

                ++j;
                double scaledDepth = depth;
                double scaledScale = scale;
                scaledDepth += depthNoise * 0.2D;
                scaledDepth = scaledDepth * 8.5D / 8.0D;
                double terrainHeight = 8.5D + scaledDepth * 4.0D;

                double startLevel = 56 + (lowestScaledDepth * 20);
                int sub = (int) (startLevel / 8);

                for (int y = 0; y < 33; y++) {
                    double falloff =
                            ((double) y - terrainHeight) * 12.0D * 128.0D / 256.0D / scaledScale;

                    if (falloff < 0.0D) {
                        falloff *= 4.0D;
                    }

                    double lowerNoise = lowerInterpolatedNoises[i] / 512.0D;
                    double upperNoise = upperInterpolatedNoises[i] / 512.0D;
                    double interpolation = (interpolationNoises[i] / 10.0D + 1.0D) / 2.0D;
                    double noise =
                            LegacyMath.denormalizeClamp(lowerNoise, upperNoise, interpolation)
                                    - falloff;

                    if (y > 29) {
                        double lerp = (float) (y - 29) / 3.0F;
                        noise = noise * (1.0D - lerp) + -10.0D * lerp;
                    }

                    double caveHere =
                            this.noise.caves.sample(
                                    noise, y * 8, chunkZ * 16 + (z * 4), chunkX * 16 + (x * 4));

                    double surfaceGuard = (lowestScaledDepth * -30) + 20;
                    double delta = (y - sub + 2) / 2.0;
                    caveHere =
                            delta < 0.0D
                                    ? caveHere
                                    : delta > 1.0D
                                            ? surfaceGuard
                                            : caveHere + delta * (surfaceGuard - caveHere);

                    caveNoise[i] = caveHere;
                    i++;
                }
            }
        }

        return caveNoise;
    }

    @Override
    protected void digBlock(
            BlockState[] data,
            int index,
            int x,
            int y,
            int z,
            int chunkX,
            int chunkZ,
            boolean foundTop) {
        BiomeGenBaseCelestial biome =
                worldObj.getBiomeGenForCoords(x + chunkX * 16, z + chunkZ * 16);
        Block top = biome.topBlock;
        Block filler = biome.fillerBlock;
        BlockState block = data[index];

        if (is(block, stoneBlock) || is(block, filler) || is(block, top)) {
            if (y < 2) {
                data[index] = Blocks.LAVA.defaultBlockState();
            } else {
                data[index] = null;
                if (foundTop && is(data[index - 1], filler)) {
                    data[index - 1] = top.defaultBlockState();
                }
            }
        }
    }
}
