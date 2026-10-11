// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-FileCopyrightText: 2024 JamesH_2
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.dim.trait;

import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.CompoundTag;

public class CBT_Bees extends CelestialBodyTrait {
    public float bees;

    public CBT_Bees() {}

    public CBT_Bees(float bees) {
        this.bees = bees;
    }

    @Override
    public void writeToNBT(CompoundTag nbt) {
        nbt.putFloat("bees", bees);
    }

    @Override
    public void readFromNBT(CompoundTag nbt) {
        bees = nbt.getFloatOr("bees", 0);
    }

    @Override
    public void writeToBytes(ByteBuf buf) {
        buf.writeFloat(bees);
    }

    @Override
    public void readFromBytes(ByteBuf buf) {
        bees = buf.readFloat();
    }
}
