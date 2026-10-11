// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-FileCopyrightText: 2024 George Paton
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.dim.ike;

import com.hbmspace.HbmSpace;
import com.hbmspace.blocks.ModBlocksSpace;
import com.hbmspace.blocks.TerrainBlocksSpace;
import com.hbmspace.dim.BiomeDecoratorCelestial;
import com.hbmspace.dim.BiomeGenBaseCelestial;
import com.hbmspace.dim.legacy.LegacyWorld;
import java.util.Random;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class BiomeGenIke extends BiomeGenBaseCelestial {

    public static final BiomeGenIke biome = new BiomeGenIke();

    private BiomeGenIke() {
        super(Identifier.fromNamespaceAndPath(HbmSpace.MOD_ID, "ike"), 93);
        this.setBiomeName("Ike");

        BiomeDecoratorCelestial decorator =
                new BiomeDecoratorCelestial(ModBlocksSpace.IKE_STONE.get());
        decorator.lakeChancePerChunk = 8;
        decorator.lakeBlock = TerrainBlocksSpace.BROMINE_BLOCK.get().defaultBlockState();
        decorator.generateLakes = false;
        this.theBiomeDecorator = decorator;

        this.setHeight(new Height(0.325F, 0.05F));
        this.topBlock = TerrainBlocksSpace.IKE_REGOLITH.get();
        this.fillerBlock = TerrainBlocksSpace.IKE_REGOLITH.get();
    }

    @Override
    public void genTerrainBlocks(
            LegacyWorld world, Random rand, BlockState[] blocks, int x, int z, double noise) {
        Block stone = ModBlocksSpace.IKE_STONE.get();
        BlockState block = this.topBlock.defaultBlockState();
        BlockState block1 = this.fillerBlock.defaultBlockState();
        int k = -1;
        int l = (int) (noise / 8.0D + 8.0D + rand.nextDouble() * 0.50D);
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
                    if (block2.getBlock() == stone) {
                        if (k == -1) {
                            if (l <= 0) {
                                block = null;
                                block1 = stone.defaultBlockState();
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
                            } else if (l1 < 56 - l) {
                                block = null;
                                block1 = stone.defaultBlockState();
                                blocks[i2] = Blocks.GRAVEL.defaultBlockState();
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
