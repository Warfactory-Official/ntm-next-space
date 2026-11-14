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

import com.hbm.inventory.container.HeldItemMenu;
import com.hbm.registration.Registrar;
import com.hbm.registration.RegistryHandle;
import com.hbmspace.HbmSpace;
import com.hbmspace.blockentity.bomb.LaunchPadRocketBlockEntity;
import com.hbmspace.blockentity.machine.DriveProcessorBlockEntity;
import com.hbmspace.blockentity.machine.RocketAssemblyBlockEntity;
import com.hbmspace.blockentity.machine.StardarBlockEntity;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.inventory.MenuType;

public final class RocketryMenusSpace {

    public static final RegistryHandle<MenuType<RocketAssemblyMenu>> MACHINE_ROCKET_ASSEMBLY =
            Registrar.registerBlockEntityMenu(
                    HbmSpace.MOD_ID + ":machine_rocket_assembly", RocketAssemblyBlockEntity.class);

    public static final RegistryHandle<MenuType<LaunchPadRocketMenu>> LAUNCH_PAD_ROCKET =
            Registrar.registerBlockEntityMenu(
                    HbmSpace.MOD_ID + ":launch_pad_rocket", LaunchPadRocketBlockEntity.class);

    public static final RegistryHandle<MenuType<DriveProcessorMenu>> MACHINE_DRIVE_PROCESSOR =
            Registrar.registerBlockEntityMenu(
                    HbmSpace.MOD_ID + ":machine_drive_processor", DriveProcessorBlockEntity.class);

    public static final RegistryHandle<MenuType<StardarMenu>> MACHINE_STARDAR =
            Registrar.registerBlockEntityMenu(
                    HbmSpace.MOD_ID + ":machine_stardar", StardarBlockEntity.class);

    public static final RegistryHandle<MenuType<HeldItemMenu>> SATELLITE_SETTINGS =
            Registrar.registerDataMenu(
                    HbmSpace.MOD_ID + ":satellite_settings",
                    (containerId, inventory, hand) ->
                            new HeldItemMenu(
                                    RocketryMenusSpace.SATELLITE_SETTINGS.get(),
                                    containerId,
                                    inventory,
                                    hand),
                    InteractionHand.STREAM_CODEC);

    private RocketryMenusSpace() {}
}
