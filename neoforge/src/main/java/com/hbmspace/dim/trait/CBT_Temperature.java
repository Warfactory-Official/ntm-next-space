// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-FileCopyrightText: 2024 George Paton
// SPDX-FileCopyrightText: 2024 JamesH_2
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.dim.trait;

import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.CompoundTag;

public class CBT_Temperature extends CelestialBodyTrait {
    public float degrees;

    public CBT_Temperature() {
        this.degrees = 20;
    }

    public CBT_Temperature(float degrees) {
        this.degrees = degrees;
    }

    @Override
    public void writeToNBT(CompoundTag nbt) {
        nbt.putFloat("c", degrees);
    }

    @Override
    public void readFromNBT(CompoundTag nbt) {
        degrees = nbt.getFloatOr("c", 0);
    }

    @Override
    public void writeToBytes(ByteBuf buf) {
        buf.writeFloat(degrees);
    }

    @Override
    public void readFromBytes(ByteBuf buf) {
        degrees = buf.readFloat();
    }
}
