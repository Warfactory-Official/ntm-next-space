// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-FileCopyrightText: 2025 George Paton
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.dim.moon;

import com.hbmspace.blocks.ore.CelestialOre;
import com.hbmspace.dim.SolarSystem.Body;
import com.hbmspace.dim.WorldProviderCelestial;
import com.hbmspace.dim.legacy.LegacyDecorWorld;
import com.hbmspace.dim.legacy.LegacyWorldGenerator;
import com.hbmspace.dim.legacy.feature.DungeonToolbox;
import java.util.Random;
import net.minecraft.world.level.block.Block;

import static com.hbmspace.data.SpaceData.*;

public class WorldGeneratorMoon implements LegacyWorldGenerator {

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
                LITHIUM_SPAWN.get(),
                6,
                4,
                8,
                CelestialOre.LITHIUM.state(Body.MUN),
                stone);
        DungeonToolbox.generateOre(
                world,
                rand,
                i,
                j,
                ALUMINIUM_SPAWN.get(),
                6,
                5,
                40,
                CelestialOre.ALUMINIUM.state(Body.MUN),
                stone);
        DungeonToolbox.generateOre(
                world,
                rand,
                i,
                j,
                FLUORITE_SPAWN.get(),
                4,
                5,
                45,
                CelestialOre.FLUORITE.state(Body.MUN),
                stone);
        DungeonToolbox.generateOre(
                world, rand, i, j, 10, 13, 5, 64, CelestialOre.QUARTZ.state(Body.MUN), stone);

        DungeonToolbox.generateOre(
                world, rand, i, j, 1, 12, 8, 32, CelestialOre.SHALE.state(Body.MUN), stone);
    }
}
