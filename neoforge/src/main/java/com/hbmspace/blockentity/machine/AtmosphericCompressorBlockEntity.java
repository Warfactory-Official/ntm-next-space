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
import com.hbm.api.fluidmk2.FluidFlushOutputs;
import com.hbm.api.fluidmk2.FluidTankHandler;
import com.hbm.api.fluidmk2.FlushFaces;
import com.hbm.api.fluidmk2.FlushLanes;
import com.hbm.blockentity.MachineBlockEntity;
import com.hbm.inventory.fluid.tank.FluidTankAccess;
import com.hbm.inventory.fluid.tank.FluidTankNTM;
import com.hbm.packet.SyncUnitSchema;
import com.hbmspace.blocks.AtmosphereBlockEntitiesSpace;
import com.hbmspace.dim.CelestialBody;
import com.hbmspace.dim.trait.CBT_Atmosphere;
import com.hbmspace.handler.atmosphere.ChunkAtmosphereManager;
import com.hbmspace.handler.atmosphere.GasExchange;
import com.hbmspace.inventory.fluid.FluidsSpace;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

public class AtmosphericCompressorBlockEntity extends MachineBlockEntity
        implements FluidTankAccess, EnergyHandler, FluidTankHandler, SyncUnitSchema {

    public static final long MAX_POWER = 1_000_000L;
    public static final int RATE = 200;
    private static final float MAX_SPEED = 30F;

    public final FluidTankNTM tank = new FluidTankNTM(FluidsSpace.earthair(), 50_000);
    private final FluidTankNTM[] tanks = {tank};
    public long power;

    public float rot;
    public float prevRot;
    private float rotSpeed;

    private final FluidFlushOutputs flush = new FluidFlushOutputs();

    public AtmosphericCompressorBlockEntity(BlockPos pos, BlockState state) {
        super(AtmosphereBlockEntitiesSpace.ATMOSPHERIC_COMPRESSOR.get(), pos, state, 0);
    }

    @Override
    public void tickServer() {
        ServerLevel server = (ServerLevel) level;
        Fluid typeBefore = tank.getTankType();
        int fillBefore = tank.getFill();

        CBT_Atmosphere atmosphere = CelestialBody.inOrbit(server) ? null : openSky(server);
        if (atmosphere != null && atmosphere.getPressure() > 0.0D) {
            if (!atmosphere.hasFluid(tank.getTankType()))
                tank.setTankType(atmosphere.getMainFluid());
        } else {
            tank.setTankType(null);
        }

        if (hasPower()
                && tank.getTankType() != null
                && tank.getFill() + RATE <= tank.getMaxFill()) {
            tank.setFill(tank.getFill() + RATE);
            hbm$syncSet$AtmosphericCompressorBlockEntity$power((long) (power - (MAX_POWER / 50)));
            GasExchange.capture(server, tank.getTankType(), RATE);
        }

        if (tank.getTankType() != typeBefore || tank.getFill() != fillBefore) setChanged();
        flush.provide(server, this);
        networkPackNT(50);
    }

    private @Nullable CBT_Atmosphere openSky(ServerLevel server) {
        return ChunkAtmosphereManager.proxy.hasAtmosphere(server, worldPosition)
                ? null
                : CelestialBody.getTrait(server, CBT_Atmosphere.class);
    }

    @Override
    public void tickClient() {
        if (hasPower()) {
            rotSpeed = Math.min(rotSpeed + 0.2F, MAX_SPEED);
        } else {
            rotSpeed = Math.max(rotSpeed - 0.1F, 0F);
        }

        prevRot = rot;
        rot += rotSpeed;

        if (rot >= 360F) {
            rot -= 360F;
            prevRot -= 360F;
        }
    }

    public boolean hasPower() {
        return power >= MAX_POWER / 50;
    }

    public void cycleGas() {
        CBT_Atmosphere atmosphere = openSky((ServerLevel) level);
        if (atmosphere == null) return;

        atmosphere = atmosphere.clone();
        atmosphere.sortDescending();

        Fluid current = tank.getTankType();
        for (int i = 0; i < atmosphere.fluids.size(); i++) {
            if (atmosphere.fluids.get(i).fluid == current) {
                int target = i + 1;
                if (target >= atmosphere.fluids.size()) target = 0;
                if (atmosphere.fluids.get(target).pressure <= 0.0001) target = 0;

                tank.setTankType(atmosphere.fluids.get(target).fluid);
                setChanged();
                break;
            }
        }
    }

    public boolean switchGas(Fluid target) {
        if (target == tank.getTankType()) return false;

        CBT_Atmosphere atmosphere = openSky((ServerLevel) level);
        if (atmosphere == null) return false;

        for (CBT_Atmosphere.FluidEntry entry : atmosphere.fluids) {
            if (entry.fluid == target) {
                tank.setTankType(target);
                setChanged();
                return true;
            }
        }
        return false;
    }

    @Override
    public long getPower() {
        return power;
    }

    @Override
    public void setPower(long p) {
        hbm$syncSet$AtmosphericCompressorBlockEntity$power(p);
    }

    @Override
    public long getMaxPower() {
        return MAX_POWER;
    }

    @Override
    public FluidTankNTM[] getSendingTanks() {
        return tanks;
    }

    @Override
    public void declareFlush(FlushLanes out) {
        for (FluidTankNTM tank : getSendingTanks()) out.add(tank, FlushFaces.own());
    }

    @Override
    public int tankCount() {
        return tanks.length;
    }

    @Override
    public FluidTankNTM tank(int index) {
        return tanks[index];
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        hbm$syncSet$AtmosphericCompressorBlockEntity$power(input.getLongOr("power", 0L));
        input.child("water").ifPresent(tank::deserialize);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putLong("power", power);
        tank.serialize(output.child("water"));
    }

    @Override
    public long syncUnitMask() {
        return 0x3L;
    }

    @Override
    public void writeSyncUnit(int unit, ByteBuf output) {
        switch (unit) {
            case 0 -> output.writeLong(power);
            case 1 -> tank.packetSerialize(output);
            default -> throw new IllegalArgumentException();
        }
    }

    @Override
    public void readSyncUnit(int unit, ByteBuf input) {
        switch (unit) {
            case 0 -> hbm$syncSet$AtmosphericCompressorBlockEntity$power(input.readLong());
            case 1 -> tank.packetDeserialize(input);
            default -> throw new IllegalArgumentException();
        }
    }

    public final void hbm$syncSet$AtmosphericCompressorBlockEntity$power(long next) {
        if (this.power != next) {
            this.power = next;
            if ((1L << 0) == 0L) syncChanged(3);
            else syncUnitsChanged(3, 1L << 0);
        }
    }

    static {
        com.hbm.packet.SyncBindings.field(
                AtmosphericCompressorBlockEntity.class, "tank", 3, 1L << 1);
    }
}
