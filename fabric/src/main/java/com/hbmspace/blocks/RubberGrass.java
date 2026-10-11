// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.blocks;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class RubberGrass extends Block {

    public static final MapCodec<RubberGrass> CODEC = simpleCodec(RubberGrass::new);

    public RubberGrass(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends RubberGrass> codec() {
        return CODEC;
    }

    @Override
    protected BlockState updateShape(
            BlockState state,
            LevelReader level,
            ScheduledTickAccess ticks,
            BlockPos pos,
            Direction directionToNeighbour,
            BlockPos neighbourPos,
            BlockState neighbourState,
            RandomSource random) {
        if (directionToNeighbour == Direction.UP
                && (neighbourState.liquid()
                        || neighbourState.isRedstoneConductor(level, neighbourPos))) {
            return TerrainBlocksSpace.RUBBER_SILT.get().defaultBlockState();
        }
        return super.updateShape(
                state,
                level,
                ticks,
                pos,
                directionToNeighbour,
                neighbourPos,
                neighbourState,
                random);
    }
}
