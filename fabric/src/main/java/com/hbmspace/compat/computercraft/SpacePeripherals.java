// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.compat.computercraft;

import com.hbm.blockentity.machine.storage.BlockEntityMachineCapacitor;
import com.hbm.compat.computercraft.ComputerCraft;
import com.hbmspace.blockentity.machine.BlockEntityAirPump;
import com.hbmspace.blocks.AtmosphereBlocksSpace;
import com.hbmspace.blocks.CapacitorBlocksSpace;

public final class SpacePeripherals {

    private SpacePeripherals() {}

    public static void register() {
        ComputerCraft.addon(
                BlockEntityAirPump.class,
                AirPumpPeripheral::new,
                false,
                AtmosphereBlocksSpace.AIR_VENT);
        ComputerCraft.extend(
                BlockEntityMachineCapacitor.class, CapacitorBlocksSpace.CAPACITOR_COMPLEX);
    }
}
