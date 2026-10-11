// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.inventory.container;

import com.hbm.registration.NeoForgeRegistrar;
import com.hbm.registration.RegistryHandle;
import com.hbmspace.HbmSpace;
import com.hbmspace.blockentity.machine.BlockEntityOrbitalStationComputer;
import com.hbmspace.blockentity.machine.BlockEntityOrbitalStationLauncher;
import com.hbmspace.blockentity.machine.BlockEntityTransporterRocket;
import net.minecraft.world.inventory.MenuType;

public final class StationMenusSpace {

    public static final RegistryHandle<MenuType<MenuOrbitalStationComputer>>
            ORBITAL_STATION_COMPUTER =
                    NeoForgeRegistrar.registerBlockEntityMenu(
                            HbmSpace.MOD_ID + ":orbital_station_computer",
                            BlockEntityOrbitalStationComputer.class);

    public static final RegistryHandle<MenuType<MenuOrbitalStationLauncher>>
            ORBITAL_STATION_LAUNCHER =
                    NeoForgeRegistrar.registerBlockEntityMenu(
                            HbmSpace.MOD_ID + ":orbital_station_launcher",
                            BlockEntityOrbitalStationLauncher.class);

    public static final RegistryHandle<MenuType<MenuTransporterRocket>> TRANSPORTER_ROCKET =
            NeoForgeRegistrar.registerBlockEntityMenu(
                    HbmSpace.MOD_ID + ":transporter_rocket", BlockEntityTransporterRocket.class);

    private StationMenusSpace() {}
}
