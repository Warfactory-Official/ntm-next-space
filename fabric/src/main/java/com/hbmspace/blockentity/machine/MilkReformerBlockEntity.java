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

package com.hbmspace.blockentity.machine;

import com.hbm.api.energymk2.EnergyHandler;
import com.hbm.api.energymk2.ItemEnergyTransfer;
import com.hbm.api.fluidmk2.FluidFlushOutputs;
import com.hbm.api.fluidmk2.FluidTankHandler;
import com.hbm.api.fluidmk2.FlushFaces;
import com.hbm.api.fluidmk2.FlushLanes;
import com.hbm.blockentity.MachineBlockEntity;
import com.hbm.inventory.fluid.tank.FluidTankAccess;
import com.hbm.inventory.fluid.tank.FluidTankNTM;
import com.hbm.packet.SyncUnitSchema;
import com.hbmspace.blocks.ModBlockEntitiesSpace;
import com.hbmspace.inventory.container.MilkReformerMenu;
import com.hbmspace.inventory.fluid.FluidsSpace;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class MilkReformerBlockEntity extends MachineBlockEntity
        implements FluidTankAccess, EnergyHandler, FluidTankHandler, MenuProvider, SyncUnitSchema {

    public static final int SLOT_BATTERY = 0;
    public static final int SLOT_INPUT_IN = 1;
    public static final int SLOT_INPUT_OUT = 2;
    public static final int SLOT_OUTPUT_IN_START = 3;
    public static final int SLOT_OUTPUT_OUT_START = 4;
    public static final int SLOT_COUNT = 9;

    public static final long MAX_POWER = 1_000_000L;
    public static final long CONSUMPTION = 10_000L;
    public static final int INPUT = 100;
    public static final int[] OUTPUTS = {50, 35, 15};

    public final FluidTankNTM[] tanks = new FluidTankNTM[4];
    private final FluidTankNTM[] receiving;
    private final FluidTankNTM[] sending;
    public long power;

    private final FluidFlushOutputs flush = new FluidFlushOutputs();

    public MilkReformerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntitiesSpace.MILK_REFORMER.get(), pos, state, SLOT_COUNT);
        hbm$syncArraySet$MilkReformerBlockEntity$tanks(
                tanks, 0, new FluidTankNTM(FluidsSpace.milk(), 64_000));
        hbm$syncArraySet$MilkReformerBlockEntity$tanks(
                tanks, 1, new FluidTankNTM(FluidsSpace.emilk(), 32_000));
        hbm$syncArraySet$MilkReformerBlockEntity$tanks(
                tanks, 2, new FluidTankNTM(FluidsSpace.cmilk(), 32_000));
        hbm$syncArraySet$MilkReformerBlockEntity$tanks(
                tanks, 3, new FluidTankNTM(FluidsSpace.cream(), 32_000));
        receiving = new FluidTankNTM[] {tanks[0]};
        sending = new FluidTankNTM[] {tanks[1], tanks[2], tanks[3]};
    }

    @Override
    public long getPower() {
        return power;
    }

    @Override
    public void setPower(long p) {
        hbm$syncSet$MilkReformerBlockEntity$power(Math.max(0L, Math.min(p, MAX_POWER)));
    }

    @Override
    public long getMaxPower() {
        return MAX_POWER;
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
    public void tickServer() {
        long prevPower = power;

        hbm$syncSet$MilkReformerBlockEntity$power(
                (long)
                        (power
                                + (ItemEnergyTransfer.extract(
                                        this, SLOT_BATTERY, MAX_POWER - power, false))));
        boolean changed = tanks[0].loadTank(SLOT_INPUT_IN, SLOT_INPUT_OUT, inventory);
        changed |= refine();
        for (int i = 1; i < tanks.length; i++) {
            changed |=
                    tanks[i].unloadTank(
                            SLOT_OUTPUT_IN_START + (i - 1) * 2,
                            SLOT_OUTPUT_OUT_START + (i - 1) * 2,
                            inventory);
        }
        if (changed || power != prevPower) setChanged();

        flush.provide((ServerLevel) level, this);
        networkPackNT(150);
    }

    private boolean refine() {
        if (power < CONSUMPTION) return false;
        if (tanks[0].getFill() < INPUT) return false;
        for (int i = 0; i < OUTPUTS.length; i++) {
            if (tanks[i + 1].getFill() + OUTPUTS[i] > tanks[i + 1].getMaxFill()) return false;
        }

        hbm$syncSet$MilkReformerBlockEntity$power((long) (power - (CONSUMPTION)));
        tanks[0].setFill(tanks[0].getFill() - INPUT);
        for (int i = 0; i < OUTPUTS.length; i++)
            tanks[i + 1].setFill(tanks[i + 1].getFill() + OUTPUTS[i]);
        return true;
    }

    @Override
    public Component getDisplayName() {
        return getName();
    }

    @Override
    public AbstractContainerMenu createMenu(
            int containerId, Inventory playerInventory, Player player) {
        return new MilkReformerMenu(containerId, playerInventory, this);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        input.getLong("power").ifPresent(v -> hbm$syncSet$MilkReformerBlockEntity$power(v));
        input.child("input").ifPresent(tanks[0]::deserialize);
        input.child("m1").ifPresent(tanks[1]::deserialize);
        input.child("m2").ifPresent(tanks[2]::deserialize);
        input.child("m3").ifPresent(tanks[3]::deserialize);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putLong("power", power);
        tanks[0].serialize(output.child("input"));
        tanks[1].serialize(output.child("m1"));
        tanks[2].serialize(output.child("m2"));
        tanks[3].serialize(output.child("m3"));
    }

    @Override
    public long syncUnitMask() {
        return 0x3L;
    }

    @Override
    public void writeSyncUnit(int unit, ByteBuf output) {
        switch (unit) {
            case 0 -> output.writeLong(power);
            case 1 -> {
                for (FluidTankNTM tank : tanks) tank.packetSerialize(output);
            }
            default -> throw new IllegalArgumentException();
        }
    }

    @Override
    public void readSyncUnit(int unit, ByteBuf input) {
        switch (unit) {
            case 0 -> hbm$syncSet$MilkReformerBlockEntity$power(input.readLong());
            case 1 -> {
                for (FluidTankNTM tank : tanks) tank.packetDeserialize(input);
            }
            default -> throw new IllegalArgumentException();
        }
    }

    public final FluidTankNTM hbm$syncArraySet$MilkReformerBlockEntity$tanks(
            FluidTankNTM[] array, int index, FluidTankNTM next) {
        if (array[index] != next) {
            if (array == this.tanks && syncBound())
                com.hbm.packet.SyncBindings.unbind(this, array[index]);
            array[index] = next;
            if (array == this.tanks) {
                syncArrayChanged(array, index, 3, 1L << 1);
            }
        }
        return next;
    }

    public final void hbm$syncSet$MilkReformerBlockEntity$power(long next) {
        if (this.power != next) {
            this.power = next;
            if ((1L << 0) == 0L) syncChanged(3);
            else syncUnitsChanged(3, 1L << 0);
        }
    }

    static {
        com.hbm.packet.SyncBindings.field(MilkReformerBlockEntity.class, "tanks", 3, 1L << 1);
        com.hbm.inventory.container.SyncedData.fields(MilkReformerBlockEntity.class, "power");
    }
}
