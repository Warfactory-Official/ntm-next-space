// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.items;

import com.hbm.registration.NeoForgeRegistrar;
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
    public static final RegistryHandle<DataComponentType<ItemVOTVdrive.Data>> DRIVE =
            NeoForgeRegistrar.registerDataComponent(
                    HbmSpace.MOD_ID + ":drive",
                    DataComponentType.<ItemVOTVdrive.Data>builder()
                            .persistent(ItemVOTVdrive.Data.CODEC)
                            .networkSynchronized(ItemVOTVdrive.Data.STREAM_CODEC)
                            .build());
    public static final RegistryHandle<DataComponentType<RocketStruct>> ROCKET =
            NeoForgeRegistrar.registerDataComponent(
                    HbmSpace.MOD_ID + ":rocket",
                    DataComponentType.<RocketStruct>builder()
                            .persistent(RocketStruct.CODEC)
                            .networkSynchronized(RocketStruct.STREAM_CODEC)
                            .build());
    public static final RegistryHandle<DataComponentType<Unit>> ROCKET_FUELED =
            NeoForgeRegistrar.registerDataComponent(
                    HbmSpace.MOD_ID + ":rocket_fueled",
                    DataComponentType.<Unit>builder()
                            .persistent(Unit.CODEC)
                            .networkSynchronized(StreamCodec.unit(Unit.INSTANCE))
                            .build());
    public static final RegistryHandle<DataComponentType<SatelliteOrbit>> SATELLITE_ORBIT =
            NeoForgeRegistrar.registerDataComponent(
                    HbmSpace.MOD_ID + ":satellite_orbit",
                    DataComponentType.<SatelliteOrbit>builder()
                            .persistent(SatelliteOrbit.CODEC)
                            .networkSynchronized(SatelliteOrbit.STREAM_CODEC)
                            .build());
    public static final RegistryHandle<DataComponentType<Fluid>> INCOMPATIBLE_TABLE =
            NeoForgeRegistrar.registerDataComponent(
                    HbmSpace.MOD_ID + ":incompatible_table",
                    DataComponentType.<Fluid>builder()
                            .persistent(BuiltInRegistries.FLUID.byNameCodec())
                            .networkSynchronized(ByteBufCodecs.registry(Registries.FLUID))
                            .build());

    private ModDataComponentsSpace() {}
}
