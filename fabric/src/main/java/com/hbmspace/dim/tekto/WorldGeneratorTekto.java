// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-FileCopyrightText: 2025 George Paton
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.dim.tekto;

import com.hbm.lib.Library;
import com.hbmspace.blocks.FloraBlocksSpace;
import com.hbmspace.blocks.TerrainBlocksSpace;
import com.hbmspace.blocks.ore.CelestialOre;
import com.hbmspace.dim.BiomeGenBaseCelestial;
import com.hbmspace.dim.SolarSystem.Body;
import com.hbmspace.dim.legacy.LegacyDecorWorld;
import com.hbmspace.dim.legacy.LegacyWorldGenerator;
import com.hbmspace.dim.legacy.feature.WorldGenMinable;
import com.hbmspace.dim.tekto.biome.BiomeGenBaseTekto;
import java.util.Random;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import static com.hbmspace.data.SpaceData.COBALT_SPAWN;

public class WorldGeneratorTekto implements LegacyWorldGenerator {

    @Override
    public void generate(Random rand, int chunkX, int chunkZ, LegacyDecorWorld world) {
        int i = chunkX * 16;
        int j = chunkZ * 16;
        Block basalt = BuiltInRegistries.BLOCK.getOptional(Library.id("basalt")).orElseThrow();

        WorldGenMinable cobalt =
                new WorldGenMinable(CelestialOre.COBALT.state(Body.TEKTO), 6, basalt);
        int cobaltVeins = COBALT_SPAWN.get() * 2;
        for (int vein = 0; vein < cobaltVeins; vein++) {
            int x = i + rand.nextInt(16);
            int y = 4 + rand.nextInt(8);
            int z = j + rand.nextInt(16);
            cobalt.generate(world, rand, x, y, z);
        }

        BiomeGenBaseCelestial biome = world.getBiomeGenForCoords(i + 16, j + 16);

        Block vent = TerrainBlocksSpace.GEYSIR_CHLORIC.get();
        BlockState ventState = vent.defaultBlockState();
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                for (int y = 32; y < 128; y++) {
                    int ox = i + x + 8;
                    int oz = j + z + 8;
                    if (world.is(ox, y, oz, vent))
                        world.setBlock(ox, y, oz, ventState, Block.UPDATE_ALL);
                }
            }
        }

        if (biome == BiomeGenBaseTekto.polyvinylPlains) {
            for (int o = 0; o < 2; o++) {
                if (rand.nextInt(10) == 0) {
                    int x = i + rand.nextInt(16) + 8;
                    int z = j + rand.nextInt(16) + 8;
                    int y = world.getHeightValue(x, z);

                    new TTree(
                                    4,
                                    2,
                                    10,
                                    2,
                                    4,
                                    false,
                                    FloraBlocksSpace.PVC_LOG,
                                    FloraBlocksSpace.RUBBER_LEAVES)
                            .generate(world.level(), rand, x, y, z);
                }

                if (rand.nextInt(8) == 0) {
                    int x = i + rand.nextInt(16) + 8;
                    int z = j + rand.nextInt(16) + 8;
                    int y = world.getHeightValue(x, z);

                    new TTree(
                                    2,
                                    4,
                                    5,
                                    3,
                                    2,
                                    false,
                                    FloraBlocksSpace.VINYL_LOG,
                                    FloraBlocksSpace.PET_LEAVES)
                            .generate(world.level(), rand, x, y, z);
                }
            }
        }

        if (biome == BiomeGenBaseTekto.halogenHills) {
            if (rand.nextInt(12) == 0) {
                for (int o = 0; o < 4; o++) {
                    int x = i + rand.nextInt(16) + 8;
                    int z = j + rand.nextInt(16) + 8;
                    int y = world.getHeightValue(x, z);

                    new TTree(
                                    3,
                                    2,
                                    14,
                                    3,
                                    3,
                                    false,
                                    FloraBlocksSpace.PVC_LOG,
                                    FloraBlocksSpace.RUBBER_LEAVES)
                            .generate(world.level(), rand, x, y, z);
                }
            }
        }

        if (biome == BiomeGenBaseTekto.forest) {
            for (int o = 0; o < 8; o++) {
                int x = i + rand.nextInt(16) + 8;
                int z = j + rand.nextInt(16) + 8;
                int y = world.getHeightValue(x, z);

                if (rand.nextInt(2) == 0) {
                    new TTree(
                                    3,
                                    2,
                                    20,
                                    3,
                                    5,
                                    true,
                                    FloraBlocksSpace.PVC_LOG,
                                    FloraBlocksSpace.RUBBER_LEAVES)
                            .generate(world.level(), rand, x, y, z);
                } else {
                    new TTree(
                                    3,
                                    1,
                                    1,
                                    3,
                                    5,
                                    false,
                                    FloraBlocksSpace.PVC_LOG,
                                    FloraBlocksSpace.RUBBER_LEAVES)
                            .generate(world.level(), rand, x, y, z);
                }
            }
        }
    }
}
