/*
 * SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
 * SPDX-FileCopyrightText: 2024-2025 George Paton
 * SPDX-FileCopyrightText: 2024 JamesH_2
 * SPDX-FileCopyrightText: 2024 StarlarkXVII
 * SPDX-License-Identifier: LGPL-3.0-only
 *
 * Copyright (C) 2026 movblock. All rights reserved.
 * Copyright (C) 2024-2025 George Paton. All rights reserved.
 * Copyright (C) 2024 JamesH_2. All rights reserved.
 * Copyright (C) 2024 StarlarkXVII. All rights reserved.
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

package com.hbmspace.client;

import com.hbm.api.environment.Environment;
import com.hbm.blocks.multiblock.MultiblockCoreBlock;
import com.hbm.client.ClientEffects;
import com.hbm.particle.RocketFlameColor;
import com.hbmspace.blockentity.machine.GasDockBlockEntity;
import com.hbmspace.blockentity.machine.HTR3BlockEntity;
import com.hbmspace.blockentity.machine.HTRF4BlockEntity;
import com.hbmspace.blockentity.machine.HTRNeoBlockEntity;
import com.hbmspace.blockentity.machine.LPW2SpaceBlockEntity;
import com.hbmspace.blockentity.machine.TransporterRocketBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public final class StationEngineEffects {

    private StationEngineEffects() {}

    public static void install() {
        LPW2SpaceBlockEntity.CLIENT_EFFECTS =
                be -> {
                    boolean burning = be.isOn && be.soundtime > 18;
                    be.audioLoop(burning, 1F);
                    if (burning) lpw2Contrail(be);
                };
        HTR3BlockEntity.CLIENT_EFFECTS =
                be -> {
                    boolean burning = be.isOn && be.soundtime > 18;
                    be.audioLoop(burning, 1F);
                    if (burning)
                        contrail(
                                be.getLevel(),
                                be.getBlockPos(),
                                be.getBlockState(),
                                7,
                                be.contrailType());
                };
        HTRF4BlockEntity.CLIENT_EFFECTS =
                be -> {
                    boolean burning = be.isOn && be.soundtime > 18;
                    be.audioLoop(burning, 1F);
                    if (burning)
                        contrail(
                                be.getLevel(),
                                be.getBlockPos(),
                                be.getBlockState(),
                                12,
                                be.contrailType());
                };
        HTRNeoBlockEntity.CLIENT_EFFECTS = be -> be.audioLoop(be.isOn && be.soundtime > 28, 1F);
        TransporterRocketBlockEntity.CLIENT_EFFECTS =
                be -> launch(be.getLevel(), be.getBlockPos(), be.launchTicks);
        GasDockBlockEntity.CLIENT_EFFECTS =
                be -> launch(be.getLevel(), be.getBlockPos(), be.launchTicks);
    }

    private static void lpw2Contrail(LPW2SpaceBlockEntity be) {
        Level level = be.getLevel();
        Direction rot = be.getBlockState().getValue(MultiblockCoreBlock.FACING).getClockWise();
        BlockPos pos = be.getBlockPos();
        double x = pos.getX() + rot.getStepX() * 8.5;
        double y = pos.getY() + 4.5;
        double z = pos.getZ() + rot.getStepZ() * 8.5;
        Player player = Minecraft.getInstance().player;
        if (player == null
                || Mth.lengthSquared(player.getX() - x, player.getEyeY() - y, player.getZ() - z)
                        > 350 * 350) return;
        ClientEffects.spawnRocketFlameInAir(
                level,
                x,
                y,
                z,
                3F,
                rot.getStepX() * 10,
                0,
                rot.getStepZ() * 10,
                20 + level.getRandom().nextInt(20),
                Environment.particlePressure(level));
    }

    private static void contrail(
            Level level, BlockPos pos, BlockState state, int reach, RocketFlameColor color) {
        Direction rot = state.getValue(MultiblockCoreBlock.FACING).getClockWise();
        double x = pos.getX() + rot.getStepX() * reach;
        double y = pos.getY() + 1;
        double z = pos.getZ() + rot.getStepZ() * reach;
        Player player = Minecraft.getInstance().player;
        if (player == null
                || Mth.lengthSquared(player.getX() - x, player.getEyeY() - y, player.getZ() - z)
                        > 350 * 350) return;

        ClientEffects.spawnRocketFlameInAir(
                level,
                x,
                y,
                z,
                3F,
                rot.getStepX() * 10,
                0,
                rot.getStepZ() * 10,
                40 + level.getRandom().nextInt(40),
                Environment.particlePressure(level),
                color);
    }

    private static void launch(Level level, BlockPos pos, int launchTicks) {
        if (launchTicks <= 0 || launchTicks >= 100) return;

        double x = pos.getX() + 0.5, z = pos.getZ() + 0.5;
        ClientEffects.spawnGasFlame(
                level, x, pos.getY() + 0.5 + launchTicks, z, 0.0, -1.0, 0.0, 6.5F);

        if (launchTicks < 10) {
            RandomSource rand = level.getRandom();
            ClientEffects.spawnSmokeShock(
                    level, x, pos.getY() + 0.5, z, 1 + rand.nextInt(3), 1 + rand.nextGaussian());
        }
    }
}
