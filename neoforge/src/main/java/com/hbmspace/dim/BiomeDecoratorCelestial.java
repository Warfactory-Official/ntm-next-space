// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.dim;

import com.hbmspace.dim.legacy.LegacyDecorWorld;
import com.hbmspace.dim.legacy.feature.LegacyFeature;
import com.hbmspace.dim.legacy.feature.WorldGenLakes;
import com.hbmspace.dim.legacy.feature.WorldGenMinable;
import java.util.Random;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

public class BiomeDecoratorCelestial {

    public int lavaCount = 20;
    public boolean generateLakes = true;

    public int waterPlantsPerChunk = 0;
    public int rubberPlantsPerChunk = 0;
    public int coralPerChunk = 0;

    public int seaLevel = 63;
    public @Nullable LegacyFeature genPlants;
    public @Nullable LegacyFeature genRPlants;
    public @Nullable LegacyFeature genCoral;

    public int lakeChancePerChunk = 0;
    public BlockState lakeBlock = Blocks.WATER.defaultBlockState();

    private final Block stoneBlock;

    public BiomeDecoratorCelestial(Block stoneBlock) {
        this.stoneBlock = stoneBlock;
    }

    public void decorate(
            LegacyDecorWorld world, Random rand, int chunkX, int chunkZ, boolean hasLife) {
        generateOres(world, rand, chunkX, chunkZ);

        if (this.generateLakes) {
            WorldGenLiquidsCelestial spring =
                    new WorldGenLiquidsCelestial(Blocks.LAVA.defaultBlockState(), stoneBlock);
            for (int i = 0; i < lavaCount; ++i) {
                int x = chunkX + rand.nextInt(16) + 8;
                int y = rand.nextInt(rand.nextInt(rand.nextInt(240) + 8) + 8);
                int z = chunkZ + rand.nextInt(16) + 8;
                spring.generate(world, rand, x, y, z);
            }
        }

        if (this.lakeChancePerChunk > 0) {
            if (rand.nextInt(lakeChancePerChunk) == 0) {
                int x = chunkX + rand.nextInt(16) + 8;
                int y = rand.nextInt(256);
                int z = chunkZ + rand.nextInt(16) + 8;
                new WorldGenLakes(lakeBlock).generate(world, rand, x, y, z);
            }
        }

        if (hasLife && this.waterPlantsPerChunk > 0) {
            for (int i = 0; i < waterPlantsPerChunk; ++i) {
                int x = chunkX + rand.nextInt(16) + 8;
                int z = chunkZ + rand.nextInt(16) + 8;
                int y = rand.nextInt(seaLevel);
                genPlants.generate(world, rand, x, y, z);
            }
        }
        if (this.rubberPlantsPerChunk > 0) {
            for (int i = 0; i < rubberPlantsPerChunk; ++i) {
                int x = chunkX + rand.nextInt(16) + 8;
                int z = chunkZ + rand.nextInt(16) + 8;
                int y = rand.nextInt(80);
                genRPlants.generate(world, rand, x, y, z);
            }
        }
        if (hasLife && this.coralPerChunk > 0) {
            for (int i = 0; i < coralPerChunk; ++i) {
                int x = chunkX + rand.nextInt(16) + 8;
                int z = chunkZ + rand.nextInt(16) + 8;
                int y = rand.nextInt(seaLevel);
                genCoral.generate(world, rand, x, y, z);
            }
        }
    }

    private static void generateOres(LegacyDecorWorld world, Random rand, int chunkX, int chunkZ) {
        standardOre(
                world,
                rand,
                chunkX,
                chunkZ,
                20,
                new WorldGenMinable(Blocks.DIRT.defaultBlockState(), 32, Blocks.STONE),
                0,
                256);
        standardOre(
                world,
                rand,
                chunkX,
                chunkZ,
                10,
                new WorldGenMinable(Blocks.GRAVEL.defaultBlockState(), 32, Blocks.STONE),
                0,
                256);
        standardOre(
                world,
                rand,
                chunkX,
                chunkZ,
                20,
                new WorldGenMinable(Blocks.IRON_ORE.defaultBlockState(), 8, Blocks.STONE),
                0,
                64);
        standardOre(
                world,
                rand,
                chunkX,
                chunkZ,
                2,
                new WorldGenMinable(Blocks.GOLD_ORE.defaultBlockState(), 8, Blocks.STONE),
                0,
                32);
        standardOre(
                world,
                rand,
                chunkX,
                chunkZ,
                8,
                new WorldGenMinable(Blocks.REDSTONE_ORE.defaultBlockState(), 7, Blocks.STONE),
                0,
                16);
        standardOre(
                world,
                rand,
                chunkX,
                chunkZ,
                1,
                new WorldGenMinable(Blocks.DIAMOND_ORE.defaultBlockState(), 7, Blocks.STONE),
                0,
                16);

        WorldGenMinable lapis =
                new WorldGenMinable(Blocks.LAPIS_ORE.defaultBlockState(), 6, Blocks.STONE);
        int x = chunkX + rand.nextInt(16);
        int y = rand.nextInt(16) + rand.nextInt(16);
        int z = chunkZ + rand.nextInt(16);
        lapis.generate(world, rand, x, y, z);
    }

    private static void standardOre(
            LegacyDecorWorld world,
            Random rand,
            int chunkX,
            int chunkZ,
            int count,
            WorldGenMinable gen,
            int minY,
            int maxY) {
        for (int l = 0; l < count; ++l) {
            int x = chunkX + rand.nextInt(16);
            int y = rand.nextInt(maxY - minY) + minY;
            int z = chunkZ + rand.nextInt(16);
            gen.generate(world, rand, x, y, z);
        }
    }
}
