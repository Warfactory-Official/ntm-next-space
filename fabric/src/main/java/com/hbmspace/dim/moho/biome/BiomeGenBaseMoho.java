// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-FileCopyrightText: 2024-2025 George Paton
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.dim.moho.biome;

import com.hbm.lib.Library;
import com.hbmspace.HbmSpace;
import com.hbmspace.blocks.ModBlocksSpace;
import com.hbmspace.dim.BiomeDecoratorCelestial;
import com.hbmspace.dim.BiomeGenBaseCelestial;
import com.hbmspace.dim.legacy.LegacyWorld;
import java.util.Random;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public abstract class BiomeGenBaseMoho extends BiomeGenBaseCelestial {

    public static final BiomeGenBaseCelestial mohoCrag = new BiomeGenMohoCrag();
    public static final BiomeGenBaseCelestial mohoBasalt = new BiomeGenMohoBasalt();
    public static final BiomeGenBaseCelestial mohoLavaSea = new BiomeGenMohoLavaSea();
    public static final BiomeGenBaseCelestial mohoPlateau = new BiomeGenMohoPlateau();

    protected final Block basalt =
            BuiltInRegistries.BLOCK.getOptional(Library.id("basalt")).orElseThrow();

    protected BiomeGenBaseMoho(String name, int id) {
        super(Identifier.fromNamespaceAndPath(HbmSpace.MOD_ID, name), id);
        this.setDisableRain();

        BiomeDecoratorCelestial decorator =
                new BiomeDecoratorCelestial(ModBlocksSpace.MOHO_STONE.get());
        decorator.lavaCount = 50;
        this.theBiomeDecorator = decorator;

        this.setTemperatureRainfall(1.0F, 0.0F);
        this.topBlock = ModBlocksSpace.MOHO_REGOLITH.get();
        this.fillerBlock = ModBlocksSpace.MOHO_REGOLITH.get();
    }

    @Override
    public void genTerrainBlocks(
            LegacyWorld world, Random rand, BlockState[] blocks, int x, int z, double noise) {
        Block stone = ModBlocksSpace.MOHO_STONE.get();
        BlockState block = this.topBlock.defaultBlockState();
        BlockState block1 = this.fillerBlock.defaultBlockState();
        int k = -1;
        int l = (int) (noise / 8.0D + 8.0D + rand.nextDouble() * 0.50D);
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
                    if (block2.getBlock() == stone) {
                        if (k == -1) {
                            if (l <= 0) {
                                block = null;
                                block1 = stone.defaultBlockState();
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
                            } else if (by < 56 - l) {
                                block = null;
                                block1 = stone.defaultBlockState();
                                blocks[i] = basalt.defaultBlockState();
                            } else {
                                blocks[i] = block1;
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
}
