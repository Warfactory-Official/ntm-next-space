// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-FileCopyrightText: 2025 George Paton
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.dim.ike;

import com.hbmspace.blocks.ore.CelestialOre;
import com.hbmspace.dim.SolarSystem.Body;
import com.hbmspace.dim.WorldProviderCelestial;
import com.hbmspace.dim.legacy.LegacyDecorWorld;
import com.hbmspace.dim.legacy.LegacyWorldGenerator;
import com.hbmspace.dim.legacy.feature.DungeonToolbox;
import java.util.Random;
import net.minecraft.world.level.block.Block;

import static com.hbmspace.data.SpaceData.*;

public class WorldGeneratorIke implements LegacyWorldGenerator {

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
                ASBESTOS_SPAWN.get(),
                8,
                3,
                22,
                CelestialOre.ASBESTOS.state(Body.IKE),
                stone);
        DungeonToolbox.generateOre(
                world,
                rand,
                i,
                j,
                COPPER_SPAWN.get(),
                9,
                4,
                27,
                CelestialOre.COPPER.state(Body.IKE),
                stone);
        DungeonToolbox.generateOre(
                world, rand, i, j, IRON_SPAWN, 8, 1, 33, CelestialOre.IRON.state(Body.IKE), stone);
        DungeonToolbox.generateOre(
                world,
                rand,
                i,
                j,
                LITHIUM_SPAWN.get(),
                6,
                4,
                8,
                CelestialOre.LITHIUM.state(Body.IKE),
                stone);
        DungeonToolbox.generateOre(
                world, rand, i, j, 2, 4, 15, 40, CelestialOre.COLTAN.state(Body.IKE), stone);

        DungeonToolbox.generateOre(
                world,
                rand,
                i,
                j,
                MINERAL_SPAWN.get(),
                10,
                12,
                32,
                CelestialOre.MINERAL.state(Body.IKE),
                stone);
    }
}
