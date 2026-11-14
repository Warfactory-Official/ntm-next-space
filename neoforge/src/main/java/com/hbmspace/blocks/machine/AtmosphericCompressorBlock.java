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

import com.hbm.blocks.machine.DeuteriumTowerBlock;
import com.hbm.capability.MachineCaps;
import com.hbm.interfaces.LookOverlay;
import com.hbm.interfaces.ToolInteractionHandler;
import com.hbm.interfaces.ToolType;
import com.hbm.inventory.fluid.NTMFluidProperties;
import com.hbm.util.NumberFormatter;
import com.hbmspace.blockentity.machine.AtmosphericCompressorBlockEntity;
import com.hbmspace.blocks.AtmosphereBlockEntitiesSpace;
import com.hbmspace.dim.CelestialBody;
import com.hbmspace.dim.trait.CBT_Atmosphere;
import com.hbmspace.handler.atmosphere.AtmosphereText;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class AtmosphericCompressorBlock extends DeuteriumTowerBlock
        implements ToolInteractionHandler {
    public AtmosphericCompressorBlock(Properties props) {
        super(props);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new AtmosphericCompressorBlockEntity(pos, state);
    }

    @Override
    public boolean onToolUse(
            Level level,
            Player player,
            BlockPos pos,
            @Nullable Direction side,
            Vec3 hit,
            ToolType tool) {
        if (tool != ToolType.SCREWDRIVER) return false;
        if (level.isClientSide()) return true;
        if (!(level.getBlockEntity(pos) instanceof AtmosphericCompressorBlockEntity compressor))
            return false;

        compressor.cycleGas();
        return true;
    }

    @Override
    protected InteractionResult useItemOnAtCore(
            ItemStack stack,
            BlockState coreState,
            Level level,
            BlockPos core,
            Player player,
            InteractionHand hand,
            BlockHitResult hit) {
        Fluid type = AtmosphereText.identified(stack);
        if (player.isShiftKeyDown() || type == null) return InteractionResult.TRY_WITH_EMPTY_HAND;
        if (level.isClientSide()) return InteractionResult.SUCCESS;

        if (level.getBlockEntity(core) instanceof AtmosphericCompressorBlockEntity compressor
                && compressor.switchGas(type)) {
            AtmosphereText.changedType(player, type);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void buildLookOverlay(Level level, BlockPos pos, LookOverlay info) {
        if (!(level.getBlockEntity(pos) instanceof AtmosphericCompressorBlockEntity compressor))
            return;

        info.title(getName());
        if (!CelestialBody.hasTrait(level, CBT_Atmosphere.class)) {
            info.row(
                    Component.empty()
                            .append(
                                    Component.translatable("overlay.hbmspace.error")
                                            .withStyle(ChatFormatting.RED))
                            .append(Component.translatable("overlay.hbmspace.vacuum_collect")));
            return;
        }
        info.row(
                Component.translatable(
                                "overlay.hbm.power",
                                NumberFormatter.formatCompact(compressor.power))
                        .withStyle(
                                compressor.power < AtmosphericCompressorBlockEntity.MAX_POWER / 20
                                        ? ChatFormatting.RED
                                        : ChatFormatting.GREEN));
        info.row(
                LookOverlay.outflow(
                        Component.translatable(
                                "overlay.hbm.tank",
                                NTMFluidProperties.clientName(compressor.tank.getFluid()),
                                compressor.tank.getFill(),
                                compressor.tank.getMaxFill())));
    }

    @Override
    public MachineCaps caps() {
        return MachineCaps.of(AtmosphereBlockEntitiesSpace.ATMOSPHERIC_COMPRESSOR)
                .powerIn()
                .fluidOut()
                .fe();
    }
}
