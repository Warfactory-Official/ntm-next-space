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
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlockContainer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import org.jspecify.annotations.Nullable;

public class KelpBlock extends Block implements LiquidBlockContainer {
    public static final MapCodec<KelpBlock> CODEC = simpleCodec(KelpBlock::new);

    public KelpBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends KelpBlock> codec() {
        return CODEC;
    }

    public static boolean isStem(BlockGetter level, BlockPos pos, BlockState state) {
        return level.getBlockState(pos.above()).is(state.getBlock());
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().canSurvive(context.getLevel(), context.getClickedPos())
                ? defaultBlockState()
                : null;
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockState below = level.getBlockState(pos.below());
        return below.is(this) || below.is(TerrainBlocksSpace.LAYTHE_SILT.get());
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
        if (!state.canSurvive(level, pos)) return Blocks.WATER.defaultBlockState();
        ticks.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
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

    @Override
    protected FluidState getFluidState(BlockState state) {
        return Fluids.WATER.getSource(false);
    }

    @Override
    public boolean canPlaceLiquid(
            @Nullable LivingEntity user,
            BlockGetter level,
            BlockPos pos,
            BlockState state,
            Fluid type) {
        return false;
    }

    @Override
    public boolean placeLiquid(
            LevelAccessor level, BlockPos pos, BlockState state, FluidState fluidState) {
        return false;
    }
}
