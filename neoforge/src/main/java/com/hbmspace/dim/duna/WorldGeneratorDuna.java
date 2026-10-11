// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-FileCopyrightText: 2024-2025 George Paton
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.dim.duna;

import com.hbm.blocks.ModBlocks;
import com.hbm.lib.Library;
import com.hbm.world.HbmFeatures;
import com.hbmspace.blocks.ore.CelestialOre;
import com.hbmspace.dim.SolarSystem.Body;
import com.hbmspace.dim.WorldProviderCelestial;
import com.hbmspace.dim.legacy.LegacyDecorWorld;
import com.hbmspace.dim.legacy.LegacyWorldGenerator;
import com.hbmspace.dim.legacy.feature.DungeonToolbox;
import java.util.Optional;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

import static com.hbmspace.data.SpaceData.*;

public class WorldGeneratorDuna implements LegacyWorldGenerator {

    @Override
    public void generate(Random rand, int chunkX, int chunkZ, LegacyDecorWorld world) {
        int i = chunkX * 16;
        int j = chunkZ * 16;
        Block stone = WorldProviderCelestial.of(world.level().getLevel()).getStone().getBlock();

        DungeonToolbox.generateOre(
                world,
                rand,
                i,
                j,
                IRON_SPAWN,
                8,
                32,
                64,
                CelestialOre.IRON.state(Body.DUNA),
                stone);
        DungeonToolbox.generateOre(
                world,
                rand,
                i,
                j,
                ZINC_SPAWN.get(),
                9,
                4,
                27,
                CelestialOre.ZINC.state(Body.DUNA),
                stone);

        Block basalt = ntm("basalt");
        DungeonToolbox.generateOre(
                world,
                rand,
                i,
                j,
                12,
                6,
                0,
                16,
                ntm("ore_basalt_sulfur").defaultBlockState(),
                basalt);
        DungeonToolbox.generateOre(
                world,
                rand,
                i,
                j,
                8,
                8,
                0,
                16,
                ntm("ore_basalt_fluorite").defaultBlockState(),
                basalt);
        DungeonToolbox.generateOre(
                world,
                rand,
                i,
                j,
                8,
                9,
                0,
                16,
                ModBlocks.ORE_BASALT_ASBESTOS.get().defaultBlockState(),
                basalt);
        DungeonToolbox.generateOre(
                world,
                rand,
                i,
                j,
                2,
                4,
                0,
                16,
                ModBlocks.ORE_BASALT_GEM.get().defaultBlockState(),
                basalt);
        DungeonToolbox.generateOre(
                world,
                rand,
                i,
                j,
                6,
                10,
                0,
                16,
                ntm("ore_basalt_molysite").defaultBlockState(),
                basalt);

        if (rand.nextInt(1234) == 0) {
            int x = i + rand.nextInt(16);
            int z = j + rand.nextInt(16);
            int y = world.getHeightValue(x, z);
            HbmFeatures.LANTERN_BEHEMOTH
                    .get()
                    .place(
                            new FeaturePlaceContext<>(
                                    Optional.empty(),
                                    world.level(),
                                    world.level().getLevel().getChunkSource().getGenerator(),
                                    RandomSource.create(rand.nextLong()),
                                    new BlockPos(x, y, z),
                                    NoneFeatureConfiguration.INSTANCE));
        }
    }

    private static Block ntm(String path) {
        return BuiltInRegistries.BLOCK.getOptional(Library.id(path)).orElseThrow();
    }
}
