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
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

public class RubberFarmBlock extends Block {
    public static final MapCodec<RubberFarmBlock> CODEC = simpleCodec(RubberFarmBlock::new);
    public static final BooleanProperty MOIST = BooleanProperty.create("moist");
    private static final VoxelShape SHAPE = Block.column(16.0, 0.0, 15.0);

    public RubberFarmBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(MOIST, false));
    }

    @Override
    protected MapCodec<? extends RubberFarmBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(MOIST);
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
        if (directionToNeighbour == Direction.UP && !state.canSurvive(level, pos)) {
            ticks.scheduleTick(pos, this, 1);
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

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockState above = level.getBlockState(pos.above());
        return !above.isSolid() || above.is(BlockTags.MAINTAINS_FARMLAND);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return !defaultBlockState().canSurvive(context.getLevel(), context.getClickedPos())
                ? TerrainBlocksSpace.RUBBER_SILT.get().defaultBlockState()
                : super.getStateForPlacement(context);
    }

    @Override
    protected boolean useShapeForLightOcclusion(BlockState state) {
        return true;
    }

    @Override
    protected VoxelShape getShape(
            BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!state.canSurvive(level, pos)) toSilt(null, state, level, pos);
    }

    @Override
    protected boolean isRandomlyTicking(BlockState state) {
        return true;
    }

    @Override
    protected void randomTick(
            BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        boolean moist = isNearCcl(level, pos) || level.isRainingAt(pos.above());
        level.setBlock(pos, state.setValue(MOIST, moist), UPDATE_CLIENTS);
    }

    private static boolean isNearCcl(LevelReader level, BlockPos pos) {
        Block ccl = TerrainBlocksSpace.CCL_BLOCK.get();
        for (BlockPos near : BlockPos.betweenClosed(pos.offset(-4, 0, -4), pos.offset(4, 1, 4))) {
            if (level.getBlockState(near).is(ccl)) return true;
        }
        return false;
    }

    @Override
    public void fallOn(
            Level level, BlockState state, BlockPos pos, Entity entity, double fallDistance) {
        if (level instanceof ServerLevel serverLevel
                && level.getRandom().nextFloat() < fallDistance - 0.5
                && entity instanceof LivingEntity
                && (entity instanceof Player
                        || serverLevel.getGameRules().get(GameRules.MOB_GRIEFING))
                && entity.getBbWidth() * entity.getBbWidth() * entity.getBbHeight() > 0.512F) {
            toSilt(entity, state, level, pos);
        }
        super.fallOn(level, state, pos, entity, fallDistance);
    }

    private static void toSilt(
            @Nullable Entity sourceEntity, BlockState state, Level level, BlockPos pos) {
        BlockState silt =
                pushEntitiesUp(
                        state,
                        TerrainBlocksSpace.RUBBER_SILT.get().defaultBlockState(),
                        level,
                        pos);
        level.setBlockAndUpdate(pos, silt);
        level.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(sourceEntity, silt));
    }

    @Override
    protected boolean isPathfindable(BlockState state, PathComputationType type) {
        return false;
    }
}
