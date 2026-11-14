/*
 * SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
 * SPDX-FileCopyrightText: 2024-2025 George Paton
 * SPDX-License-Identifier: LGPL-3.0-only
 *
 * Copyright (C) 2026 movblock. All rights reserved.
 * Copyright (C) 2024-2025 George Paton. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License version 3 only,
 * as published by the Free Software Foundation.
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this code. If not, see <https://www.gnu.org/licenses/>.
 *
 * Under GPLv3 §7(c) and §7(e), these additional terms apply to LGPL-covered
 * material authored by movblock:
 *
 * - the license grants no rights to the name "NTM NEXT: Space";
 * - a modified version must not be named NTM NEXT: Space or presented as
 *   the official release, and must be marked as different from it.
 *
 * Accurately identifying a work as derived from NTM NEXT: Space does not
 * violate these additional terms, provided it is clearly distinguished from
 * the official release and does not imply endorsement. Ports are encouraged
 * to acknowledge NTM NEXT: Space as their upstream project and provide a
 * link to it.
 */

package com.hbmspace.blocks.machine;

import com.hbm.blocks.TickingEntityBlock;
import com.hbm.capability.CapabilityBlock;
import com.hbm.capability.MachineCaps;
import com.hbm.interfaces.LookOverlay;
import com.hbm.interfaces.LookOverlayProvider;
import com.hbm.inventory.fluid.NTMFluidProperties;
import com.hbm.util.Blink;
import com.hbmspace.blockentity.machine.AirPumpBlockEntity;
import com.hbmspace.blocks.AtmosphereBlockEntitiesSpace;
import com.hbmspace.dim.trait.CBT_Atmosphere;
import com.hbmspace.handler.atmosphere.AtmosphereText;
import com.hbmspace.handler.atmosphere.SealableBlock;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jspecify.annotations.Nullable;

public class AirPumpBlock extends Block
        implements TickingEntityBlock, LookOverlayProvider, CapabilityBlock, SealableBlock {

    public AirPumpBlock(Properties props) {
        super(props);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new AirPumpBlockEntity(pos, state);
    }

    @Override
    protected InteractionResult useItemOn(
            ItemStack stack,
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            InteractionHand hand,
            BlockHitResult hit) {
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof AirPumpBlockEntity pump) {
            AtmosphereText.retypeGaseous(stack, player, pump.tank, pump);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    protected InteractionResult useWithoutItem(
            BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        return InteractionResult.SUCCESS;
    }

    @Override
    public boolean isSealed(Level level, BlockPos pos) {
        return false;
    }

    @Override
    public void buildLookOverlay(Level level, BlockPos pos, LookOverlay info) {
        if (!(level.getBlockEntity(pos) instanceof AirPumpBlockEntity pump)) return;

        info.title(getName());
        info.row(
                NTMFluidProperties.getDisplayName(pump.tank.getFluid())
                        .copy()
                        .append(": ")
                        .append(
                                Component.translatable(
                                        "unit.millibuckets",
                                        pump.tank.getFill() + "/" + pump.tank.getMaxFill())));

        int blink = Blink.lit() ? 0xFF0000 : 0xFFFF00;
        if (pump.tank.getFill() <= 10) {
            info.row(
                    Component.literal("! ! ! ")
                            .append(Component.translatable("atmosphere.noTank"))
                            .append(" ! ! !"),
                    blink);
        } else if (!pump.hasSeal()) {
            info.row(
                    Component.literal("! ! ! ")
                            .append(Component.translatable("atmosphere.noSeal"))
                            .append(" ! ! !"),
                    blink);
        }

        info.row(Component.translatable("atmosphere.name").append(": "));

        boolean hasPressure = false;
        CBT_Atmosphere atmosphere = pump.currentAtmosphere;
        if (atmosphere != null) {
            for (CBT_Atmosphere.FluidEntry entry : atmosphere.fluids) {
                if (entry.pressure > 0.01) {
                    info.row(
                            Component.literal(" - ")
                                    .append(NTMFluidProperties.getDisplayName(entry.fluid))
                                    .append(" - ")
                                    .append(AtmosphereText.roundDecimal(entry.pressure, 3) + "atm")
                                    .withStyle(ChatFormatting.AQUA));
                    hasPressure = true;
                }
            }
        }

        if (!hasPressure)
            info.row(
                    Component.literal(" - ")
                            .append(Component.translatable("atmosphere.vacuum"))
                            .withStyle(ChatFormatting.AQUA));
        if (pump.isRecycling())
            info.row(
                    Component.translatable("atmosphere.recycling").withStyle(ChatFormatting.GREEN));
    }

    @Override
    public MachineCaps caps() {
        return MachineCaps.of(AtmosphereBlockEntitiesSpace.AIR_PUMP).fluidIn();
    }
}
