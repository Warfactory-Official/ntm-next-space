// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.dim.tekto.biome;

import com.hbm.lib.Library;
import com.hbmspace.HbmSpace;
import com.hbmspace.blocks.TerrainBlocksSpace;
import com.hbmspace.dim.BiomeDecoratorCelestial;
import com.hbmspace.dim.BiomeGenBaseCelestial;
import com.hbmspace.dim.WorldGenRubberPlant;
import com.hbmspace.dim.legacy.LegacyWorld;
import java.util.Random;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public abstract class BiomeGenBaseTekto extends BiomeGenBaseCelestial {

    public static final BiomeGenBaseCelestial polyvinylPlains = new BiomeGenPolyvinylPlains();
    public static final BiomeGenBaseCelestial halogenHills = new BiomeGenHalogenHills();
    public static final BiomeGenBaseCelestial tetrachloricRiver = new BiomeGenTetrachloricRiver();
    public static final BiomeGenBaseCelestial forest = new BiomeGenForest();
    public static final BiomeGenBaseCelestial vinylsands = new BiomeGenVinylSands();

    protected final Block basalt =
            BuiltInRegistries.BLOCK.getOptional(Library.id("basalt")).orElseThrow();
    protected final Block basaltSmooth =
            BuiltInRegistries.BLOCK.getOptional(Library.id("basalt_smooth")).orElseThrow();

    protected BiomeGenBaseTekto(String name, int id) {
        super(Identifier.fromNamespaceAndPath(HbmSpace.MOD_ID, name), id);
        this.waterColorMultiplier = 0x5B009A;

        this.setTemperatureRainfall(1.0F, 0.5F);

        BiomeDecoratorCelestial decorator = new BiomeDecoratorCelestial(basalt);
        decorator.rubberPlantsPerChunk = 64;
        decorator.genRPlants = new WorldGenRubberPlant();
        decorator.generateLakes = false;
        this.theBiomeDecorator = decorator;

        this.topBlock = TerrainBlocksSpace.VINYL_SAND.get();
        this.fillerBlock = TerrainBlocksSpace.VINYL_SAND.get();
    }

    @Override
    public void genTerrainBlocks(
            LegacyWorld world, Random rand, BlockState[] blocks, int x, int z, double noise) {
        genTektoTerrain(world, rand, blocks, x, z, noise, basalt, basalt, basaltSmooth);
    }

    protected void genTektoTerrain(
            LegacyWorld world,
            Random rand,
            BlockState[] blocks,
            int x,
            int z,
            double noise,
            Block rock,
            Block lowA,
            Block lowB) {
        BlockState block = this.topBlock.defaultBlockState();
        BlockState block1 = this.fillerBlock.defaultBlockState();
        int k = -1;
        int l = (int) (noise / 6.0D + 6.0D + rand.nextDouble() * 0.85D);
        int i1 = x & 15;
        int j1 = z & 15;
        int k1 = blocks.length / 256;

        for (int l1 = 255; l1 >= 0; --l1) {
            int i2 = (j1 * 16 + i1) * k1 + l1;

            if (l1 <= rand.nextInt(5)) {
                blocks[i2] = Blocks.BEDROCK.defaultBlockState();
            } else {
                BlockState block2 = blocks[i2];

                if (block2 != null && !block2.isAir()) {
                    if (block2.getBlock() == rock) {
                        if (k == -1) {
                            if (l <= 0) {
                                block = null;
                                block1 = rock.defaultBlockState();
                            } else if (l1 >= 59 && l1 <= 64) {
                                block = this.topBlock.defaultBlockState();
                                block1 = this.fillerBlock.defaultBlockState();
                            }

                            if (l1 < 63 && (block == null || block.isAir())) {
                                block = this.topBlock.defaultBlockState();
                            }

                            k = l;

                            if (l1 >= 62) {
                                blocks[i2] = block;
                            } else {
                                block = null;
                                block1 = rock.defaultBlockState();
                                blocks[i2] =
                                        (columnRandom(world, x, l1, z) > 0.4 ? lowA : lowB)
                                                .defaultBlockState();
                            }
                        } else if (k > 0) {
                            --k;
                            blocks[i2] = block1;

                            if (k == 0 && block1.getBlock() == Blocks.SAND) {
                                k = rand.nextInt(4) + Math.max(0, l1 - 63);
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
