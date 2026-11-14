/*
 * SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
 * SPDX-FileCopyrightText: 2024 George Paton
 * SPDX-License-Identifier: LGPL-3.0-only
 *
 * Copyright (C) 2026 movblock. All rights reserved.
 * Copyright (C) 2024 George Paton. All rights reserved.
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
import com.hbm.capability.CapabilityBlock;
import com.hbm.capability.MachineCaps;
import com.hbm.interfaces.LookOverlay;
import com.hbm.interfaces.LookOverlayProvider;
import com.hbm.inventory.fluid.NTMFluidProperties;
import com.hbmspace.blockentity.machine.CryoDistillBlockEntity;
import com.hbmspace.blocks.IndustryBlockEntitiesSpace;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

public class CryoDistillBlock extends MultiblockCoreBlock
        implements TickingEntityBlock, LookOverlayProvider, CapabilityBlock {
    public CryoDistillBlock(Properties props) {
        super(props);
    }

    private static BlockPos input(BlockPos core, Direction facing) {
        return core.below(2).relative(facing, -1).relative(facing.getClockWise(), -2);
    }

    private static BlockPos power(BlockPos core, Direction facing) {
        return core.below(2).relative(facing, -2).relative(facing.getClockWise(), -2);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CryoDistillBlockEntity(pos, state);
    }

    @Override
    public void buildLookOverlay(Level level, BlockPos pos, LookOverlay info) {
        BlockPos hit = info.hit();
        if (hit == null) return;
        if (!(level.getBlockEntity(pos) instanceof CryoDistillBlockEntity distill)) return;

        Direction facing = distill.getBlockState().getValue(FACING);
        Direction rot = facing.getClockWise();
        if (hit.equals(input(pos, facing))) {
            info.row(
                    LookOverlay.inflow(
                            Component.literal(
                                    NTMFluidProperties.clientName(
                                            distill.tanks[0].getTankType()))));
        }
        if (hit.equals(power(pos, facing))) {
            info.row(LookOverlay.inflow(Component.translatable("overlay.hbm.power_word")));
        }
        for (int side : new int[] {-2, -1, 1, 2}) {
            if (!hit.equals(pos.below(2).relative(facing, 3).relative(rot, side))) continue;
            for (int i = 1; i < distill.tanks.length; i++) {
                info.row(
                        LookOverlay.outflow(
                                Component.literal(
                                        NTMFluidProperties.clientName(
                                                distill.tanks[i].getTankType()))));
            }
        }

        if (!info.isEmpty()) info.title(Component.translatable(getDescriptionId()));
    }

    @Override
    public MachineCaps caps() {
        return MachineCaps.of(IndustryBlockEntitiesSpace.CRYO_DISTILL)
                .powerIn()
                .fluidIn()
                .fluidOut()
                .fe()
                .faces(CryoDistillBlockEntity.class, (be, side) -> side != Direction.DOWN)
                .fluidFaces(
                        CryoDistillBlockEntity.class, (be, face) -> face.side() != Direction.DOWN);
    }
}
