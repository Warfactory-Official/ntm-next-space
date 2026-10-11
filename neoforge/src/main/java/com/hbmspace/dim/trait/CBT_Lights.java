// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-FileCopyrightText: 2025 George Paton
// SPDX-FileCopyrightText: 2025 JamesH_2
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.dim.trait;

import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;

public class CBT_Lights extends CelestialBodyTrait {

    public int lights;
    public boolean isCivilized;

    public CBT_Lights() {}

    public CBT_Lights(int light) {
        this.lights = light;
    }

    public int getIntensity() {
        if (lights > 10000) return 3;
        if (lights > 2000) return 2;
        if (lights > 400) return 1;
        return 0;
    }

    public void addLight(BlockState state) {
        lights += state.getLightEmission();
    }

    public void removeLight(BlockState state) {
        lights -= state.getLightEmission();
        if (lights < 0) lights = 0;
    }

    @Override
    public void writeToNBT(CompoundTag nbt) {
        nbt.putInt("lights", lights);
        nbt.putBoolean("isCiv", isCivilized);
    }

    @Override
    public void readFromNBT(CompoundTag nbt) {
        lights = nbt.getIntOr("lights", 0);
        isCivilized = nbt.getBooleanOr("isCiv", false);
    }

    @Override
    public void writeToBytes(ByteBuf buf) {
        buf.writeInt(lights);
        buf.writeBoolean(isCivilized);
    }

    @Override
    public void readFromBytes(ByteBuf buf) {
        lights = buf.readInt();
        isCivilized = buf.readBoolean();
    }
}
