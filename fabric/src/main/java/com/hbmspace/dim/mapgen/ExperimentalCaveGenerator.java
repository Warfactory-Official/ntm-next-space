// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-FileCopyrightText: 2024-2025 George Paton
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.dim.mapgen;

import com.hbmspace.dim.BiomeGenBaseCelestial;
import com.hbmspace.dim.legacy.LegacyMath;
import com.hbmspace.dim.legacy.LegacyWorld;
import com.hbmspace.dim.legacy.MapGenBase;
import com.hbmspace.dim.noise.PerlinFestival;
import java.util.Random;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

public class ExperimentalCaveGenerator extends MapGenBase {

    public Block lavaBlock = Blocks.LAVA;
    public Block stoneBlock = Blocks.STONE;

    private final float carvingScale;
    private final int depthThreshold;
    private final float largeCaveSize;

    public float smallCaveSize = 1.0F;

    public @Nullable BiomeGenBaseCelestial ignoreBiome;
    public @Nullable BiomeGenBaseCelestial onlyBiome;

    public ExperimentalCaveGenerator(float carvingScale, int depthThreshold, float largeCaveSize) {
        this.carvingScale = carvingScale;
        this.depthThreshold = depthThreshold;
        this.largeCaveSize = largeCaveSize;
    }

    protected void generateLargeCave(
            long seed, int chunkX, int chunkZ, BlockState[] blocks, double x, double y, double z) {
        this.generateCaveNode(
                seed,
                chunkX,
                chunkZ,
                blocks,
                x,
                y,
                z,
                (1.0F + this.rand.nextFloat()) * largeCaveSize,
                0.0F,
                0.0F,
                -1,
                -1,
                0.5D);
    }

    protected void generateCaveNode(
            long seed,
            int chunkX,
            int chunkZ,
            BlockState[] blocks,
            double x,
            double y,
            double z,
            float caveSize,
            float yaw,
            float pitch,
            int currentStep,
            int totalSteps,
            double caveSizeIncrease) {
        double centerX = (double) (chunkX * 16 + 8);
        double centerZ = (double) (chunkZ * 16 + 8);
        float f3 = 0.0F;
        float f4 = 0.0F;
        Random random = new Random(seed);
        PerlinFestival perlinNoise = new PerlinFestival(seed);

        if (totalSteps <= 0) {
            int j1 = this.range * 16 - 16;
            totalSteps = j1 - random.nextInt(j1 / 4);
        }

        boolean flag2 = false;

        if (currentStep == -1) {
            currentStep = totalSteps / 2;
            flag2 = true;
        }

        int k1 = random.nextInt(totalSteps / 2) + totalSteps / 4;

        for (boolean flag = random.nextInt(6) == 0; currentStep < totalSteps; ++currentStep) {
            double d6 =
                    1.5D
                            + (double)
                                    (LegacyMath.sin(
                                                    (float) currentStep
                                                            * (float) Math.PI
                                                            / (float) totalSteps)
                                            * caveSize
                                            * 1.0F);
            double d7 = d6 * caveSizeIncrease;
            float f5 = LegacyMath.cos(pitch);
            float f6 = LegacyMath.sin(pitch);
            x += (double) (LegacyMath.cos(yaw) * f5);
            y += (double) f6;
            z += (double) (LegacyMath.sin(yaw) * f5);

            double dynamicScale = 3 + Math.sin(currentStep * 4);
            double noiseFactor =
                    perlinNoise.noise(x * dynamicScale, y * dynamicScale, z * dynamicScale);
            if (y < depthThreshold) {
                double noiseFactorWidth = perlinNoise.noise(x * 0.1, y * 0.1, z * 0.1);
                double noiseFactorHeight = perlinNoise.noise(x * 0.1, y * 0.2, z * 0.1);

                d6 *= 3 + noiseFactorWidth;
                d7 *= 2 + noiseFactorHeight;
                pitch += noiseFactor * LegacyMath.sin((float) (random.nextFloat() * Math.PI));
                yaw += noiseFactor * LegacyMath.sin((float) (random.nextInt() * Math.PI));
            }

            if (flag) {
                pitch *= 0.92F;
            } else {
                pitch *= 0.7F;
            }

            pitch += f4 * 0.1F;
            yaw += f3 * 0.1F;
            f4 *= 0.9F;
            f3 *= 0.75F;
            f4 += (random.nextFloat() - random.nextFloat()) * random.nextFloat() * 2.0F;
            f3 += (random.nextFloat() - random.nextFloat()) * random.nextFloat() * 4.0F;

            if (!flag2 && currentStep == k1 && caveSize > 1.0F && totalSteps > 0) {
                this.generateCaveNode(
                        random.nextLong(),
                        chunkX,
                        chunkZ,
                        blocks,
                        x,
                        y,
                        z,
                        random.nextFloat() * 0.5F + 0.5F,
                        yaw - ((float) Math.PI / 2F),
                        pitch / 3.0F,
                        currentStep,
                        totalSteps,
                        1.0D);
                this.generateCaveNode(
                        random.nextLong(),
                        chunkX,
                        chunkZ,
                        blocks,
                        x,
                        y,
                        z,
                        random.nextFloat() * 0.5F + 0.5F,
                        yaw + ((float) Math.PI / 2F),
                        pitch / 3.0F,
                        currentStep,
                        totalSteps,
                        1.0D);
                return;
            }

            if (flag2 || random.nextInt(4) != 0) {
                double d8 = x - centerX;
                double d9 = z - centerZ;
                double d10 = (double) (totalSteps - currentStep);
                double d11 = (double) (caveSize + 2.0F + 16.0F);

                if (d8 * d8 + d9 * d9 - d10 * d10 > d11 * d11) {
                    return;
                }

                if (x >= centerX - 16.0D - d6 * 2.0D
                        && z >= centerZ - 16.0D - d6 * 2.0D
                        && x <= centerX + 16.0D + d6 * 2.0D
                        && z <= centerZ + 16.0D + d6 * 2.0D) {
                    int i4 = Mth.floor(x - d6) - chunkX * 16 - 1;
                    int l1 = Mth.floor(x + d6) - chunkX * 16 + 1;
                    int j4 = Mth.floor(y - d7) - 1;
                    int i2 = Mth.floor(y + d7) + 1;
                    int k4 = Mth.floor(z - d6) - chunkZ * 16 - 1;
                    int j2 = Mth.floor(z + d6) - chunkZ * 16 + 1;

                    if (i4 < 0) {
                        i4 = 0;
                    }

                    if (l1 > 16) {
                        l1 = 16;
                    }

                    if (j4 < 1) {
                        j4 = 1;
                    }

                    if (i2 > 248) {
                        i2 = 248;
                    }

                    if (k4 < 0) {
                        k4 = 0;
                    }

                    if (j2 > 16) {
                        j2 = 16;
                    }

                    boolean flag3 = false;
                    int k2;
                    int j3;

                    for (k2 = i4; !flag3 && k2 < l1; ++k2) {
                        for (int l2 = k4; !flag3 && l2 < j2; ++l2) {
                            for (int i3 = i2 + 1; !flag3 && i3 >= j4 - 1; --i3) {
                                j3 = (k2 * 16 + l2) * 256 + i3;

                                if (i3 >= 0 && i3 < 256) {
                                    if (isOceanBlock(blocks, j3)) {
                                        flag3 = true;
                                    }

                                    if (i3 != j4 - 1
                                            && k2 != i4
                                            && k2 != l1 - 1
                                            && l2 != k4
                                            && l2 != j2 - 1) {
                                        i3 = j4;
                                    }
                                }
                            }
                        }
                    }

                    if (!flag3) {
                        double noiseScaleX = 0.05;
                        double noiseScaleZ = 0.05;
                        double noiseMagnitude = 0.3;
                        for (k2 = i4; k2 < l1; ++k2) {
                            double noiseX =
                                    perlinNoise.noise(
                                                    (k2 + chunkX * 16) * noiseScaleX,
                                                    y * noiseScaleX,
                                                    z * noiseScaleX)
                                            * noiseMagnitude;
                            double d13 = (((double) (k2 + chunkX * 16) + 0.5D - x) / d6) + noiseX;

                            for (j3 = k4; j3 < j2; ++j3) {
                                double noiseZ =
                                        perlinNoise.noise(
                                                        x * noiseScaleZ,
                                                        y * noiseScaleZ,
                                                        (j3 + chunkZ * 16) * noiseScaleZ)
                                                * noiseMagnitude;
                                double d14 =
                                        (((double) (j3 + chunkZ * 16) + 0.5D - z) / d6) + noiseZ;

                                int k3 = (k2 * 16 + j3) * 256 + i2;
                                boolean flag1 = false;

                                if (d13 * d13 + d14 * d14 < 1.0D) {
                                    for (int l3 = i2 - 1; l3 >= j4; --l3) {
                                        double d12 = ((double) l3 + 0.5D - y) / d7;

                                        if (d12 > -0.7D
                                                && (d13 * d13 + d12 * d12 + d14 * d14)
                                                                * (1 + noiseFactor)
                                                        < 1.0D) {
                                            if (isTopBlock(blocks, k3, k2, j3, chunkX, chunkZ)) {
                                                flag1 = true;
                                            }
                                            double carvingNoise =
                                                    perlinNoise.noise(
                                                            x * carvingScale,
                                                            y * carvingScale,
                                                            z * carvingScale);
                                            double noiseValue =
                                                    perlinNoise.noise(
                                                            x * caveSizeIncrease,
                                                            y * caveSizeIncrease,
                                                            z * caveSizeIncrease);
                                            if ((d13 * d13 + d12 * d12 + d14 * d14)
                                                            * (1 + noiseValue)
                                                    < 1.0D + carvingNoise) {
                                                if (Math.pow(d13, 2.5) + Math.pow(d14, 2.2) < 2) {
                                                    digBlock(
                                                            blocks, k3, k2, l3, j3, chunkX, chunkZ,
                                                            flag1);
                                                }
                                            }
                                            digBlock(blocks, k3, k2, l3, j3, chunkX, chunkZ, flag1);
                                        }

                                        --k3;
                                    }
                                }
                            }
                        }

                        if (flag2) {
                            break;
                        }
                    }
                }
            }
        }
    }

    @Override
    protected void recursiveGenerate(
            LegacyWorld world,
            int offsetX,
            int offsetZ,
            int chunkX,
            int chunkZ,
            BlockState[] blocks) {
        int i1 = this.rand.nextInt(this.rand.nextInt(this.rand.nextInt(15) + 1) + 1);

        if (this.rand.nextInt(7) != 0) {
            i1 = 0;
        }

        for (int j1 = 0; j1 < i1; ++j1) {
            double d0 = (double) (offsetX * 16 + this.rand.nextInt(16));
            double d1 = (double) this.rand.nextInt(this.rand.nextInt(120) + 8);
            double d2 = (double) (offsetZ * 16 + this.rand.nextInt(16));
            int k1 = 1;

            if (this.rand.nextInt(4) == 0) {
                this.generateLargeCave(this.rand.nextLong(), chunkX, chunkZ, blocks, d0, d1, d2);
                k1 += this.rand.nextInt(4);
            }

            for (int l1 = 0; l1 < k1; ++l1) {
                float f = this.rand.nextFloat() * (float) Math.PI * 2.0F;
                float f1 = (this.rand.nextFloat() - 0.5F) * 2.0F / 8.0F;
                float f2 = this.rand.nextFloat() * 2.0F + this.rand.nextFloat();

                if (this.rand.nextInt(10) == 0) {
                    f2 *=
                            smallCaveSize * this.rand.nextFloat() * this.rand.nextFloat() * 3.0F
                                    + 1.0F;
                }

                this.generateCaveNode(
                        this.rand.nextLong(),
                        chunkX,
                        chunkZ,
                        blocks,
                        d0,
                        d1,
                        d2,
                        f2,
                        f,
                        f1,
                        0,
                        0,
                        1.0D);
            }
        }
    }

    protected boolean isOceanBlock(BlockState[] data, int index) {
        return data[index] != null && !data[index].getFluidState().isEmpty();
    }

    private boolean isTopBlock(BlockState[] data, int index, int x, int z, int chunkX, int chunkZ) {
        BiomeGenBaseCelestial biome =
                worldObj.getBiomeGenForCoords(x + chunkX * 16, z + chunkZ * 16);
        return is(data[index], biome.topBlock);
    }

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
        if (biome == ignoreBiome) return;
        if (onlyBiome != null && biome != onlyBiome) return;

        BlockState block = data[index];

        if (block != null
                && (is(block, stoneBlock)
                        || is(block, biome.fillerBlock)
                        || is(block, biome.topBlock)
                        || !block.getFluidState().isEmpty())) {
            if (y < 10) {
                data[index] = lavaBlock.defaultBlockState();
            } else {
                data[index] = null;

                if (foundTop && is(data[index - 1], biome.fillerBlock)) {
                    data[index - 1] = biome.topBlock.defaultBlockState();
                }
            }
        }
    }
}
