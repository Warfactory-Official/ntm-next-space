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

import com.hbm.blockentity.MachineBlockEntity;
import com.hbm.packet.SyncUnitSchema;
import com.hbm.util.TickPhase;
import com.hbmspace.blocks.AtmosphereBlockEntitiesSpace;
import com.hbmspace.handler.atmosphere.GasExchange;
import com.hbmspace.inventory.fluid.FluidsSpace;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class AtmosphereEditorBlockEntity extends MachineBlockEntity implements SyncUnitSchema {
    public boolean isOn;
    public int throughputFactor;

    {
        hbm$syncSet$AtmosphereEditorBlockEntity$throughputFactor(10);
    }

    public Fluid fluid;

    {
        hbm$syncSet$AtmosphereEditorBlockEntity$fluid(FluidsSpace.earthair());
    }

    public boolean isEmitting;

    {
        hbm$syncSet$AtmosphereEditorBlockEntity$isEmitting(true);
    }

    public AtmosphereEditorBlockEntity(BlockPos pos, BlockState state) {
        super(AtmosphereBlockEntitiesSpace.ATMOSPHERE_EDITOR.get(), pos, state, 0);
    }

    @Override
    public void tickServer() {
        if (isOn && TickPhase.every(this, 5)) {
            double mB = Math.pow(10, throughputFactor);
            if (isEmitting) {
                GasExchange.release((ServerLevel) level, fluid, mB);
            } else {
                GasExchange.capture((ServerLevel) level, fluid, mB);
            }
        }

        networkPackNT(25);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        hbm$syncSet$AtmosphereEditorBlockEntity$isOn(input.getBooleanOr("isOn", false));
        hbm$syncSet$AtmosphereEditorBlockEntity$throughputFactor(input.getIntOr("throughput", 10));
        input.getString("fluid")
                .map(Identifier::tryParse)
                .flatMap(BuiltInRegistries.FLUID::getOptional)
                .ifPresent(f -> hbm$syncSet$AtmosphereEditorBlockEntity$fluid(f));
        hbm$syncSet$AtmosphereEditorBlockEntity$isEmitting(input.getBooleanOr("emit", true));
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putBoolean("isOn", isOn);
        output.putInt("throughput", throughputFactor);
        output.putString("fluid", BuiltInRegistries.FLUID.getKey(fluid).toString());
        output.putBoolean("emit", isEmitting);
    }

    @Override
    public long syncUnitMask() {
        return 0x1L;
    }

    @Override
    public void writeSyncUnit(int unit, ByteBuf output) {
        if (unit != 0) throw new IllegalArgumentException();
        output.writeBoolean(isOn);
        output.writeInt(throughputFactor);
        output.writeInt(BuiltInRegistries.FLUID.getId(fluid));
        output.writeBoolean(isEmitting);
    }

    @Override
    public void readSyncUnit(int unit, ByteBuf input) {
        if (unit != 0) throw new IllegalArgumentException();
        hbm$syncSet$AtmosphereEditorBlockEntity$isOn(input.readBoolean());
        hbm$syncSet$AtmosphereEditorBlockEntity$throughputFactor(input.readInt());
        hbm$syncSet$AtmosphereEditorBlockEntity$fluid(
                BuiltInRegistries.FLUID.byId(input.readInt()));
        hbm$syncSet$AtmosphereEditorBlockEntity$isEmitting(input.readBoolean());
    }

    public final void hbm$syncSet$AtmosphereEditorBlockEntity$isOn(boolean next) {
        if (this.isOn != next) {
            this.isOn = next;
            if ((1L << 0) == 0L) syncChanged(3);
            else syncUnitsChanged(3, 1L << 0);
        }
    }

    public final void hbm$syncSet$AtmosphereEditorBlockEntity$throughputFactor(int next) {
        if (this.throughputFactor != next) {
            this.throughputFactor = next;
            if ((1L << 0) == 0L) syncChanged(3);
            else syncUnitsChanged(3, 1L << 0);
        }
    }

    public final void hbm$syncSet$AtmosphereEditorBlockEntity$fluid(Fluid next) {
        if (this.fluid != next) {
            boolean bound = syncBound();
            if (bound) com.hbm.packet.SyncBindings.unbind(this, this.fluid);
            this.fluid = next;
            if (bound) bindSyncValue(next, 3, 1L << 0);
            if ((1L << 0) == 0L) syncChanged(3);
            else syncUnitsChanged(3, 1L << 0);
        }
    }

    public final void hbm$syncSet$AtmosphereEditorBlockEntity$isEmitting(boolean next) {
        if (this.isEmitting != next) {
            this.isEmitting = next;
            if ((1L << 0) == 0L) syncChanged(3);
            else syncUnitsChanged(3, 1L << 0);
        }
    }

    static {
        com.hbm.packet.SyncBindings.field(AtmosphereEditorBlockEntity.class, "fluid", 3, 1L << 0);
    }
}
