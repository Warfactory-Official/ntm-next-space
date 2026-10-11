// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.compat.computercraft;

import com.hbm.compat.computercraft.SnapshotPeripheral;
import com.hbm.inventory.fluid.NTMFluids;
import com.hbmspace.blockentity.machine.BlockEntityAirPump;
import com.hbmspace.dim.trait.CBT_Atmosphere.FluidEntry;
import com.hbmspace.dim.trait.CBT_Atmosphere;
import dan200.computercraft.api.lua.LuaFunction;
import java.util.List;
import net.minecraft.world.level.material.Fluid;
import org.jspecify.annotations.Nullable;

public final class AirPumpPeripheral extends SnapshotPeripheral<BlockEntityAirPump> {

    private static final int FILL = 0;
    private static final int MAX = 1;
    private static final int SEALED = 2;
    private static final int DETAILS = 0;
    private static final Object[] VACUUM = {"VACUUM"};

    private Fluid[] fluids = new Fluid[0];
    private double[] pressures = new double[0];
    private Object[] details = VACUUM;

    public AirPumpPeripheral(BlockEntityAirPump machine) {
        super(machine, "ntm_atmospheric_vent", 3, 1);
    }

    @Override
    protected void capture(BlockEntityAirPump machine) {
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
    public final Object[] getAtmosphereDetails() {
        return read(s -> (Object[]) s.refAt(DETAILS));
    }

    @LuaFunction
    public final Object[] getFluid() {
        return read(s -> new Object[] {s.intAt(FILL), s.intAt(MAX)});
    }

    @LuaFunction
    public final Object[] isSealed() {
        return read(s -> new Object[] {s.booleanAt(SEALED)});
    }
}
