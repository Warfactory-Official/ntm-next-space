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
import com.hbm.blockentity.PersistentDrop;
import com.hbm.inventory.fluid.FluidStackNTM;
import com.hbm.inventory.fluid.NTMFluids;
import com.hbm.inventory.fluid.tank.FluidTankAccess;
import com.hbm.inventory.fluid.tank.FluidTankNTM;
import com.hbm.items.ModDataComponents;
import com.hbm.packet.SyncUnitSchema;
import com.hbmspace.blocks.IndustryBlockEntitiesSpace;
import com.hbmspace.inventory.container.CryoDistillMenu;
import com.hbmspace.inventory.fluid.FluidsSpace;
import com.hbmspace.inventory.recipes.CryoRecipe;
import com.hbmspace.inventory.recipes.CryoRecipes;
import io.netty.buffer.ByteBuf;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class CryoDistillBlockEntity extends MachineBlockEntity
        implements FluidTankAccess,
                EnergyHandler,
                FluidTankHandler,
                MenuProvider,
                PersistentDrop,
                SyncUnitSchema {

    public static final int SLOT_BATTERY = 0;
    public static final int SLOT_FLUID_ID = 7;
    public static final int[][] SLOT_UNLOAD = {{1, 2}, {3, 4}, {5, 6}, {8, 9}};
    public static final int SLOT_COUNT = 10;

    public static final long MAX_POWER = 1_000_000L;
    public static final long CONSUMPTION = 20_000L;
    private static final String[] TANK_KEYS = {"input", "o1", "o2", "o3", "o4"};

    public final FluidTankNTM[] tanks = new FluidTankNTM[5];
    private final FluidTankNTM[] receiving;
    private final FluidTankNTM[] sending;
    public long power;

    private final FluidFlushOutputs flush = new FluidFlushOutputs();

    public CryoDistillBlockEntity(BlockPos pos, BlockState state) {
        super(IndustryBlockEntitiesSpace.CRYO_DISTILL.get(), pos, state, SLOT_COUNT);
        hbm$syncArraySet$CryoDistillBlockEntity$tanks(
                tanks, 0, new FluidTankNTM(FluidsSpace.earthair(), 64_000));
        hbm$syncArraySet$CryoDistillBlockEntity$tanks(
                tanks, 1, new FluidTankNTM(FluidsSpace.nitrogen(), 24_000));
        hbm$syncArraySet$CryoDistillBlockEntity$tanks(
                tanks, 2, new FluidTankNTM(NTMFluids.OXYGEN, 24_000));
        hbm$syncArraySet$CryoDistillBlockEntity$tanks(
                tanks, 3, new FluidTankNTM(FluidsSpace.krypton(), 24_000));
        hbm$syncArraySet$CryoDistillBlockEntity$tanks(
                tanks, 4, new FluidTankNTM(NTMFluids.CARBONDIOXIDE, 24_000));
        receiving = new FluidTankNTM[] {tanks[0]};
        sending = new FluidTankNTM[] {tanks[1], tanks[2], tanks[3], tanks[4]};
    }

    @Override
    public long getPower() {
        return power;
    }

    @Override
    public void setPower(long p) {
        hbm$syncSet$CryoDistillBlockEntity$power(Math.max(0L, Math.min(p, MAX_POWER)));
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
        networkPackNT(15);
        long prevPower = power;

        hbm$syncSet$CryoDistillBlockEntity$power(
                (long)
                        (power
                                + (ItemEnergyTransfer.extract(
                                        this, SLOT_BATTERY, MAX_POWER - power, false))));
        boolean changed = tanks[0].setType(SLOT_FLUID_ID, SLOT_FLUID_ID, inventory);
        changed |= distill();
        for (int i = 0; i < SLOT_UNLOAD.length; i++) {
            changed |= tanks[i + 1].unloadTank(SLOT_UNLOAD[i][0], SLOT_UNLOAD[i][1], inventory);
        }
        if (changed || power != prevPower) setChanged();

        flush.provide((ServerLevel) level, this);
    }

    private boolean distill() {
        CryoRecipe recipe = CryoRecipes.INSTANCE.getOutput(tanks[0].getTankType());
        if (recipe == null) {
            for (int i = 1; i < tanks.length; i++) tanks[i].setTankType(null);
            return false;
        }

        FluidStackNTM[] out = recipe.outputFluid;
        for (int i = 1; i < tanks.length; i++) tanks[i].setTankType(out[i - 1].type());

        if (power < CONSUMPTION) return false;
        if (tanks[0].getFill() < CryoRecipes.FEED) return false;
        for (int i = 1; i < tanks.length; i++) {
            if (tanks[i].getFill() + out[i - 1].amount() > tanks[i].getMaxFill()) return false;
        }

        tanks[0].setFill(tanks[0].getFill() - CryoRecipes.FEED);
        for (int i = 1; i < tanks.length; i++)
            tanks[i].setFill(tanks[i].getFill() + out[i - 1].amount());
        hbm$syncSet$CryoDistillBlockEntity$power((long) (power - (CONSUMPTION)));
        return true;
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("container.cryoDistillator");
    }

    @Override
    public Component getDisplayName() {
        return getName();
    }

    @Override
    public AbstractContainerMenu createMenu(
            int containerId, Inventory playerInventory, Player player) {
        return new CryoDistillMenu(containerId, playerInventory, this);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        input.getLong("power").ifPresent(v -> hbm$syncSet$CryoDistillBlockEntity$power(v));
        for (int i = 0; i < tanks.length; i++)
            input.child(TANK_KEYS[i]).ifPresent(tanks[i]::deserialize);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putLong("power", power);
        for (int i = 0; i < tanks.length; i++) tanks[i].serialize(output.child(TANK_KEYS[i]));
    }

    @Override
    public void writePersistent(DataComponentMap.Builder components) {
        for (FluidTankNTM tank : tanks) {
            if (tank.getFill() > 0) {
                components.set(
                        ModDataComponents.TANK_CONTENTS.get(), FluidStackNTM.snapshot(tanks));
                return;
            }
        }
    }

    @Override
    public void readPersistent(DataComponentGetter components) {
        List<FluidStackNTM> contents = components.get(ModDataComponents.TANK_CONTENTS.get());
        if (contents != null) FluidStackNTM.restore(contents, tanks);
    }

    @Override
    public String[] persistentKeys() {
        return TANK_KEYS;
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
            case 0 -> hbm$syncSet$CryoDistillBlockEntity$power(input.readLong());
            case 1 -> {
                for (FluidTankNTM tank : tanks) tank.packetDeserialize(input);
            }
            default -> throw new IllegalArgumentException();
        }
    }

    public final FluidTankNTM hbm$syncArraySet$CryoDistillBlockEntity$tanks(
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

    public final void hbm$syncSet$CryoDistillBlockEntity$power(long next) {
        if (this.power != next) {
            this.power = next;
            if ((1L << 0) == 0L) syncChanged(3);
            else syncUnitsChanged(3, 1L << 0);
        }
    }

    static {
        com.hbm.packet.SyncBindings.field(CryoDistillBlockEntity.class, "tanks", 3, 1L << 1);
        com.hbm.inventory.container.SyncedData.fields(CryoDistillBlockEntity.class, "power");
    }

    public void stripPersistent(ValueOutput output) {
        for (String key : persistentKeys()) output.discard(key);
    }

    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder components) {
        super.collectImplicitComponents(components);
        writePersistent(components);
    }

    @Override
    protected void applyImplicitComponents(DataComponentGetter components) {
        super.applyImplicitComponents(components);
        readPersistent(components);
    }

    @Override
    public void removeComponentsFromTag(ValueOutput output) {
        super.removeComponentsFromTag(output);
        stripPersistent(output);
    }
}
