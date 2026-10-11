// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-FileCopyrightText: 2024 George Paton
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.dim.trait;

import com.hbm.inventory.fluid.NTMFluids;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

public class CBT_Water extends CelestialBodyTrait {

    public Fluid fluid;

    public CBT_Water() {
        fluid = Fluids.WATER;
    }

    public CBT_Water(Fluid fluid) {
        this.fluid = fluid;
    }

    @Override
    public void writeToNBT(CompoundTag nbt) {
        nbt.putString("fluid", BuiltInRegistries.FLUID.getKey(fluid).toString());
    }

    @Override
    public void readFromNBT(CompoundTag nbt) {
        fluid = BuiltInRegistries.FLUID.getValue(Identifier.parse(nbt.getStringOr("fluid", "")));
        if (fluid == NTMFluids.NONE) fluid = Fluids.WATER;
    }

    @Override
    public void writeToBytes(ByteBuf buf) {
        buf.writeInt(BuiltInRegistries.FLUID.getId(fluid));
    }

    @Override
    public void readFromBytes(ByteBuf buf) {
        fluid = BuiltInRegistries.FLUID.byId(buf.readInt());
    }
}
