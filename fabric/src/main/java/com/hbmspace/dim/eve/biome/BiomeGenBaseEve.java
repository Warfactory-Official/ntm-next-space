// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-FileCopyrightText: 2025 George Paton
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.dim.eve.biome;

import com.hbm.lib.Library;
import com.hbmspace.HbmSpace;
import com.hbmspace.blocks.TerrainBlocksSpace;
import com.hbmspace.dim.BiomeDecoratorCelestial;
import com.hbmspace.dim.BiomeGenBaseCelestial;
import com.hbmspace.dim.legacy.LegacyWorld;
import java.util.Random;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public abstract class BiomeGenBaseEve extends BiomeGenBaseCelestial {
    public static final BiomeGenBaseCelestial evePlains = new BiomeGenEvePlains();
    public static final BiomeGenBaseCelestial eveOcean = new BiomeGenEveOcean();
    public static final BiomeGenBaseCelestial eveMountains = new BiomeGenEveMountains();
    public static final BiomeGenBaseCelestial eveSeismicPlains = new BiomeGenEveSeismicPlains();
    public static final BiomeGenBaseCelestial eveRiver = new BiomeGenEveRiver();

    protected final Block eveRock = TerrainBlocksSpace.EVE_ROCK.get();
    protected final Block basalt =
            BuiltInRegistries.BLOCK.getOptional(Library.id("basalt")).orElseThrow();
    protected final Block basaltSmooth =
            BuiltInRegistries.BLOCK.getOptional(Library.id("basalt_smooth")).orElseThrow();

    protected BiomeGenBaseEve(String name, int id) {
        super(Identifier.fromNamespaceAndPath(HbmSpace.MOD_ID, name), id);
        this.waterColorMultiplier = 0x5b009a;
        this.setTemperatureRainfall(1.0F, 0.5F);

        this.theBiomeDecorator = new BiomeDecoratorCelestial(eveRock);

        this.topBlock = TerrainBlocksSpace.EVE_SILT.get();
        this.fillerBlock = eveRock;
    }

    protected void genHighlandBlocks(
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
                                blocks[i] = block;
                            } else {
                                block = null;
                                block1 = eveRock.defaultBlockState();
                                blocks[i] =
                                        (columnRandom(world, x, by, z) > 0.4
                                                        ? eveRock
                                                        : basaltSmooth)
                                                .defaultBlockState();
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

    protected static void genBedrockFloor(Random rand, BlockState[] blocks, int x, int z) {
        rand.nextDouble();
        int bx = x & 15;
        int bz = z & 15;
        int s = blocks.length / 256;

        for (int by = 255; by >= 0; --by) {
            if (by <= rand.nextInt(5))
                blocks[(bz * 16 + bx) * s + by] = Blocks.BEDROCK.defaultBlockState();
        }
    }
}
