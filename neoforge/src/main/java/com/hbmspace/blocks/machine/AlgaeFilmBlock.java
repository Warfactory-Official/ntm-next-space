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

import com.hbm.blocks.TickingEntityBlock;
import com.hbm.capability.CapabilityBlock;
import com.hbm.capability.MachineCaps;
import com.hbm.interfaces.LookOverlay;
import com.hbm.interfaces.LookOverlayProvider;
import com.hbm.inventory.fluid.NTMFluidProperties;
import com.hbm.inventory.fluid.tank.FluidTankNTM;
import com.hbm.util.Blink;
import com.hbmspace.blockentity.machine.AlgaeFilmBlockEntity;
import com.hbmspace.blocks.TerrainBlockEntitiesSpace;
import com.mojang.serialization.MapCodec;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import org.jspecify.annotations.Nullable;

public class AlgaeFilmBlock extends HorizontalDirectionalBlock
        implements TickingEntityBlock, LookOverlayProvider, CapabilityBlock {
    public static final MapCodec<AlgaeFilmBlock> CODEC = simpleCodec(AlgaeFilmBlock::new);

    public AlgaeFilmBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new AlgaeFilmBlockEntity(pos, state);
    }

    @Override
    public MachineCaps caps() {
        return MachineCaps.of(TerrainBlockEntitiesSpace.ALGAE_FILM)
                .fluidIn()
                .fluidOut()
                .fluidFaces(
                        AlgaeFilmBlockEntity.class,
                        (be, face) ->
                                face.side().getAxis()
                                        == be.getBlockState()
                                                .getValue(FACING)
                                                .getClockWise()
                                                .getAxis());
    }

    @Override
    public void buildLookOverlay(Level level, BlockPos pos, LookOverlay info) {
        if (!(level.getBlockEntity(pos) instanceof AlgaeFilmBlockEntity film)) return;

        info.title(getName());
        if (!film.canOperate) {
            info.row(
                    Component.literal("! ! ! ")
                            .append(Component.translatable("atmosphere.noGravity"))
                            .append(" ! ! !"),
                    Blink.lit() ? 0xFF0000 : 0xFFFF00);
        }
        line(info, Component.literal("-> ").withStyle(ChatFormatting.GREEN), film.tanks[0]);
        line(info, Component.literal("<- ").withStyle(ChatFormatting.RED), film.tanks[1]);
    }

    private static void line(LookOverlay info, Component prefix, FluidTankNTM tank) {
        info.row(
                prefix.copy()
                        .append(
                                NTMFluidProperties.getDisplayName(tank.getTankType())
                                        .copy()
                                        .withStyle(ChatFormatting.WHITE))
                        .append(Component.literal(": ").withStyle(ChatFormatting.WHITE))
                        .append(
                                Component.translatable(
                                                "unit.millibuckets",
                                                tank.getFill() + "/" + tank.getMaxFill())
                                        .withStyle(ChatFormatting.WHITE)));
    }
}
