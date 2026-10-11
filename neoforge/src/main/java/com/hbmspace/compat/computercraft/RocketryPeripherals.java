// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.compat.computercraft;

import com.hbm.compat.computercraft.ComputerCraft;
import com.hbmspace.blockentity.bomb.BlockEntityLaunchPadRocket;
import com.hbmspace.blockentity.machine.BlockEntityMachineStardar;
import com.hbmspace.blocks.RocketryBlocksSpace;

public final class RocketryPeripherals {

    private RocketryPeripherals() {}

    public static void register() {
        ComputerCraft.addon(
                BlockEntityLaunchPadRocket.class,
                RocketPadPeripheral::new,
                true,
                RocketryBlocksSpace.LAUNCH_PAD_ROCKET);
        ComputerCraft.addon(
                BlockEntityMachineStardar.class,
                StardarPeripheral::new,
                false,
                RocketryBlocksSpace.MACHINE_STARDAR);
    }
}
