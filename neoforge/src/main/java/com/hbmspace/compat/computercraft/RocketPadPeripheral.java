// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.compat.computercraft;

import com.hbm.compat.computercraft.SnapshotPeripheral;
import com.hbm.inventory.fluid.NTMFluids;
import com.hbm.inventory.fluid.tank.FluidTankNTM;
import com.hbmspace.blockentity.bomb.BlockEntityLaunchPadRocket;
import com.hbmspace.dim.Target;
import com.hbmspace.handler.RocketStruct;
import com.hbmspace.items.ItemVOTVdrive;
import com.hbmspace.items.weapon.ItemCustomRocket;
import dan200.computercraft.api.lua.LuaFunction;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;

public final class RocketPadPeripheral extends SnapshotPeripheral<BlockEntityLaunchPadRocket> {

    private static final int POWER = 0;
    private static final int SOLID = 1;
    private static final int SOLID_MAX = 2;
    private static final int FUEL = 0;

    private Fluid[] types = new Fluid[0];
    private int[] fills = new int[0];
    private int[] maxes = new int[0];
    private Object[] fuel = new Object[0];

    public RocketPadPeripheral(BlockEntityLaunchPadRocket pad) {
        super(pad, "ntm_rocket_pad", 3, 1);
    }

    @Override
    protected void capture(BlockEntityLaunchPadRocket pad) {
        put(POWER, pad.power);
        put(SOLID, pad.solidLevel);
        put(SOLID_MAX, pad.solidMax);
        putRef(FUEL, fuel(pad.tanks));
    }

    private Object[] fuel(FluidTankNTM[] tanks) {
        List<FluidTankNTM> typed = new ArrayList<>(tanks.length);
        for (FluidTankNTM tank : tanks) {
            if (tank.getTankType() != null) typed.add(tank);
        }
        if (unchanged(typed)) return fuel;

        int n = typed.size();
        types = new Fluid[n];
        fills = new int[n];
        maxes = new int[n];
        Object[] out = new Object[n];
        for (int i = 0; i < n; i++) {
            FluidTankNTM tank = typed.get(i);
            types[i] = tank.getTankType();
            fills[i] = tank.getFill();
            maxes[i] = tank.getMaxFill();
            out[i] = new Object[] {fills[i], maxes[i], NTMFluids.legacyName(types[i])};
        }
        return fuel = out;
    }

    private boolean unchanged(List<FluidTankNTM> typed) {
        if (typed.size() != types.length) return false;
        for (int i = 0; i < types.length; i++) {
            FluidTankNTM tank = typed.get(i);
            if (tank.getTankType() != types[i]
                    || tank.getFill() != fills[i]
                    || tank.getMaxFill() != maxes[i]) {
                return false;
            }
        }
        return true;
    }

    @LuaFunction
    public final Object[] getEnergyInfo() {
        return read(s -> new Object[] {s.longAt(POWER), BlockEntityLaunchPadRocket.MAX_POWER});
    }

    @LuaFunction
    public final Object[] getFuel() {
        return read(s -> (Object[]) Objects.requireNonNull(s.refAt(FUEL)));
    }

    @LuaFunction
    public final Object[] getSolidFuel() {
        return read(s -> new Object[] {s.intAt(SOLID), s.intAt(SOLID_MAX)});
    }

    @LuaFunction(mainThread = true)
    public final Object[] canLaunch() {
        return new Object[] {machine().canLaunch()};
    }

    @LuaFunction(mainThread = true)
    public final Object[] getRocketStats() {
        RocketStruct rocket =
                ItemCustomRocket.get(machine().getItem(BlockEntityLaunchPadRocket.SLOT_ROCKET));
        if (rocket == null) return new Object[] {null, ""};
        return new Object[] {rocket.stages.size(), rocket.getLaunchMass(), rocket.getHeight()};
    }

    @LuaFunction(mainThread = true)
    public final Object[] launch() {
        machine().launch(null);
        return new Object[] {};
    }

    @LuaFunction(mainThread = true)
    public final Object[] getDestination() {
        BlockEntityLaunchPadRocket pad = machine();
        ItemStack drive = pad.getItem(BlockEntityLaunchPadRocket.SLOT_DRIVE);
        if (!(drive.getItem() instanceof ItemVOTVdrive))
            return new Object[] {null, "No destination drive."};
        Target target = ItemVOTVdrive.getTarget(drive, pad.getLevel());
        if (target.body() != null)
            return new Object[] {target.body().name.toLowerCase(Locale.ROOT)};
        return new Object[] {null, "Drive has no destination."};
    }
}
