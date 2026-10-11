// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-FileCopyrightText: 2020 HbmMods (The Bobcat)
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.dim.legacy.feature;

import com.hbmspace.dim.legacy.LegacyDecorWorld;
import java.util.Random;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public final class DungeonToolbox {

    private DungeonToolbox() {}

    public static void generateOre(
            LegacyDecorWorld world,
            Random rand,
            int chunkX,
            int chunkZ,
            int veinCount,
            int amount,
            int minHeight,
            int variance,
            BlockState ore,
            Block target) {
        WorldGenMinable vein = new WorldGenMinable(ore, amount, target);
        for (int i = 0; i < veinCount; i++) {
            int x = chunkX + rand.nextInt(16);
            int y = minHeight + (variance > 0 ? rand.nextInt(variance) : 0);
            int z = chunkZ + rand.nextInt(16);
            vein.generate(world, rand, x, y, z);
        }
    }
}
