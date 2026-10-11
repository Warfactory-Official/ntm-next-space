// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-FileCopyrightText: 2024-2025 George Paton
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.dim.eve;

import com.hbmspace.blocks.TerrainBlocksSpace;
import com.hbmspace.blocks.ore.CelestialOre;
import com.hbmspace.dim.SolarSystem.Body;
import com.hbmspace.dim.eve.biome.BiomeGenBaseEve;
import com.hbmspace.dim.eve.genlayer.WorldGenEveSpike;
import com.hbmspace.dim.legacy.LegacyDecorWorld;
import com.hbmspace.dim.legacy.LegacyWorldGenerator;
import com.hbmspace.dim.legacy.feature.DungeonToolbox;
import java.util.Random;
import net.minecraft.world.level.block.Block;

public class WorldGeneratorEve implements LegacyWorldGenerator {

    @Override
    public void generate(Random rand, int chunkX, int chunkZ, LegacyDecorWorld world) {
        int i = chunkX * 16;
        int j = chunkZ * 16;
        Block stone = TerrainBlocksSpace.EVE_ROCK.get();

        DungeonToolbox.generateOre(
                world, rand, i, j, 12, 8, 1, 33, CelestialOre.NIOBIUM.state(Body.EVE), stone);
        DungeonToolbox.generateOre(
                world, rand, i, j, 8, 4, 5, 48, CelestialOre.IODINE.state(Body.EVE), stone);
        DungeonToolbox.generateOre(
                world, rand, i, j, 1, 4, 1, 16, CelestialOre.SCHRABIDIUM.state(Body.EVE), stone);

        int x = i + rand.nextInt(16) + 8;
        int z = j + rand.nextInt(16) + 8;
        int y = world.getHeightValue(x, z);

        if (world.getBiomeGenForCoords(x, z) == BiomeGenBaseEve.eveSeismicPlains) {
            new WorldGenEveSpike().generate(world, rand, x, y, z);
        }
    }
}
