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

package com.hbmspace.compat.computercraft;

import com.hbm.compat.computercraft.SnapshotPeripheral;
import com.hbm.inventory.fluid.NTMFluids;
import com.hbmspace.blockentity.machine.AirPumpBlockEntity;
import com.hbmspace.dim.trait.CBT_Atmosphere.FluidEntry;
import com.hbmspace.dim.trait.CBT_Atmosphere;
import dan200.computercraft.api.lua.LuaFunction;
import java.util.List;
import net.minecraft.world.level.material.Fluid;
import org.jspecify.annotations.Nullable;

public final class AirPumpPeripheral extends SnapshotPeripheral<AirPumpBlockEntity> {

    private static final int FILL = 0;
    private static final int MAX = 1;
    private static final int SEALED = 2;
    private static final int DETAILS = 0;
    private static final Object[] VACUUM = {"VACUUM"};

    private Fluid[] fluids = new Fluid[0];
    private double[] pressures = new double[0];
    private Object[] details = VACUUM;

    public AirPumpPeripheral(AirPumpBlockEntity machine) {
        super(machine, "ntm_atmospheric_vent", 3, 1);
    }

    @Override
    protected void capture(AirPumpBlockEntity machine) {
        put(FILL, machine.tank.getFill());
        put(MAX, machine.tank.getMaxFill());
        put(SEALED, machine.hasSeal());
        putRef(DETAILS, details(machine.currentAtmosphere));
    }

    private Object[] details(@Nullable CBT_Atmosphere atmosphere) {
        List<FluidEntry> entries = atmosphere == null ? null : atmosphere.fluids;
        if (entries == null || entries.isEmpty()) {
            fluids = new Fluid[0];
            pressures = new double[0];
            return details = VACUUM;
        }
        if (unchanged(entries)) return details;

        int n = entries.size();
        fluids = new Fluid[n];
        pressures = new double[n];
        Object[] out = new Object[n];
        for (int i = 0; i < n; i++) {
            FluidEntry entry = entries.get(i);
            fluids[i] = entry.fluid;
            pressures[i] = entry.pressure;
            out[i] = new Object[] {NTMFluids.legacyName(entry.fluid), entry.pressure};
        }
        return details = out;
    }

    private boolean unchanged(List<FluidEntry> entries) {
        if (entries.size() != fluids.length) return false;
        for (int i = 0; i < fluids.length; i++) {
            FluidEntry entry = entries.get(i);
            if (entry.fluid != fluids[i] || entry.pressure != pressures[i]) return false;
        }
        return true;
    }

    @LuaFunction
    public Object[] getAtmosphereDetails() {
        return read(s -> (Object[]) s.refAt(DETAILS));
    }

    @LuaFunction
    public Object[] getFluid() {
        return read(s -> new Object[] {s.intAt(FILL), s.intAt(MAX)});
    }

    @LuaFunction
    public Object[] isSealed() {
        return new Object[] {booleanAt(SEALED)};
    }
}
