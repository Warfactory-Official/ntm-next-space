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

package com.hbmspace.blocks.machine;

import com.hbm.blocks.CapabilityDependentShape;
import com.hbm.blocks.TickingEntityBlock;
import com.hbm.blocks.multiblock.MultiblockCoreBlock;
import com.hbm.capability.CapabilityBlock;
import com.hbm.capability.MachineCaps;
import com.hbm.uninos.graph.EndpointRegistry;
import com.hbm.uninos.graph.PhysicalGraph;
import com.hbmspace.blockentity.machine.SolarPanelBlockEntity;
import com.hbmspace.blocks.StationBlockEntitiesSpace;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.redstone.Orientation;
import org.jspecify.annotations.Nullable;

public class SolarBlock extends MultiblockCoreBlock
        implements TickingEntityBlock, CapabilityBlock, CapabilityDependentShape.Endpoint {
    private static final BooleanProperty UP = BooleanProperty.create("up");
    private static final BooleanProperty DOWN = BooleanProperty.create("down");
    private static final BooleanProperty LEFT = BooleanProperty.create("left");
    private static final BooleanProperty RIGHT = BooleanProperty.create("right");

    public SolarBlock(Properties props) {
        super(props);
        registerDefaultState(
                defaultBlockState()
                        .setValue(UP, false)
                        .setValue(DOWN, false)
                        .setValue(LEFT, false)
                        .setValue(RIGHT, false));
    }

    private static @Nullable BooleanProperty arm(BlockState state, Direction dir) {
        return switch (dir) {
            case UP -> UP;
            case DOWN -> DOWN;
            default -> {
                Direction facing = coreFacing(state);
                yield dir == facing.getClockWise()
                        ? RIGHT
                        : dir == facing.getCounterClockWise() ? LEFT : null;
            }
        };
    }

    public static int arms(BlockState state) {
        int mask = 0;
        for (Direction dir : Direction.values()) {
            var property = arm(state, dir);
            if (property != null && state.getValue(property)) mask |= 1 << dir.ordinal();
        }
        return mask;
    }

    @Override
    public BlockState withPowerArm(BlockState state, Direction side, boolean joins) {
        var property = arm(state, side);
        return property == null ? state : state.setValue(property, joins);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(UP, DOWN, LEFT, RIGHT);
    }

    @Override
    protected int getLightDampening(BlockState state) {
        return 0;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new SolarPanelBlockEntity(pos, state);
    }

    @Override
    public MachineCaps caps() {
        return MachineCaps.of(StationBlockEntitiesSpace.SOLAR_PANEL).powerOut().fe();
    }

    @Override
    public void setPlacedBy(
            Level level,
            BlockPos pos,
            BlockState state,
            @Nullable LivingEntity placer,
            ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (!(level instanceof ServerLevel server)) return;
        FoldedOwner owner = ownerOf(level, pos);
        if (owner != null)
            PhysicalGraph.redraw(server.getBlockState(owner.pos()), server, owner.pos());
    }

    @Override
    public void reindexLoadedCells(ServerLevel level, BlockPos core) {
        super.reindexLoadedCells(level, core);
        BlockEntity be = level.getBlockEntity(core);
        assert be instanceof SolarPanelBlockEntity;
        EndpointRegistry.declareSelfIfEndpoint(level, be);
    }

    @Override
    protected BlockState updateShape(
            BlockState state,
            LevelReader level,
            ScheduledTickAccess ticks,
            BlockPos pos,
            Direction dir,
            BlockPos neighbourPos,
            BlockState neighbourState,
            RandomSource random) {
        BlockState updated =
                super.updateShape(
                        state, level, ticks, pos, dir, neighbourPos, neighbourState, random);
        var property = arm(updated, dir);
        if (property == null) return updated;
        boolean joins = PhysicalGraph.powerContact(level, neighbourPos, dir.getOpposite());
        if (level.isClientSide())
            return PhysicalGraph.predictArm(updated, property, neighbourState, joins);
        return updated.setValue(property, joins);
    }

    @Override
    protected void neighborChanged(
            BlockState state,
            Level level,
            BlockPos pos,
            Block block,
            @Nullable Orientation orientation,
            boolean movedByPiston) {
        super.neighborChanged(state, level, pos, block, orientation, movedByPiston);
        if (level instanceof ServerLevel server) {
            PhysicalGraph.redraw(state, server, pos);
        }
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        if (mirror == Mirror.NONE) return state;
        return super.mirror(state, mirror)
                .setValue(LEFT, state.getValue(RIGHT))
                .setValue(RIGHT, state.getValue(LEFT));
    }
}
