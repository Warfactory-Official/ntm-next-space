// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.blocks;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;

public class BlockCoral extends BlockWaterPlant {

    public static final MapCodec<BlockCoral> CODEC = simpleCodec(BlockCoral::new);

    private static final Direction[] SEARCH = {
        Direction.NORTH,
        Direction.SOUTH,
        Direction.WEST,
        Direction.EAST,
        Direction.UP,
        Direction.DOWN
    };

    public BlockCoral(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BlockCoral> codec() {
        return CODEC;
    }

    public static Direction attachment(BlockGetter level, BlockPos pos) {
        for (Direction dir : SEARCH) {
            if (level.getBlockState(pos.relative(dir)).isSolid()) return dir;
        }
        return Direction.DOWN;
    }
}
