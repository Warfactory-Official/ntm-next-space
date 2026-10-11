// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-FileCopyrightText: 2025 George Paton
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.dim.trait;

import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.CompoundTag;

public class CBT_Impact extends CelestialBodyTrait {

    public long time;

    public CBT_Impact() {}

    public CBT_Impact(long time) {
        this.time = time;
    }

    @Override
    public void writeToNBT(CompoundTag nbt) {
        nbt.putLong("time", time);
    }

    @Override
    public void readFromNBT(CompoundTag nbt) {
        time = nbt.getLongOr("time", 0);
    }

    @Override
    public void writeToBytes(ByteBuf buf) {
        buf.writeLong(time);
    }

    @Override
    public void readFromBytes(ByteBuf buf) {
        time = buf.readLong();
    }
}
