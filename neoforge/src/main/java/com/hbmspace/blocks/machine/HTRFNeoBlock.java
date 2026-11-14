/*
 * SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
 * SPDX-FileCopyrightText: 2026 George Paton
 * SPDX-FileCopyrightText: 2025 JamesH_2
 * SPDX-License-Identifier: LGPL-3.0-only
 *
 * Copyright (C) 2026 movblock. All rights reserved.
 * Copyright (C) 2026 George Paton. All rights reserved.
 * Copyright (C) 2025 JamesH_2. All rights reserved.
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

import com.hbm.blockentity.machine.fusion.FusionPorts;
import com.hbm.blocks.machine.fusion.FusionMachineBlock;
import com.hbm.capability.CapabilityBlock;
import com.hbm.capability.MachineCaps;
import com.hbm.interfaces.LookOverlay;
import com.hbm.interfaces.LookOverlayProvider;
import com.hbm.inventory.fluid.NTMFluidProperties;
import com.hbm.inventory.fluid.tank.FluidTankNTM;
import com.hbm.util.Blink;
import com.hbm.util.NumberFormatter;
import com.hbmspace.blockentity.machine.HTRNeoBlockEntity;
import com.hbmspace.blocks.StationBlockEntitiesSpace;
import com.hbmspace.dim.CelestialBody;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

public class HTRFNeoBlock extends FusionMachineBlock
        implements CapabilityBlock, LookOverlayProvider {
    public HTRFNeoBlock(Properties props) {
        super(props);
    }

    @Override
    public Direction getDirModified(Direction dir) {
        return dir.getCounterClockWise();
    }

    private static BlockPos plasmaCell(BlockPos core, Direction facing) {
        return core.relative(facing.getClockWise().getOpposite(), 9);
    }

    @Override
    public List<FusionPorts.Port> links(BlockPos core, Direction facing) {
        return List.of(
                new FusionPorts.Port(
                        FusionPorts.Kind.PLASMA,
                        plasmaCell(core, facing),
                        facing.getClockWise().getOpposite()));
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new HTRNeoBlockEntity(pos, state);
    }

    @Override
    public MachineCaps caps() {
        return MachineCaps.of(StationBlockEntitiesSpace.HTRF4_NEO)
                .powerIn()
                .fluidIn()
                .fluidOut()
                .fe();
    }

    @Override
    public void buildLookOverlay(Level level, BlockPos pos, LookOverlay info) {
        if (!CelestialBody.inOrbit(level)) return;
        if (!(level.getBlockEntity(pos) instanceof HTRNeoBlockEntity thruster)) return;

        info.title(getName());

        if (!thruster.isFacingPrograde()) {
            info.row(
                    Component.literal("! ! ! ")
                            .append(Component.translatable("atmosphere.engineFacing"))
                            .append(" ! ! !"),
                    Blink.lit() ? 0xFF0000 : 0xFFFF00);
            return;
        }

        int heat = (int) Math.ceil(thruster.temperature);
        for (Component text :
                List.of(
                        Component.translatable(
                                "overlay.hbmspace.htrf4neo.energy",
                                Component.literal(
                                                NumberFormatter.formatCompact(
                                                        thruster.plasmaEnergy))
                                        .withStyle(
                                                thruster.plasmaEnergy == 0
                                                        ? ChatFormatting.RED
                                                        : ChatFormatting.GREEN)),
                        Component.translatable(
                                "overlay.hbmspace.htrf4neo.power_level",
                                Component.literal(NumberFormatter.formatCompact(thruster.power))
                                        .withStyle(
                                                thruster.power < thruster.getMaxPower()
                                                        ? ChatFormatting.RED
                                                        : ChatFormatting.GREEN)),
                        Component.translatable(
                                "overlay.hbmspace.htrf4neo.temperature",
                                Component.literal(Integer.toString(heat))
                                        .withStyle(
                                                heat > 123
                                                        ? ChatFormatting.RED
                                                        : ChatFormatting.AQUA)))) {
            var line = Component.empty();
            var color = TextColor.WHITE;
            for (Component part : text.toFlatList()) {
                var style = part.getStyle();
                if (style.getColor() != null) color = style.getColor();
                line.append(part.copy().withStyle(style.withColor(color)));
            }
            info.row(line);
        }

        FluidTankNTM cold = thruster.coolantTanks[0];
        FluidTankNTM hot = thruster.coolantTanks[1];
        info.row(
                LookOverlay.inflow(
                        Component.translatable(
                                "overlay.hbm.tank",
                                NTMFluidProperties.clientName(cold.getTankType()),
                                cold.getFill(),
                                cold.getMaxFill())));
        info.row(
                LookOverlay.outflow(
                        Component.translatable(
                                "overlay.hbm.tank",
                                NTMFluidProperties.clientName(hot.getTankType()),
                                hot.getFill(),
                                hot.getMaxFill())));

        if (!thruster.isCool()) {
            info.row(
                    Component.literal("! ! ! ")
                            .append(Component.translatable("overlay.hbmspace.htrf4neo.cooling"))
                            .append(" ! ! !"),
                    Blink.lit() ? 0xFF0000 : 0xFFFF00);
        }

        if (plasmaCell(pos, coreFacing(level.getBlockState(pos))).equals(info.hit())) {
            info.row(Component.translatable("overlay.hbmspace.htrf4neo.feed"));
        }
    }
}
