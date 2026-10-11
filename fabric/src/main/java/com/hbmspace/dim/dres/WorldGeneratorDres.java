// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-FileCopyrightText: 2025 George Paton
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.dim.dres;

import com.hbmspace.blocks.ore.CelestialOre;
import com.hbmspace.dim.SolarSystem.Body;
import com.hbmspace.dim.WorldProviderCelestial;
import com.hbmspace.dim.legacy.LegacyDecorWorld;
import com.hbmspace.dim.legacy.LegacyWorldGenerator;
import com.hbmspace.dim.legacy.feature.DungeonToolbox;
import java.util.Random;
import net.minecraft.world.level.block.Block;

import static com.hbmspace.data.SpaceData.*;

public class WorldGeneratorDres implements LegacyWorldGenerator {

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
                COBALT_SPAWN.get(),
                4,
                3,
                22,
                CelestialOre.COBALT.state(Body.DRES),
                stone);
        DungeonToolbox.generateOre(
                world,
                rand,
                i,
                j,
                DRES_IRON_SPAWN.get(),
                9,
                4,
                27,
                CelestialOre.IRON.state(Body.DRES),
                stone);
        DungeonToolbox.generateOre(
                world, rand, i, j, 12, 8, 1, 33, CelestialOre.NIOBIUM.state(Body.DRES), stone);
        DungeonToolbox.generateOre(
                world,
                rand,
                i,
                j,
                COLTAN_RATE.get(),
                4,
                15,
                40,
                CelestialOre.COLTAN.state(Body.DRES),
                stone);
        DungeonToolbox.generateOre(
                world, rand, i, j, 1, 6, 4, 64, CelestialOre.LANTHANIUM.state(Body.DRES), stone);

        DungeonToolbox.generateOre(
                world, rand, i, j, 1, 12, 8, 32, CelestialOre.SHALE.state(Body.DRES), stone);
    }
}
