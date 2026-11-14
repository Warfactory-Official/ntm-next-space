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
import com.hbmspace.blocks.TerrainBlockEntitiesSpace;
import com.hbmspace.dim.SpaceDimensions;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class AlgaeFilmBlockEntity extends MachineBlockEntity
        implements FluidTankAccess, FluidTankHandler, SyncUnitSchema {

    private static final long OPERATE_UNIT = 1L << 0;
    private static final long TANKS_UNIT = 1L << 1;

    public boolean canOperate;
    public final FluidTankNTM[] tanks = {
        new FluidTankNTM(NTMFluids.CARBONDIOXIDE, 8_000), new FluidTankNTM(NTMFluids.OXYGEN, 8_000)
    };
    private final FluidTankNTM[] receiving = {tanks[0]};
    private final FluidTankNTM[] sending = {tanks[1]};
    private final FluidFlushOutputs flush = new FluidFlushOutputs();

    public AlgaeFilmBlockEntity(BlockPos pos, BlockState state) {
        super(TerrainBlockEntitiesSpace.ALGAE_FILM.get(), pos, state, 0);
    }

    @Override
    public void onGraphLoad(ServerLevel server) {
        hbm$syncSet$AlgaeFilmBlockEntity$canOperate(server.dimension() == SpaceDimensions.ORBIT);
    }

    @Override
    public void tickServer() {
        if (canOperate
                && level.getRandom().nextBoolean()
                && tanks[0].getFill() > 0
                && tanks[1].getFill() < tanks[1].getMaxFill()) {
            tanks[0].setFill(tanks[0].getFill() - 1);
            tanks[1].setFill(tanks[1].getFill() + 1);
            setChanged();
        }

        flush.provide((ServerLevel) level, this);
        networkPackNT(20);
    }

    @Override
    public FluidTankNTM[] getReceivingTanks() {
        return receiving;
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
        for (int i = 0; i < tanks.length; i++)
            input.child("t" + i).ifPresent(tanks[i]::deserialize);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        for (int i = 0; i < tanks.length; i++) tanks[i].serialize(output.child("t" + i));
    }

    @Override
    public long syncUnitMask() {
        return OPERATE_UNIT | TANKS_UNIT;
    }

    @Override
    public void writeSyncUnit(int unit, ByteBuf output) {
        switch (unit) {
            case 0 -> output.writeBoolean(canOperate);
            case 1 -> {
                for (FluidTankNTM tank : tanks) tank.packetSerialize(output);
            }
            default -> throw new IllegalArgumentException();
        }
    }

    @Override
    public void readSyncUnit(int unit, ByteBuf input) {
        switch (unit) {
            case 0 -> hbm$syncSet$AlgaeFilmBlockEntity$canOperate(input.readBoolean());
            case 1 -> {
                for (FluidTankNTM tank : tanks) tank.packetDeserialize(input);
            }
            default -> throw new IllegalArgumentException();
        }
    }

    public final void hbm$syncSet$AlgaeFilmBlockEntity$canOperate(boolean next) {
        if (this.canOperate != next) {
            this.canOperate = next;
            if ((OPERATE_UNIT) == 0L) syncChanged(3);
            else syncUnitsChanged(3, OPERATE_UNIT);
        }
    }

    public final FluidTankNTM hbm$syncArraySet$AlgaeFilmBlockEntity$tanks(
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

    static {
        com.hbm.packet.SyncBindings.field(AlgaeFilmBlockEntity.class, "tanks", 3, TANKS_UNIT);
    }
}
