// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.inventory.container;

import com.hbm.registration.NeoForgeRegistrar;
import com.hbm.registration.RegistryHandle;
import com.hbmspace.HbmSpace;
import com.hbmspace.blockentity.machine.BlockEntityMachineWarController;
import net.minecraft.world.inventory.MenuType;

public final class WarMenusSpace {

    public static final RegistryHandle<MenuType<MenuMachineWarController>> WAR_CONTROLLER =
            NeoForgeRegistrar.registerBlockEntityMenu(
                    HbmSpace.MOD_ID + ":war_controller", BlockEntityMachineWarController.class);

    private WarMenusSpace() {}
}
