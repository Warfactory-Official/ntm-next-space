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
import com.hbm.blocks.multiblock.MultiblockCoreBlock;
import com.hbm.interfaces.LookOverlay;
import com.hbm.interfaces.LookOverlayProvider;
import com.hbm.items.ModItems;
import com.hbm.util.NumberFormatter;
import com.hbmspace.blockentity.machine.DysonReceiverBlockEntity;
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

public class DysonReceiverBlock extends MultiblockCoreBlock
        implements TickingEntityBlock, LookOverlayProvider {
    public DysonReceiverBlock(Properties properties) {
        super(properties);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new DysonReceiverBlockEntity(pos, state);
    }

    @Override
    protected InteractionResult useItemOnAtCore(
            ItemStack held,
            BlockState coreState,
            Level level,
            BlockPos core,
            Player player,
            InteractionHand hand,
            BlockHitResult hit) {
        if (level.isClientSide() || player.isShiftKeyDown()) return InteractionResult.SUCCESS;
        if (!(level.getBlockEntity(core) instanceof DysonReceiverBlockEntity receiver))
            return InteractionResult.PASS;

        if (held.is(ModItems.SAT_CHIP.get())) {
            if (!receiver.getItem(0).isEmpty()) return InteractionResult.PASS;
            receiver.setItem(0, held.copy());
            held.consume(held.getCount(), player);
            DysonLauncherBlock.plug(level, hit.getBlockPos());
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    protected InteractionResult useAtCore(
            BlockState coreState, Level level, BlockPos core, Player player, BlockHitResult hit) {
        if (level.isClientSide() || player.isShiftKeyDown()) return InteractionResult.SUCCESS;
        if (!(level.getBlockEntity(core) instanceof DysonReceiverBlockEntity receiver))
            return InteractionResult.PASS;

        ItemStack chip = receiver.getItem(0);
        if (!chip.isEmpty()) {
            receiver.setItem(0, ItemStack.EMPTY);
            player.getInventory().placeItemBackInInventory(chip);
            DysonLauncherBlock.plug(level, hit.getBlockPos());
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void buildLookOverlay(Level level, BlockPos pos, LookOverlay info) {
        if (!(level.getBlockEntity(pos) instanceof DysonReceiverBlockEntity receiver)) return;

        long energyOutput =
                receiver.isReceiving
                        ? DysonReceiverBlockEntity.getEnergyOutput(receiver.swarmCount)
                                / receiver.swarmConsumers
                                * 20
                        : 0;

        info.title(getName());
        if (receiver.swarmId > 0) {
            info.row(Component.translatable("overlay.hbmspace.dyson.id", receiver.swarmId));
            info.row(Component.translatable("overlay.hbmspace.dyson.swarm", receiver.swarmCount));
            info.row(
                    Component.translatable(
                            "overlay.hbmspace.dyson_receiver.consumers", receiver.swarmConsumers));
            info.row(
                    Component.translatable(
                            "overlay.hbmspace.dyson_receiver.power",
                            NumberFormatter.formatCompact(energyOutput)));
        } else {
            info.row(Component.translatable("overlay.hbmspace.dyson.no_chip"));
        }
    }
}
