// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.dim.laythe;

import com.hbmspace.blocks.ore.CelestialOre;
import com.hbmspace.dim.SolarSystem.Body;
import com.hbmspace.dim.legacy.LegacyDecorWorld;
import com.hbmspace.dim.legacy.LegacyWorldGenerator;
import com.hbmspace.dim.legacy.feature.DungeonToolbox;
import java.util.Random;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import static com.hbmspace.data.SpaceData.ASBESTOS_SPAWN;
import static com.hbmspace.data.SpaceData.BERYLLIUM_SPAWN;
import static com.hbmspace.data.SpaceData.RARE_SPAWN;

public class WorldGeneratorLaythe implements LegacyWorldGenerator {

    @Override
    public void generate(Random rand, int chunkX, int chunkZ, LegacyDecorWorld world) {
        int i = chunkX * 16;
        int j = chunkZ * 16;
        Block stone = Blocks.STONE;

        DungeonToolbox.generateOre(
                world,
                rand,
                i,
                j,
                ASBESTOS_SPAWN.get(),
                4,
                16,
                16,
                CelestialOre.ASBESTOS.state(Body.LAYTHE),
                stone);
        DungeonToolbox.generateOre(
                world,
                rand,
                i,
                j,
                BERYLLIUM_SPAWN.get(),
                4,
                5,
                30,
                CelestialOre.BERYLLIUM.state(Body.LAYTHE),
                stone);
        DungeonToolbox.generateOre(
                world,
                rand,
                i,
                j,
                RARE_SPAWN.get(),
                5,
                5,
                20,
                CelestialOre.RARE.state(Body.LAYTHE),
                stone);
    }
}
