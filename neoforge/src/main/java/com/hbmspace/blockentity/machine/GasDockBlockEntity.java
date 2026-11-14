/*
 * SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
 * SPDX-FileCopyrightText: 2024-2025 George Paton
 * SPDX-FileCopyrightText: 2024 JamesH_2
 * SPDX-License-Identifier: LGPL-3.0-only
 *
 * Copyright (C) 2026 movblock. All rights reserved.
 * Copyright (C) 2024-2025 George Paton. All rights reserved.
 * Copyright (C) 2024 JamesH_2. All rights reserved.
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

import com.hbm.api.fluidmk2.FluidFlushOutputs;
import com.hbm.api.fluidmk2.FluidTankHandler;
import com.hbm.api.fluidmk2.FlushFaces;
import com.hbm.api.fluidmk2.FlushLanes;
import com.hbm.blockentity.MachineBlockEntity;
import com.hbm.inventory.fluid.NTMFluids;
import com.hbm.inventory.fluid.tank.FluidTankAccess;
import com.hbm.inventory.fluid.tank.FluidTankNTM;
import com.hbm.packet.SyncUnitSchema;
import com.hbmspace.blocks.StationBlockEntitiesSpace;
import com.hbmspace.dim.CelestialBody;
import com.hbmspace.inventory.fluid.FluidsSpace;
import io.netty.buffer.ByteBuf;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class GasDockBlockEntity extends MachineBlockEntity
        implements FluidTankAccess, FluidTankHandler, SyncUnitSchema {

    public static Consumer<GasDockBlockEntity> CLIENT_EFFECTS = be -> {};

    private static final long ROCKET_UNIT = 1L << 0;
    private static final long TANKS_UNIT = 1L << 1;

    public final FluidTankNTM[] tanks = {
        new FluidTankNTM(FluidsSpace.joolgas(), 64_000),
        new FluidTankNTM(NTMFluids.HYDROGEN, 32_000),
        new FluidTankNTM(NTMFluids.OXYGEN, 32_000)
    };
    private final FluidTankNTM[] sending = {tanks[0]};
    private final FluidTankNTM[] receiving = {tanks[1], tanks[2]};
    private final FluidFlushOutputs flush = new FluidFlushOutputs();

    public boolean hasRocket;

    {
        hbm$syncSet$GasDockBlockEntity$hasRocket(true);
    }

    public int launchTicks = 0;

    public GasDockBlockEntity(BlockPos pos, BlockState state) {
        super(StationBlockEntitiesSpace.GAS_DOCK.get(), pos, state, 0);
    }

    @Override
    public void tickServer() {
        flush.provide((ServerLevel) level, this);

        CelestialBody planet = CelestialBody.getPlanet(level);

        stepLaunch();

        if (planet.gas != null) {
            tanks[0].setTankType(planet.gas);

            if (hasFuel() && launchTicks <= -20) {
                hbm$syncSet$GasDockBlockEntity$hasRocket(false);
                collectGas();
            } else if (launchTicks >= 100) {
                hbm$syncSet$GasDockBlockEntity$hasRocket(true);
            }
        }

        networkPackNT(150);
    }

    @Override
    public void tickClient() {
        stepLaunch();
        CLIENT_EFFECTS.accept(this);
    }

    private void stepLaunch() {
        launchTicks = Mth.clamp(launchTicks + (hasRocket ? -1 : 1), hasRocket ? -20 : 0, 100);
    }

    private void collectGas() {
        if (tanks[1].getFill() < 500) return;
        if (tanks[2].getFill() < 500) return;
        if (tanks[0].getFill() + 8000 > tanks[0].getMaxFill()) return;

        tanks[1].setFill(tanks[1].getFill() - 500);
        tanks[2].setFill(tanks[2].getFill() - 500);
        tanks[0].setFill(tanks[0].getFill() + 8000);
    }

    private boolean hasFuel() {
        return tanks[1].getFill() >= 500 && tanks[2].getFill() >= 500;
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
    public FluidTankNTM[] getSendingTanks() {
        return sending;
    }

    @Override
    public void declareFlush(FlushLanes out) {
        for (FluidTankNTM tank : getSendingTanks()) out.add(tank, FlushFaces.own());
    }

    @Override
    public FluidTankNTM[] getReceivingTanks() {
        return receiving;
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        hbm$syncSet$GasDockBlockEntity$hasRocket(input.getBooleanOr("hasRocker", true));
        input.child("gas").ifPresent(tanks[0]::deserialize);
        input.child("f1").ifPresent(tanks[1]::deserialize);
        input.child("f2").ifPresent(tanks[2]::deserialize);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putBoolean("hasRocker", hasRocket);
        tanks[0].serialize(output.child("gas"));
        tanks[1].serialize(output.child("f1"));
        tanks[2].serialize(output.child("f2"));
    }

    @Override
    public long syncUnitMask() {
        return super.syncUnitMask() | ROCKET_UNIT | TANKS_UNIT;
    }

    @Override
    public void writeSyncUnit(int unit, ByteBuf output) {
        switch (unit) {
            case 0 -> output.writeBoolean(hasRocket);
            case 1 -> {
                for (FluidTankNTM tank : tanks) tank.packetSerialize(output);
            }
            default -> super.writeSyncUnit(unit, output);
        }
    }

    @Override
    public void readSyncUnit(int unit, ByteBuf input) {
        switch (unit) {
            case 0 -> hbm$syncSet$GasDockBlockEntity$hasRocket(input.readBoolean());
            case 1 -> {
                for (FluidTankNTM tank : tanks) tank.packetDeserialize(input);
            }
            default -> super.readSyncUnit(unit, input);
        }
    }

    public final FluidTankNTM hbm$syncArraySet$GasDockBlockEntity$tanks(
            FluidTankNTM[] array, int index, FluidTankNTM next) {
        if (array[index] != next) {
            if (array == this.tanks && syncBound())
                com.hbm.packet.SyncBindings.unbind(this, array[index]);
            array[index] = next;
            if (array == this.tanks) {
                syncArrayChanged(array, index, 3, TANKS_UNIT);
            }
        }
        return next;
    }

    public final void hbm$syncSet$GasDockBlockEntity$hasRocket(boolean next) {
        if (this.hasRocket != next) {
            this.hasRocket = next;
            if ((ROCKET_UNIT) == 0L) syncChanged(3);
            else syncUnitsChanged(3, ROCKET_UNIT);
        }
    }

    static {
        com.hbm.packet.SyncBindings.field(GasDockBlockEntity.class, "tanks", 3, TANKS_UNIT);
    }
}
