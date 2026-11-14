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
import com.hbm.inventory.fluid.NTMFluids;
import com.hbm.inventory.fluid.tank.FluidTankAccess;
import com.hbm.inventory.fluid.tank.FluidTankNTM;
import com.hbm.packet.SyncUnitSchema;
import com.hbm.util.TickPhase;
import com.hbmspace.blocks.AtmosphereBlockEntitiesSpace;
import com.hbmspace.handler.atmosphere.AtmosphereBlob;
import com.hbmspace.handler.atmosphere.ChunkAtmosphereManager;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

public class AirScrubberBlockEntity extends MachineBlockEntity
        implements FluidTankAccess, EnergyHandler, FluidTankHandler, SyncUnitSchema {

    public static final long MAX_POWER = 10_000L;
    public static final long MIN_POWER = 200L;
    public static final long POWER_PER_MB = 10L;
    private static final float MAX_SPEED = 30F;

    public final FluidTankNTM tank = new FluidTankNTM(NTMFluids.CARBONDIOXIDE, 16_000);
    private final FluidTankNTM[] tanks = {tank};
    public long power;

    public float rot;
    public float prevRot;
    private float rotSpeed;

    private @Nullable AirPumpBlockEntity pump;
    private final FluidFlushOutputs flush = new FluidFlushOutputs();

    public AirScrubberBlockEntity(BlockPos pos, BlockState state) {
        super(AtmosphereBlockEntitiesSpace.AIR_SCRUBBER.get(), pos, state, 0);
    }

    @Override
    public void tickServer() {
        if (canOperate()
                && TickPhase.every(this, 5)
                && (pump == null || pump.getFluidPressure() == 0 || !pump.registerScrubber(this))) {
            pump = findPump((ServerLevel) level);
        }

        flush.provide((ServerLevel) level, this);
        networkPackNT(20);
    }

    private @Nullable AirPumpBlockEntity findPump(ServerLevel level) {
        for (AtmosphereBlob blob : ChunkAtmosphereManager.proxy.getBlobs(level, worldPosition)) {
            if (level.getBlockEntity(blob.getRootPosition()) instanceof AirPumpBlockEntity candidate
                    && candidate.registerScrubber(this)) {
                return candidate;
            }
        }
        return null;
    }

    @Override
    public void tickClient() {
        if (canOperate()) {
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

    public boolean canOperate() {
        return power > MIN_POWER;
    }

    public int scrub(int amount) {
        if (!canOperate()) return 0;
        int add = Math.min(tank.getMaxFill() - tank.getFill(), amount);
        tank.setFill(tank.getFill() + add);
        hbm$syncSet$AirScrubberBlockEntity$power((long) (power - (add * POWER_PER_MB)));
        if (add > 0) setChanged();
        return add;
    }

    @Override
    public long getPower() {
        return power;
    }

    @Override
    public void setPower(long p) {
        hbm$syncSet$AirScrubberBlockEntity$power(p);
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
        hbm$syncSet$AirScrubberBlockEntity$power(input.getLongOr("power", 0L));
        input.child("t").ifPresent(tank::deserialize);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putLong("power", power);
        tank.serialize(output.child("t"));
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
            case 0 -> hbm$syncSet$AirScrubberBlockEntity$power(input.readLong());
            case 1 -> tank.packetDeserialize(input);
            default -> throw new IllegalArgumentException();
        }
    }

    public final void hbm$syncSet$AirScrubberBlockEntity$power(long next) {
        if (this.power != next) {
            this.power = next;
            if ((1L << 0) == 0L) syncChanged(3);
            else syncUnitsChanged(3, 1L << 0);
        }
    }

    static {
        com.hbm.packet.SyncBindings.field(AirScrubberBlockEntity.class, "tank", 3, 1L << 1);
    }
}
