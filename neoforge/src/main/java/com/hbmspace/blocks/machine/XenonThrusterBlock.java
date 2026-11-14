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
import com.hbm.inventory.fluid.NTMFluidProperties;
import com.hbm.inventory.fluid.tank.FluidTankNTM;
import com.hbm.util.Blink;
import com.hbm.util.NumberFormatter;
import com.hbmspace.blockentity.machine.XenonThrusterBlockEntity;
import com.hbmspace.blocks.StationBlockEntitiesSpace;
import com.hbmspace.dim.CelestialBody;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

public class XenonThrusterBlock extends MultiblockCoreBlock
        implements TickingEntityBlock, CapabilityBlock, LookOverlayProvider {
    public XenonThrusterBlock(Properties props) {
        super(props);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new XenonThrusterBlockEntity(pos, state);
    }

    @Override
    public MachineCaps caps() {
        return MachineCaps.of(StationBlockEntitiesSpace.XENON_THRUSTER)
                .powerIn()
                .fluidIn()
                .fe()
                .faces(
                        XenonThrusterBlockEntity.class,
                        (be, side) -> CelestialBody.inOrbit(be.getLevel()))
                .fluidFaces(
                        XenonThrusterBlockEntity.class,
                        (be, face) -> CelestialBody.inOrbit(be.getLevel()));
    }

    @Override
    public void buildLookOverlay(Level level, BlockPos pos, LookOverlay info) {
        if (!CelestialBody.inOrbit(level)) return;
        if (!(level.getBlockEntity(pos) instanceof XenonThrusterBlockEntity thruster)) return;

        info.title(getName());

        if (!thruster.isFacingPrograde()) {
            info.row(
                    Component.literal("! ! ! ")
                            .append(Component.translatable("atmosphere.engineFacing"))
                            .append(" ! ! !"),
                    Blink.lit() ? 0xFF0000 : 0xFFFF00);
            return;
        }

        info.row(
                Component.translatable("unit.energy", NumberFormatter.formatCompact(thruster.power))
                        .withStyle(
                                thruster.power == 0 ? ChatFormatting.RED : ChatFormatting.GREEN));
        for (FluidTankNTM tank : thruster.tanks) {
            info.row(
                    Component.literal("-> ")
                            .withStyle(ChatFormatting.GREEN)
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
}
