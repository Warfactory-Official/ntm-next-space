// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.blockentity.machine;

import com.hbm.blockentity.BlockEntityMachineBase;
import com.hbm.packet.SyncField;
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

public class BlockEntityAtmosphereEditor extends BlockEntityMachineBase implements SyncUnitSchema {

    @SyncField(units = 1L << 0)
    public boolean isOn;

    @SyncField(units = 1L << 0)
    public int throughputFactor = 10;

    @SyncField(units = 1L << 0)
    public Fluid fluid = FluidsSpace.earthair();

    @SyncField(units = 1L << 0)
    public boolean isEmitting = true;

    public BlockEntityAtmosphereEditor(BlockPos pos, BlockState state) {
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
        isOn = input.getBooleanOr("isOn", false);
        throughputFactor = input.getIntOr("throughput", 10);
        input.getString("fluid")
                .map(Identifier::tryParse)
                .flatMap(BuiltInRegistries.FLUID::getOptional)
                .ifPresent(f -> fluid = f);
        isEmitting = input.getBooleanOr("emit", true);
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
        isOn = input.readBoolean();
        throughputFactor = input.readInt();
        fluid = BuiltInRegistries.FLUID.byId(input.readInt());
        isEmitting = input.readBoolean();
    }
}
