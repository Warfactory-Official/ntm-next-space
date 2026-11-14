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

import com.hbm.blocks.machine.IntakeBlock;
import com.hbm.capability.MachineCaps;
import com.hbm.interfaces.LookOverlay;
import com.hbm.inventory.fluid.NTMFluidProperties;
import com.hbm.util.NumberFormatter;
import com.hbmspace.blockentity.machine.AtmosphericEmitterBlockEntity;
import com.hbmspace.blocks.AtmosphereBlockEntitiesSpace;
import com.hbmspace.handler.atmosphere.AtmosphereText;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jspecify.annotations.Nullable;

public class AtmosphericEmitterBlock extends IntakeBlock {
    public AtmosphericEmitterBlock(Properties props) {
        super(props);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new AtmosphericEmitterBlockEntity(pos, state);
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
        if (player.isShiftKeyDown()) return InteractionResult.SUCCESS;
        if (AtmosphereText.identified(stack) == null) return InteractionResult.TRY_WITH_EMPTY_HAND;
        if (!level.isClientSide()
                && level.getBlockEntity(core) instanceof AtmosphericEmitterBlockEntity emitter) {
            AtmosphereText.retypeGaseous(stack, player, emitter.tank, emitter);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void buildLookOverlay(Level level, BlockPos pos, LookOverlay info) {
        if (!(level.getBlockEntity(pos) instanceof AtmosphericEmitterBlockEntity emitter)) return;

        info.title(getName());
        info.row(
                Component.translatable(
                                "overlay.hbm.power", NumberFormatter.formatCompact(emitter.power))
                        .withStyle(
                                emitter.isRunning() ? ChatFormatting.GREEN : ChatFormatting.RED));
        info.row(
                LookOverlay.inflow(
                        Component.translatable(
                                "overlay.hbm.tank",
                                NTMFluidProperties.clientName(emitter.tank.getFluid()),
                                emitter.tank.getFill(),
                                emitter.tank.getMaxFill())));
    }

    @Override
    public MachineCaps caps() {
        return MachineCaps.of(AtmosphereBlockEntitiesSpace.ATMOSPHERIC_EMITTER)
                .powerIn()
                .fluidIn()
                .fe();
    }
}
