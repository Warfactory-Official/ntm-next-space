// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.blocks.machine;

import com.hbm.blocks.multiblock.MultiblockHandlerXR;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;

public final class ChungusPlacementSpace {
    public static final int[] DIMENSIONS = {4, 0, 10, 3, 2, 2};
    public static final int[][] BOXES = {
        {3, 0, 0, 3, 2, 2}, {4, -4, 0, 3, 1, 1}, {3, 0, 6, -1, 1, 1}, {2, 0, 10, -7, 1, 1}
    };

    private ChungusPlacementSpace() {}

    public static boolean check(Level level, BlockPos placed, Direction facing, int offset) {
        BlockPos core = placed.relative(facing, offset);
        for (int[] box : BOXES) {
            if (!MultiblockHandlerXR.checkSpace(level, core, box, placed, facing)) return false;
        }
        return level.getBlockState(placed.relative(facing).above(2)).canBeReplaced();
    }
}
