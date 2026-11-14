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
import com.hbm.blocks.multiblock.MultiblockCoreBlock;
import com.hbm.capability.CapabilityBlock;
import com.hbm.capability.MachineCaps;
import com.hbm.interfaces.LookOverlay;
import com.hbm.interfaces.LookOverlayProvider;
import com.hbm.inventory.material.MaterialStack;
import com.hbm.inventory.material.MaterialTooltip;
import com.hbm.util.Blink;
import com.hbm.util.NumberFormatter;
import com.hbmspace.blockentity.machine.MagmaBlockEntity;
import com.hbmspace.blocks.IndustryBlockEntitiesSpace;
import com.hbmspace.dim.CelestialBody;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

public class MagmaBlock extends MultiblockCoreBlock
        implements TickingEntityBlock, LookOverlayProvider, CapabilityBlock {
    public MagmaBlock(Properties props) {
        super(props);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new MagmaBlockEntity(pos, state);
    }

    @Override
    public void buildLookOverlay(Level level, BlockPos pos, LookOverlay info) {
        if (!(level.getBlockEntity(pos) instanceof MagmaBlockEntity drill)) return;

        info.title(Component.translatable(getDescriptionId()));
        int blink = Blink.lit() ? 0xFF0000 : 0xFFFF00;
        if (!"moho".equals(CelestialBody.getBody(level).name)) {
            info.row(
                    Component.literal("! ! ! ")
                            .append(Component.translatable("desc.hbmspace.magma.notMoho"))
                            .append(" ! ! !"),
                    blink);
        } else if (!drill.validPosition) {
            info.row(
                    Component.literal("! ! ! ")
                            .append(Component.translatable("desc.hbmspace.magma.noLava"))
                            .append(" ! ! !"),
                    blink);
        } else {
            info.row(
                    Component.translatable(
                                    "overlay.hbm.power", NumberFormatter.formatCompact(drill.power))
                            .withStyle(
                                    drill.power < MagmaBlockEntity.CONSUMPTION
                                            ? ChatFormatting.RED
                                            : ChatFormatting.GREEN));
            for (MaterialStack stack : drill.liquids) {
                info.row(
                        stack.material
                                .displayName()
                                .copy()
                                .append(": ")
                                .append(MaterialTooltip.of(stack.amount, false)),
                        0xFFFF55);
            }
        }
    }

    @Override
    public MachineCaps caps() {
        return MachineCaps.of(IndustryBlockEntitiesSpace.MAGMA).powerIn().fe();
    }

    @java.lang.Override
    public boolean wantsNeighborUpdates() {
        return true;
    }

    @java.lang.Override
    public void cellNeighborChanged(
            net.minecraft.server.level.ServerLevel level,
            net.minecraft.core.BlockPos core,
            net.minecraft.core.BlockPos cell) {
        net.minecraft.world.level.block.entity.BlockEntity hbm$be = level.getBlockEntity(core);
        if (hbm$be instanceof MagmaBlockEntity) ((MagmaBlockEntity) hbm$be).refreshLava();
    }
}
