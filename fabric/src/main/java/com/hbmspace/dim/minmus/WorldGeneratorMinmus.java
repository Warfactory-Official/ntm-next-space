// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-FileCopyrightText: 2025 George Paton
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.dim.minmus;

import com.hbm.blocks.ModBlocks;
import com.hbmspace.blocks.ore.CelestialOre;
import com.hbmspace.dim.SolarSystem.Body;
import com.hbmspace.dim.WorldProviderCelestial;
import com.hbmspace.dim.legacy.LegacyDecorWorld;
import com.hbmspace.dim.legacy.LegacyWorldGenerator;
import com.hbmspace.dim.legacy.feature.DungeonToolbox;
import java.util.Random;
import net.minecraft.world.level.block.Block;

import static com.hbmspace.data.SpaceData.*;

public class WorldGeneratorMinmus implements LegacyWorldGenerator {

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
                NICKEL_SPAWN.get(),
                8,
                1,
                43,
                CelestialOre.NICKEL.state(Body.MINMUS),
                stone);
        DungeonToolbox.generateOre(
                world,
                rand,
                i,
                j,
                TITANIUM_SPAWN.get(),
                12,
                4,
                27,
                CelestialOre.TITANIUM.state(Body.MINMUS),
                stone);
        DungeonToolbox.generateOre(
                world,
                rand,
                i,
                j,
                1,
                16,
                6,
                40,
                ModBlocks.STONE_RESOURCE_MALACHITE.get().defaultBlockState(),
                stone);
        DungeonToolbox.generateOre(
                world,
                rand,
                i,
                j,
                COPPER_SPAWN.get() * 3,
                12,
                8,
                56,
                CelestialOre.COPPER.state(Body.MINMUS),
                stone);
    }
}
