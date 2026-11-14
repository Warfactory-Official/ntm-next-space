/*
 * SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
 * SPDX-FileCopyrightText: 2025 George Paton
 * SPDX-FileCopyrightText: 2025 JamesH_2
 * SPDX-License-Identifier: LGPL-3.0-only
 *
 * Copyright (C) 2026 movblock. All rights reserved.
 * Copyright (C) 2025 George Paton. All rights reserved.
 * Copyright (C) 2025 JamesH_2. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License version 3 only,
 * as published by the Free Software Foundation.
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this code. If not, see <https://www.gnu.org/licenses/>.
 *
 * Under GPLv3 §7(c) and §7(e), these additional terms apply to LGPL-covered
 * material authored by movblock:
 *
 * - the license grants no rights to the name "NTM NEXT: Space";
 * - a modified version must not be named NTM NEXT: Space or presented as
 *   the official release, and must be marked as different from it.
 *
 * Accurately identifying a work as derived from NTM NEXT: Space does not
 * violate these additional terms, provided it is clearly distinguished from
 * the official release and does not imply endorsement. Ports are encouraged
 * to acknowledge NTM NEXT: Space as their upstream project and provide a
 * link to it.
 */

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
