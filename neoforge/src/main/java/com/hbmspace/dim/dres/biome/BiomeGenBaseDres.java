// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.dim.dres.biome;

import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.generic.BlockSellafieldSlaked;
import com.hbmspace.HbmSpace;
import com.hbmspace.blocks.ModBlocksSpace;
import com.hbmspace.dim.BiomeDecoratorCelestial;
import com.hbmspace.dim.BiomeGenBaseCelestial;
import com.hbmspace.dim.legacy.LegacyWorld;
import java.util.Random;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public abstract class BiomeGenBaseDres extends BiomeGenBaseCelestial {

    public static final BiomeGenBaseCelestial dresPlains = new BiomeGenDresPlains();
    public static final BiomeGenBaseCelestial dresCanyon = new BiomeGenDresCanyon();

    protected BiomeGenBaseDres(String name, int id) {
        super(Identifier.fromNamespaceAndPath(HbmSpace.MOD_ID, name), id);
        this.setDisableRain();
        this.setTemperatureRainfall(-1.0F, 0.0F);

        this.theBiomeDecorator = new BiomeDecoratorCelestial(ModBlocksSpace.DRES_ROCK.get());
        this.theBiomeDecorator.generateLakes = false;

        this.topBlock = ModBlocks.SELLAFIELD_SLAKED.get();
        this.fillerBlock = ModBlocks.SELLAFIELD_SLAKED.get();
    }

    protected void genDresTerrain(
            LegacyWorld world,
            Random rand,
            BlockState[] blocks,
            int x,
            int z,
            double noise,
            double depth,
            double jitter,
            int topFrom,
            int slakedBelow,
            int shadeA,
            int shadeB) {
        Block rock = ModBlocksSpace.DRES_ROCK.get();
        BlockState slaked = ModBlocks.SELLAFIELD_SLAKED.get().defaultBlockState();
        BlockState block = this.topBlock.defaultBlockState();
        BlockState block1 = this.fillerBlock.defaultBlockState();
        int k = -1;
        int l = (int) (noise / depth + depth + rand.nextDouble() * jitter);
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
                                block1 = slaked;
                            } else if (l1 >= 59 && l1 <= 64) {
                                block = this.topBlock.defaultBlockState();
                                block1 = this.fillerBlock.defaultBlockState();
                            }

                            if (l1 < 63 && (block == null || block.isAir())) {
                                block = this.topBlock.defaultBlockState();
                            }

                            k = l;

                            if (l1 >= topFrom) {
                                blocks[i2] = block;
                            } else if (l1 < slakedBelow) {
                                block = null;
                                block1 = slaked;
                                blocks[i2] =
                                        slaked.setValue(
                                                BlockSellafieldSlaked.SHADE,
                                                columnRandom(world, x, l1, z) > 0.4
                                                        ? shadeA
                                                        : shadeB);
                            } else {
                                blocks[i2] = block1;
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
