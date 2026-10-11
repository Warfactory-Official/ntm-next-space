// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-FileCopyrightText: 2024 George Paton
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.dim.duna.biome;

import com.hbmspace.blocks.ModBlocksSpace;
import com.hbmspace.blocks.TerrainBlocksSpace;
import com.hbmspace.dim.legacy.LegacyWorld;
import com.hbmspace.dim.legacy.NoiseGeneratorPerlin;
import java.util.Arrays;
import java.util.Random;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

public class BiomeGenDunaHills extends BiomeGenBaseDuna {
    private static final Block[] TERRACOTTA = new Block[16];

    static {
        for (int meta = 0; meta < 16; meta++)
            TERRACOTTA[meta] = Blocks.DYED_TERRACOTTA.pick(DyeColor.byId(meta));
    }

    private record Bands(long seed, byte[] bands, NoiseGeneratorPerlin perlin) {}

    private volatile @Nullable Bands bands;

    BiomeGenDunaHills() {
        super("duna_hills", 119);
        this.setBiomeName("Weathered Dunaian Hills");
        this.setHeight(new Height(0.525F, 0.51F));
    }

    @Override
    public void genTerrainBlocks(
            LegacyWorld world, Random rand, BlockState[] blocks, int x, int z, double noise) {
        Bands bands = bands(world.getSeed());
        Block rock = TerrainBlocksSpace.DUNA_ROCK.get();
        BlockState rockState = rock.defaultBlockState();
        BlockState ferric = ModBlocksSpace.FERRIC_CLAY.get().defaultBlockState();
        BlockState calcium = ModBlocksSpace.STONE_RESOURCE_CALCIUM.get().defaultBlockState();

        int k = x & 15;
        int l = z & 15;
        BlockState block2 = this.fillerBlock.defaultBlockState();
        int i1 = (int) (noise / 3.0D + 3.0D + rand.nextDouble() * 0.25D);
        boolean flag1 = Math.cos(noise / 3.0D * Math.PI) > 0.0D;
        int j1 = -1;
        boolean flag2 = false;
        int k1 = blocks.length / 256;

        for (int l1 = 255; l1 >= 0; --l1) {
            int i2 = (l * 16 + k) * k1 + l1;

            if (l1 <= rand.nextInt(5)) {
                blocks[i2] = Blocks.BEDROCK.defaultBlockState();
            } else {
                BlockState block1 = blocks[i2];

                if (block1 != null && !block1.isAir()) {
                    if (block1.getBlock() == rock) {
                        if (j1 == -1) {
                            flag2 = false;

                            if (i1 <= 0) {
                                block2 = rockState;
                            } else if (l1 >= 59 && l1 <= 64) {
                                block2 = this.fillerBlock.defaultBlockState();
                            }

                            j1 = i1 + Math.max(0, l1 - 63);

                            if (l1 >= 62) {
                                if (l1 > 66 + i1) {
                                    int b0 = 16;

                                    if (l1 >= 64 && l1 <= 127) {
                                        if (!flag1) {
                                            b0 = band(bands, x, l1);
                                        }
                                    } else {
                                        b0 = 1;
                                    }

                                    blocks[i2] =
                                            b0 < 16
                                                    ? TERRACOTTA[DyeColor.RED.getId()]
                                                            .defaultBlockState()
                                                    : rockState;
                                } else {
                                    blocks[i2] = this.topBlock.defaultBlockState();
                                    flag2 = true;
                                }
                            } else {
                                blocks[i2] = block2;
                            }
                        } else if (j1 > 0) {
                            --j1;

                            if (flag2) {
                                blocks[i2] = ferric;
                            } else {
                                int b0 = band(bands, x, l1);

                                if (b0 == 4) {
                                    blocks[i2] = ferric;
                                } else if (b0 == 0) {
                                    blocks[i2] = calcium;
                                } else if (b0 < 16) {
                                    blocks[i2] = TERRACOTTA[b0].defaultBlockState();
                                } else {
                                    blocks[i2] = rockState;
                                }
                            }
                        }
                    }
                } else {
                    j1 = -1;
                }
            }
        }
    }

    private Bands bands(long seed) {
        Bands current = bands;
        if (current == null || current.seed() != seed) {
            current = generateBands(seed);
            bands = current;
        }
        return current;
    }

    private static Bands generateBands(long seed) {
        byte[] bands = new byte[64];
        Arrays.fill(bands, (byte) 16);
        Random random = new Random(seed);
        NoiseGeneratorPerlin perlin = new NoiseGeneratorPerlin(random, 1);
        int j;

        for (j = 0; j < 64; ++j) {
            j += random.nextInt(5) + 1;

            if (j < 64) {
                bands[j] = 1;
            }
        }

        j = random.nextInt(4) + 2;
        int k;
        int l;
        int i1;
        int j1;

        for (k = 0; k < j; ++k) {
            l = random.nextInt(3) + 1;
            i1 = random.nextInt(64);

            for (j1 = 0; i1 + j1 < 64 && j1 < l; ++j1) {
                bands[i1 + j1] = 4;
            }
        }

        k = random.nextInt(4) + 2;
        int k1;

        for (l = 0; l < k; ++l) {
            i1 = random.nextInt(3) + 2;
            j1 = random.nextInt(64);

            for (k1 = 0; j1 + k1 < 64 && k1 < i1; ++k1) {
                bands[j1 + k1] = 12;
            }
        }

        l = random.nextInt(4) + 2;

        for (i1 = 0; i1 < l; ++i1) {
            j1 = random.nextInt(3) + 1;
            k1 = random.nextInt(64);

            for (int l1 = 0; k1 + l1 < 64 && l1 < j1; ++l1) {
                bands[k1 + l1] = 14;
            }
        }

        i1 = random.nextInt(3) + 3;
        j1 = 0;

        for (k1 = 0; k1 < i1; ++k1) {
            byte b0 = 1;
            j1 += random.nextInt(16) + 4;

            for (int i2 = 0; j1 + i2 < 64 && i2 < b0; ++i2) {
                bands[j1 + i2] = 0;

                if (j1 + i2 > 1 && random.nextBoolean()) {
                    bands[j1 + i2 - 1] = 8;
                }

                if (j1 + i2 < 63 && random.nextBoolean()) {
                    bands[j1 + i2 + 1] = 8;
                }
            }
        }

        return new Bands(seed, bands, perlin);
    }

    private static int band(Bands bands, int x, int y) {
        double at = (double) x * 1.0D / 512.0D;
        int l = (int) Math.round(bands.perlin().getValue(at, at) * 2.0D);
        return bands.bands()[(y + l + 64) % 64];
    }
}
