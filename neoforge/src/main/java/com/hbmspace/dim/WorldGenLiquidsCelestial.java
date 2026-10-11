// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-FileCopyrightText: 2024 George Paton
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.dim;

import com.hbmspace.dim.legacy.LegacyDecorWorld;
import java.util.Random;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class WorldGenLiquidsCelestial {

    private final BlockState liquidBlock;
    private final Block targetBlock;

    public WorldGenLiquidsCelestial(BlockState liquidBlock, Block targetBlock) {
        this.liquidBlock = liquidBlock;
        this.targetBlock = targetBlock;
    }

    public boolean generate(LegacyDecorWorld world, Random rand, int x, int y, int z) {
        if (!world.is(x, y + 1, z, targetBlock)) {
            return false;
        } else if (!world.is(x, y - 1, z, targetBlock)) {
            return false;
        } else if (!world.isAirBlock(x, y, z) && !world.is(x, y, z, targetBlock)) {
            return false;
        } else {
            int l = 0;

            if (world.is(x - 1, y, z, targetBlock)) {
                ++l;
            }

            if (world.is(x + 1, y, z, targetBlock)) {
                ++l;
            }

            if (world.is(x, y, z - 1, targetBlock)) {
                ++l;
            }

            if (world.is(x, y, z + 1, targetBlock)) {
                ++l;
            }

            int i1 = 0;

            if (world.isAirBlock(x - 1, y, z)) {
                ++i1;
            }

            if (world.isAirBlock(x + 1, y, z)) {
                ++i1;
            }

            if (world.isAirBlock(x, y, z - 1)) {
                ++i1;
            }

            if (world.isAirBlock(x, y, z + 1)) {
                ++i1;
            }

            if (l == 3 && i1 == 1) {
                world.flowLiquid(x, y, z, liquidBlock);
            }

            return true;
        }
    }
}
