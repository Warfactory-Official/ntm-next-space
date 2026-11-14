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

package com.hbmspace.items;

import com.hbm.registration.Registrar;
import com.hbm.registration.RegistryHandle;
import com.hbmspace.HbmSpace;
import com.hbmspace.handler.RocketStruct;
import com.hbmspace.saveddata.satellites.SatelliteOrbit;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.Unit;
import net.minecraft.world.level.material.Fluid;

public final class ModDataComponentsSpace {
    public static final RegistryHandle<DataComponentType<VOTVdriveItem.Data>> DRIVE =
            Registrar.registerDataComponent(
                    HbmSpace.MOD_ID + ":drive",
                    DataComponentType.<VOTVdriveItem.Data>builder()
                            .persistent(VOTVdriveItem.Data.CODEC)
                            .networkSynchronized(VOTVdriveItem.Data.STREAM_CODEC)
                            .build());
    public static final RegistryHandle<DataComponentType<RocketStruct>> ROCKET =
            Registrar.registerDataComponent(
                    HbmSpace.MOD_ID + ":rocket",
                    DataComponentType.<RocketStruct>builder()
                            .persistent(RocketStruct.CODEC)
                            .networkSynchronized(RocketStruct.STREAM_CODEC)
                            .build());
    public static final RegistryHandle<DataComponentType<Unit>> ROCKET_FUELED =
            Registrar.registerDataComponent(
                    HbmSpace.MOD_ID + ":rocket_fueled",
                    DataComponentType.<Unit>builder()
                            .persistent(Unit.CODEC)
                            .networkSynchronized(StreamCodec.unit(Unit.INSTANCE))
                            .build());
    public static final RegistryHandle<DataComponentType<SatelliteOrbit>> SATELLITE_ORBIT =
            Registrar.registerDataComponent(
                    HbmSpace.MOD_ID + ":satellite_orbit",
                    DataComponentType.<SatelliteOrbit>builder()
                            .persistent(SatelliteOrbit.CODEC)
                            .networkSynchronized(SatelliteOrbit.STREAM_CODEC)
                            .build());
    public static final RegistryHandle<DataComponentType<Fluid>> INCOMPATIBLE_TABLE =
            Registrar.registerDataComponent(
                    HbmSpace.MOD_ID + ":incompatible_table",
                    DataComponentType.<Fluid>builder()
                            .persistent(BuiltInRegistries.FLUID.byNameCodec())
                            .networkSynchronized(ByteBufCodecs.registry(Registries.FLUID))
                            .build());

    private ModDataComponentsSpace() {}
}
