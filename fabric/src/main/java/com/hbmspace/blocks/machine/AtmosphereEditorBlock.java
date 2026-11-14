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
import com.hbm.interfaces.LookOverlay;
import com.hbm.interfaces.LookOverlayProvider;
import com.hbm.interfaces.ToolInteractionHandler;
import com.hbm.interfaces.ToolType;
import com.hbm.inventory.fluid.NTMFluidProperties;
import com.hbmspace.blockentity.machine.AtmosphereEditorBlockEntity;
import com.hbmspace.dim.CelestialBody;
import com.hbmspace.dim.trait.CBT_Atmosphere;
import com.hbmspace.handler.atmosphere.AtmosphereText;
import com.hbmspace.util.AstronomyUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class AtmosphereEditorBlock extends Block
        implements TickingEntityBlock, ToolInteractionHandler, LookOverlayProvider {

    public AtmosphereEditorBlock(Properties props) {
        super(props);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new AtmosphereEditorBlockEntity(pos, state);
    }

    @Override
    public boolean onToolUse(
            Level level,
            Player player,
            BlockPos pos,
            @Nullable Direction side,
            Vec3 hit,
            ToolType tool) {
        if (tool != ToolType.SCREWDRIVER && tool != ToolType.HAND_DRILL && tool != ToolType.DEFUSER)
            return false;
        if (level.isClientSide()) return true;
        if (!(level.getBlockEntity(pos) instanceof AtmosphereEditorBlockEntity editor))
            return false;

        switch (tool) {
            case SCREWDRIVER -> editor.hbm$syncSet$AtmosphereEditorBlockEntity$isOn(!editor.isOn);
            case HAND_DRILL ->
                    editor.hbm$syncSet$AtmosphereEditorBlockEntity$throughputFactor(
                            (int) (editor.throughputFactor + (player.isShiftKeyDown() ? -1 : 1)));
            default ->
                    editor.hbm$syncSet$AtmosphereEditorBlockEntity$isEmitting(!editor.isEmitting);
        }
        editor.setChanged();
        return true;
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
        Fluid type = AtmosphereText.identified(stack);
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        if (type == null
                || !(level.getBlockEntity(pos) instanceof AtmosphereEditorBlockEntity editor)) {
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        }

        editor.hbm$syncSet$AtmosphereEditorBlockEntity$fluid(type);
        editor.setChanged();
        AtmosphereText.changedType(player, type);
        return InteractionResult.SUCCESS;
    }

    @Override
    public void buildLookOverlay(Level level, BlockPos pos, LookOverlay info) {
        if (CelestialBody.inOrbit(level)) return;
        if (!(level.getBlockEntity(pos) instanceof AtmosphereEditorBlockEntity editor)) return;

        CBT_Atmosphere atmosphere = CelestialBody.getTrait(level, CBT_Atmosphere.class);
        double pressure = atmosphere != null ? atmosphere.getPressure(editor.fluid) : 0;
        if (pressure < 0.0001) pressure = 0;
        pressure = Math.round(pressure * 1_000.0) / 1_000.0;

        info.title(getName());
        info.row(
                Component.translatable(
                        "overlay.hbmspace.state",
                        Component.translatable(
                                editor.isOn
                                        ? "overlay.hbmspace.state.running"
                                        : "overlay.hbmspace.state.off")));
        info.row(
                Component.translatable(
                        "overlay.hbmspace.anatmogenesis.gas",
                        NTMFluidProperties.clientName(editor.fluid),
                        pressure));
        info.row(
                Component.translatable(
                        "overlay.hbmspace.current_mode",
                        Component.translatable(
                                editor.isEmitting
                                        ? "overlay.hbmspace.current_mode.emitting"
                                        : "overlay.hbmspace.current_mode.capturing")));
        info.row(
                Component.translatable(
                        "overlay.hbmspace.current_throughput",
                        Math.pow(10, editor.throughputFactor) / AstronomyUtil.MB_PER_ATM));
    }
}
