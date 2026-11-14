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

package com.hbmspace.blockentity.machine;

import com.hbm.api.energymk2.EnergyHandler;
import com.hbm.blockentity.MachineBlockEntity;
import com.hbm.blocks.multiblock.MultiblockCoreBlock;
import com.hbm.handler.threading.TargetPoint;
import com.hbm.packet.PacketWire;
import com.hbm.packet.SyncUnitSchema;
import com.hbm.packet.layout.EffectHadronLayout;
import com.hbm.particle.helper.ParticleCreators;
import com.hbm.util.TickPhase;
import com.hbmspace.blockentity.DysonEnergyHandler;
import com.hbmspace.blocks.SkyBlockEntitiesSpace;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;

public class DysonConverterHEBlockEntity extends MachineBlockEntity
        implements DysonEnergyHandler, EnergyHandler, SyncUnitSchema {

    public long power;
    public boolean isConverting;
    private int cooldown;

    public DysonConverterHEBlockEntity(BlockPos pos, BlockState state) {
        super(SkyBlockEntitiesSpace.DYSON_CONVERTER_HE.get(), pos, state, 0);
    }

    @Override
    public void tickServer() {
        ServerLevel level = (ServerLevel) this.level;
        Direction dir = MultiblockCoreBlock.coreFacing(getBlockState()).getOpposite();
        Direction rot = dir.getClockWise();

        hbm$syncSet$DysonConverterHEBlockEntity$isConverting(power > 0);

        if (isConverting && TickPhase.every(this, 2)) {
            boolean tau = TickPhase.every(level.getGameTime() / 2, worldPosition, 5);
            spark(level, tau, dir, 4, 2.25, rot);
            spark(level, tau, dir, 4, 2.25, rot.getOpposite());
            spark(level, tau, dir, 3, 2.75, rot);
            spark(level, tau, dir, 3, 2.75, rot.getOpposite());

            cooldown++;
            if (cooldown > 10) {
                power = 0;
                cooldown = 0;
            }
        }

        networkPackNT(250);
    }

    private void spark(
            ServerLevel level, boolean tau, Direction dir, int forward, double up, Direction side) {
        double x = worldPosition.getX() + 0.5 + dir.getStepX() * forward + side.getStepX();
        double y = worldPosition.getY() + up;
        double z = worldPosition.getZ() + 0.5 + dir.getStepZ() * forward + side.getStepZ();
        if (tau) {
            ParticleCreators.sparks(level, x, y, z, 1, false, 25);
        } else {
            PacketWire.sendToAllAround(
                    EffectHadronLayout.encode(x, y, z),
                    new TargetPoint(
                            level,
                            worldPosition.getX(),
                            worldPosition.getY(),
                            worldPosition.getZ(),
                            25));
        }
    }

    @Override
    public boolean provideEnergy(BlockPos pos, long energy) {
        Direction facing = MultiblockCoreBlock.coreFacing(getBlockState());
        if (!pos.equals(worldPosition.relative(facing, 4).above())) return false;

        power = energy;
        cooldown = 0;
        return true;
    }

    @Override
    public long getPower() {
        return power;
    }

    @Override
    public void setPower(long power) {
        this.power = power;
    }

    @Override
    public long getMaxPower() {
        return power;
    }

    @Override
    public long syncUnitMask() {
        return 1L;
    }

    @Override
    public void writeSyncUnit(int unit, ByteBuf output) {
        if (unit != 0) throw new IllegalArgumentException();
        output.writeBoolean(isConverting);
    }

    @Override
    public void readSyncUnit(int unit, ByteBuf input) {
        if (unit != 0) throw new IllegalArgumentException();
        hbm$syncSet$DysonConverterHEBlockEntity$isConverting(input.readBoolean());
    }

    public final void hbm$syncSet$DysonConverterHEBlockEntity$isConverting(boolean next) {
        if (this.isConverting != next) {
            this.isConverting = next;
            if ((1L) == 0L) syncChanged(3);
            else syncUnitsChanged(3, 1L);
        }
    }
}
