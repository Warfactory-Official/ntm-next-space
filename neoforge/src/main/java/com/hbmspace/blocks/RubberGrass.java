/*
 * SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright (C) 2026 movblock. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * You may not use this file under the Apache License except in
 * compliance with that License. You may obtain a copy at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Apache License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License for the specific language governing permissions
 * and limitations under the License.
 */

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
