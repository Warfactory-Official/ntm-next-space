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

package com.hbmspace.inventory.container;

import com.hbm.registration.Registrar;
import com.hbm.registration.RegistryHandle;
import com.hbmspace.HbmSpace;
import com.hbmspace.blockentity.machine.OrbitalStationComputerBlockEntity;
import com.hbmspace.blockentity.machine.OrbitalStationLauncherBlockEntity;
import com.hbmspace.blockentity.machine.TransporterBaseBlockEntity;
import com.hbmspace.blockentity.machine.TransporterRocketBlockEntity;
import net.minecraft.world.inventory.MenuType;

public final class StationMenusSpace {

    public static final RegistryHandle<MenuType<OrbitalStationComputerMenu>>
            ORBITAL_STATION_COMPUTER =
                    Registrar.registerBlockEntityMenu(
                            HbmSpace.MOD_ID + ":orbital_station_computer",
                            OrbitalStationComputerBlockEntity.class);

    public static final RegistryHandle<MenuType<OrbitalStationLauncherMenu>>
            ORBITAL_STATION_LAUNCHER =
                    Registrar.registerBlockEntityMenu(
                            HbmSpace.MOD_ID + ":orbital_station_launcher",
                            OrbitalStationLauncherBlockEntity.class);

    public static final RegistryHandle<MenuType<TransporterRocketMenu>> TRANSPORTER_ROCKET =
            Registrar.registerBlockEntityMenu(
                    HbmSpace.MOD_ID + ":transporter_rocket", TransporterRocketBlockEntity.class);

    public static final RegistryHandle<MenuType<TransporterLinkerMenu>> TRANSPORTER_LINKER =
            Registrar.registerDataMenu(
                    HbmSpace.MOD_ID + ":transporter_linker",
                    (containerId, inventory, opening) ->
                            new TransporterLinkerMenu(
                                    containerId,
                                    inventory,
                                    (TransporterBaseBlockEntity)
                                            inventory
                                                    .player
                                                    .level()
                                                    .getBlockEntity(opening.from().pos()),
                                    opening),
                    TransporterLinkerMenu.Opening.STREAM_CODEC);

    private StationMenusSpace() {}
}
