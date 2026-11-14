/*
 * SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
 * SPDX-FileCopyrightText: 2025 George Paton
 * SPDX-License-Identifier: LGPL-3.0-only
 *
 * Copyright (C) 2026 movblock. All rights reserved.
 * Copyright (C) 2025 George Paton. All rights reserved.
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
import com.hbm.blocks.multiblock.MultiblockCellShapes;
import com.hbm.blocks.multiblock.MultiblockCoreBlock;
import com.hbm.blocks.multiblock.MultiblockCoreIndex;
import com.hbm.blocks.multiblock.MultiblockMaskTable;
import com.hbm.capability.CapabilityBlock;
import com.hbm.capability.MachineCaps;
import com.hbm.interfaces.LookOverlay;
import com.hbm.interfaces.LookOverlayProvider;
import com.hbm.inventory.fluid.NTMFluidProperties;
import com.hbm.inventory.fluid.tank.FluidTankNTM;
import com.hbm.util.NumberFormatter;
import com.hbmspace.blockentity.machine.HydroponicBlockEntity;
import com.hbmspace.blocks.CropBlock;
import com.hbmspace.blocks.IndustryBlockEntitiesSpace;
import com.hbmspace.blocks.IndustryBlocksSpace;
import com.hbmspace.blocks.SpaceCellBuckets;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

public class HydroponicBlock extends MultiblockCoreBlock
        implements TickingEntityBlock, LookOverlayProvider, CapabilityBlock {
    private static final int LAMP_Y = 2;

    public HydroponicBlock(Properties props) {
        super(props);
    }

    private static final int END_CELL = 1, LAMP = 2;

    @Override
    protected BlockState cellStateOfKind(int kind, Direction facing, int shapeId) {
        if (kind != END_CELL) return super.cellStateOfKind(kind, facing, shapeId);
        return IndustryBlocksSpace.HYDROBAY_END_CELL
                .get()
                .defaultBlockState()
                .setValue(HydrobayEndCell.ALONG_X, facing.getAxis() == Direction.Axis.Z);
    }

    @Override
    protected @Nullable BlockState swappedStateOfKind(int kind, Direction facing) {
        return kind == LAMP ? litLamp() : null;
    }

    private static BlockState litLamp() {
        return SpaceCellBuckets.HYDROBAY_LIT_CELLS.get().defaultBlockState();
    }

    public void setLights(ServerLevel level, BlockPos core, Direction facing, boolean lit) {
        List<BlockPos> swapped = new ArrayList<>(2);
        MultiblockCoreBlock.withoutTeardown(
                () -> {
                    for (int lx = -1; lx <= 1; lx += 2) {
                        BlockState state =
                                lit
                                        ? litLamp()
                                        : cellStateFor(
                                                lx,
                                                LAMP_Y,
                                                0,
                                                facing,
                                                MultiblockCellShapes.idAt(
                                                        this, lx, LAMP_Y, 0, facing));
                        BlockPos lamp =
                                core.offset(
                                        MultiblockMaskTable.worldX(lx, LAMP_Y, 0, facing),
                                        LAMP_Y,
                                        MultiblockMaskTable.worldZ(lx, LAMP_Y, 0, facing));
                        if (level.setBlock(lamp, state, Block.UPDATE_ALL)) swapped.add(lamp);
                    }
                });
        if (!swapped.isEmpty()) claimCells(level, core, swapped, MultiblockCoreIndex.FOLDED);
    }

    public static net.minecraft.world.level.block.@Nullable CropBlock hydroponicCrop(
            ItemStack seed) {
        if (!(seed.getItem() instanceof BlockItem item)
                || !(item.getBlock() instanceof net.minecraft.world.level.block.CropBlock crop))
            return null;
        return crop instanceof CropBlock space && !space.canHydro() ? null : crop;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new HydroponicBlockEntity(pos, state);
    }

    @Override
    public void buildLookOverlay(Level level, BlockPos pos, LookOverlay info) {
        if (!(level.getBlockEntity(pos) instanceof HydroponicBlockEntity hydro)) return;

        info.title(Component.translatable(getDescriptionId()));
        info.row(
                Component.translatable(
                                "overlay.hbm.power", NumberFormatter.formatCompact(hydro.power))
                        .withStyle(
                                hydro.power <= HydroponicBlockEntity.LIGHT_POWER
                                        ? ChatFormatting.RED
                                        : ChatFormatting.GREEN));
        info.row(LookOverlay.inflow(tankLine(hydro.tanks[0])));
        info.row(LookOverlay.outflow(tankLine(hydro.tanks[1])));
    }

    private static Component tankLine(FluidTankNTM tank) {
        return Component.translatable(
                "overlay.hbm.tank",
                NTMFluidProperties.clientName(tank.getTankType()),
                tank.getFill(),
                tank.getMaxFill());
    }

    @Override
    public MachineCaps caps() {
        return MachineCaps.of(IndustryBlockEntitiesSpace.HYDROBAY)
                .powerIn()
                .fluidIn()
                .fluidOut()
                .items()
                .itemsAtCells()
                .fe();
    }
}
