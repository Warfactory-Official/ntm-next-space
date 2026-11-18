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
import com.hbm.util.TickPhase;
import com.hbmspace.blocks.IndustryBlockEntitiesSpace;
import com.hbmspace.inventory.fluid.FluidsSpace;
import com.hbmspace.inventory.recipes.AlkylationRecipe;
import com.hbmspace.inventory.recipes.AlkylationRecipes;
import io.netty.buffer.ByteBuf;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

public class AlkylationBlockEntity extends MachineBlockEntity
        implements FluidTankAccess,
                EnergyHandler,
                FluidTankHandler,
                PersistentDrop,
                SyncUnitSchema {

    public static final long MAX_POWER = 1_000_000L;
    public static final long CONSUMPTION = 4_000L;
    private static final String[] TANK_KEYS = {"t0", "t1", "t2", "t3"};

    public final FluidTankNTM[] tanks = new FluidTankNTM[4];
    private final FluidTankNTM[] receiving;
    private final FluidTankNTM[] sending;
    public long power;

    private final FluidFlushOutputs flush = new FluidFlushOutputs();

    public AlkylationBlockEntity(BlockPos pos, BlockState state) {
        super(IndustryBlockEntitiesSpace.ALKYLATION.get(), pos, state, 0);
        hbm$syncArraySet$AlkylationBlockEntity$tanks(
                tanks, 0, new FluidTankNTM(FluidsSpace.chloromethane(), 8_000));
        hbm$syncArraySet$AlkylationBlockEntity$tanks(tanks, 1, new FluidTankNTM(null, 4_000));
        hbm$syncArraySet$AlkylationBlockEntity$tanks(
                tanks, 2, new FluidTankNTM(NTMFluids.UNSATURATEDS, 8_000));
        hbm$syncArraySet$AlkylationBlockEntity$tanks(
                tanks, 3, new FluidTankNTM(NTMFluids.CHLORINE, 8_000));
        receiving = new FluidTankNTM[] {tanks[0], tanks[1]};
        sending = new FluidTankNTM[] {tanks[2], tanks[3]};
    }

    @Override
    public long getPower() {
        return power;
    }

    @Override
    public void setPower(long p) {
        hbm$syncSet$AlkylationBlockEntity$power(Math.max(0L, Math.min(p, MAX_POWER)));
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
        networkPackNT(25);
        if (TickPhase.every(this, 2) && alkylate()) setChanged();
        if (TickPhase.every(this, 10)) flush.provide((ServerLevel) level, this);
    }

    private boolean alkylate() {
        AlkylationRecipe recipe = AlkylationRecipes.INSTANCE.getOutput(tanks[0].getTankType());
        if (recipe == null) {
            tanks[2].setTankType(null);
            tanks[3].setTankType(null);
            return false;
        }

        @Nullable FluidStackNTM acid = recipe.inputFluid.length > 1 ? recipe.inputFluid[1] : null;
        FluidStackNTM first = recipe.outputFluid[0];
        FluidStackNTM second = recipe.outputFluid[1];
        tanks[1].setTankType(acid == null ? null : acid.type());
        tanks[2].setTankType(first.type());
        tanks[3].setTankType(second.type());

        long acidFill = acid == null ? 0L : acid.amount();
        if (power < CONSUMPTION) return false;
        if (tanks[0].getFill() < AlkylationRecipes.FEED) return false;
        if (tanks[1].getFill() < acidFill) return false;
        if (tanks[2].getFill() + first.amount() > tanks[2].getMaxFill()) return false;
        if (tanks[3].getFill() + second.amount() > tanks[3].getMaxFill()) return false;

        tanks[0].setFill(tanks[0].getFill() - AlkylationRecipes.FEED);
        tanks[1].setFill(tanks[1].getFill() - acidFill);
        tanks[2].setFill(tanks[2].getFill() + first.amount());
        tanks[3].setFill(tanks[3].getFill() + second.amount());
        hbm$syncSet$AlkylationBlockEntity$power((long) (power - (CONSUMPTION)));
        return true;
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        input.getLong("power").ifPresent(v -> hbm$syncSet$AlkylationBlockEntity$power(v));
        for (int i = 0; i < tanks.length; i++) {
            FluidTankNTM tank = tanks[i];
            input.child(TANK_KEYS[i]).ifPresent(tank::deserialize);
        }
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
            case 0 -> hbm$syncSet$AlkylationBlockEntity$power(input.readLong());
            case 1 -> {
                for (FluidTankNTM tank : tanks) tank.packetDeserialize(input);
            }
            default -> throw new IllegalArgumentException();
        }
    }

    public final FluidTankNTM hbm$syncArraySet$AlkylationBlockEntity$tanks(
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

    public final void hbm$syncSet$AlkylationBlockEntity$power(long next) {
        if (this.power != next) {
            this.power = next;
            if ((1L << 0) == 0L) syncChanged(3);
            else syncUnitsChanged(3, 1L << 0);
        }
    }

    static {
        com.hbm.packet.SyncBindings.field(AlkylationBlockEntity.class, "tanks", 3, 1L << 1);
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
