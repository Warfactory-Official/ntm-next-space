// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.inventory.container;

import com.hbm.registration.NeoForgeRegistrar;
import com.hbm.registration.RegistryHandle;
import com.hbmspace.HbmSpace;
import com.hbmspace.blockentity.machine.BlockEntityHydroponic;
import com.hbmspace.blockentity.machine.BlockEntityMachineCryoDistill;
import com.hbmspace.blockentity.machine.BlockEntityMachineVacuumCircuit;
import net.minecraft.world.inventory.MenuType;

public final class IndustryMenusSpace {
    public static final RegistryHandle<MenuType<MenuMachineCryoDistill>> MACHINE_CRYO_DISTILL =
            NeoForgeRegistrar.registerBlockEntityMenu(
                    HbmSpace.MOD_ID + ":machine_cryo_distill", BlockEntityMachineCryoDistill.class);
    public static final RegistryHandle<MenuType<MenuMachineVacuumCircuit>> MACHINE_VACUUM_CIRCUIT =
            NeoForgeRegistrar.registerBlockEntityMenu(
                    HbmSpace.MOD_ID + ":machine_vacuum_circuit",
                    BlockEntityMachineVacuumCircuit.class);
    public static final RegistryHandle<MenuType<MenuHydroponic>> HYDROBAY =
            NeoForgeRegistrar.registerBlockEntityMenu(
                    HbmSpace.MOD_ID + ":hydrobay", BlockEntityHydroponic.class);

    private IndustryMenusSpace() {}
}
