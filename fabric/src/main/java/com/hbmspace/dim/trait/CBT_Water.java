/*
 * SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
 * SPDX-FileCopyrightText: 2024 George Paton
 * SPDX-License-Identifier: LGPL-3.0-only
 *
 * Copyright (C) 2026 movblock. All rights reserved.
 * Copyright (C) 2024 George Paton. All rights reserved.
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
