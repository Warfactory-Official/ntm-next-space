// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-FileCopyrightText: 2025 George Paton
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.dim.laythe.biome;

import com.hbmspace.HbmSpace;
import com.hbmspace.blocks.TerrainBlocksSpace;
import com.hbmspace.dim.BiomeDecoratorCelestial;
import com.hbmspace.dim.BiomeGenBaseCelestial;
import com.hbmspace.dim.WorldGenWaterCoral;
import com.hbmspace.dim.WorldGenWaterPlant;
import com.hbmspace.dim.legacy.LegacyWorld;
import com.hbmspace.entity.MobEntitiesSpace;
import java.util.Random;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public abstract class BiomeGenBaseLaythe extends BiomeGenBaseCelestial {

    public static final BiomeGenBaseCelestial laytheIsland = new BiomeGenLaytheIslands();
    public static final BiomeGenBaseCelestial laytheOcean = new BiomeGenLaytheOcean();
    public static final BiomeGenBaseCelestial laythePolar = new BiomeGenLaythePolar();
    public static final BiomeGenBaseCelestial laytheCoast = new BiomeGenLaytheCoast();

    protected BiomeGenBaseLaythe(String name, int id) {
        super(Identifier.fromNamespaceAndPath(HbmSpace.MOD_ID, name), id);
        this.waterColorMultiplier = 0x5b009a;

        this.waterCreatures.add(new SpawnListEntry(MobEntitiesSpace.SCUTTERFISH, 10, 4, 4));
        this.waterCreatures.add(new SpawnListEntry(MobEntitiesSpace.SIFTER_EEL, 10, 1, 3));
        this.monsters.add(new SpawnListEntry(MobEntitiesSpace.CREEPER_FLESH, 10, 4, 4));

        BiomeDecoratorCelestial decorator = new BiomeDecoratorCelestial(Blocks.STONE);
        decorator.waterPlantsPerChunk = 32;
        decorator.generateLakes = false;
        decorator.genPlants = new WorldGenWaterPlant(decorator.seaLevel);
        decorator.genCoral = new WorldGenWaterCoral(decorator.seaLevel);
        this.theBiomeDecorator = decorator;

        this.topBlock = TerrainBlocksSpace.LAYTHE_SILT.get();
        this.fillerBlock = TerrainBlocksSpace.LAYTHE_SILT.get();
    }

    @Override
    public void genTerrainBlocks(
            LegacyWorld world, Random rand, BlockState[] blocks, int x, int z, double noise) {
        BlockState block = this.topBlock.defaultBlockState();
        BlockState block1 = this.fillerBlock.defaultBlockState();
        int k = -1;
        int l = (int) (noise / 3.0D + 3.0D + rand.nextDouble() * 0.25D);
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
                    if (block2.is(Blocks.STONE)) {
                        if (k == -1) {
                            if (l <= 0) {
                                block = null;
                                block1 = Blocks.STONE.defaultBlockState();
                            } else if (l1 >= 59 && l1 <= 64) {
                                block = this.topBlock.defaultBlockState();
                                block1 = this.fillerBlock.defaultBlockState();
                            }

                            if (l1 < 63 && (block == null || block.isAir())) {
                                if (this.getFloatTemperature(x, l1, z) < 0.15F) {
                                    block = Blocks.ICE.defaultBlockState();
                                } else {
                                    block = Blocks.WATER.defaultBlockState();
                                }
                            }

                            k = l;

                            if (l1 >= 62) {
                                blocks[i2] = block;
                            } else if (l1 < 56 - l) {
                                block = null;
                                block1 = Blocks.STONE.defaultBlockState();
                                blocks[i2] =
                                        TerrainBlocksSpace.LAYTHE_SILT.get().defaultBlockState();
                            } else {
                                blocks[i2] = block1;
                            }
                        } else if (k > 0) {
                            --k;
                            blocks[i2] = block1;

                            if (k == 0 && block1.is(Blocks.SAND)) {
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
