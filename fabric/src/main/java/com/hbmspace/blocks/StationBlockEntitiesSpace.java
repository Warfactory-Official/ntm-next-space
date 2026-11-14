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

package com.hbmspace.blocks;

import com.hbm.blockentity.BlockEntityTicks;
import com.hbm.registration.Reg;
import com.hbmspace.HbmSpace;
import com.hbmspace.blockentity.machine.GasDockBlockEntity;
import com.hbmspace.blockentity.machine.HTR3BlockEntity;
import com.hbmspace.blockentity.machine.HTRF4BlockEntity;
import com.hbmspace.blockentity.machine.HTRNeoBlockEntity;
import com.hbmspace.blockentity.machine.OrbitalStationBlockEntity;
import com.hbmspace.blockentity.machine.OrbitalStationComputerBlockEntity;
import com.hbmspace.blockentity.machine.OrbitalStationLauncherBlockEntity;
import com.hbmspace.blockentity.machine.RadiatorBlockEntity;
import com.hbmspace.blockentity.machine.SolarPanelBlockEntity;
import com.hbmspace.blockentity.machine.StationPropulsionCreativeBlockEntity;
import com.hbmspace.blockentity.machine.TransporterRocketBlockEntity;
import com.hbmspace.blockentity.machine.XenonThrusterBlockEntity;
import java.util.Set;
import net.minecraft.world.level.block.entity.BlockEntityType;

public final class StationBlockEntitiesSpace {

    public static final Reg.BeHandle<OrbitalStationBlockEntity> ORBITAL_STATION =
            (HbmSpace.REG.<OrbitalStationBlockEntity>be(
                            "orbital_station",
                            () ->
                                    new BlockEntityType<>(
                                            OrbitalStationBlockEntity::new,
                                            Set.of(
                                                    StationBlocksSpace.ORBITAL_STATION.get(),
                                                    StationBlocksSpace.ORBITAL_STATION_PORT
                                                            .get()))))
                    .ticksServerAt(BlockEntityTicks.SERVER)
                    .ticksClientAt(BlockEntityTicks.CLIENT);

    public static final Reg.BeHandle<OrbitalStationLauncherBlockEntity> ORBITAL_STATION_LAUNCHER =
            (HbmSpace.REG.<OrbitalStationLauncherBlockEntity>be(
                            "orbital_station_launcher",
                            () ->
                                    new BlockEntityType<>(
                                            OrbitalStationLauncherBlockEntity::new,
                                            Set.of(
                                                    StationBlocksSpace.ORBITAL_STATION_LAUNCHER
                                                            .get()))))
                    .ticksServerAt(BlockEntityTicks.SERVER)
                    .ticksClientAt(BlockEntityTicks.CLIENT);

    public static final Reg.BeHandle<OrbitalStationComputerBlockEntity> ORBITAL_STATION_COMPUTER =
            (HbmSpace.REG.<OrbitalStationComputerBlockEntity>be(
                            "orbital_station_computer",
                            () ->
                                    new BlockEntityType<>(
                                            OrbitalStationComputerBlockEntity::new,
                                            Set.of(
                                                    StationBlocksSpace.ORBITAL_STATION_COMPUTER
                                                            .get()))))
                    .ticksServerAt(BlockEntityTicks.SERVER);

    public static final Reg.BeHandle<StationPropulsionCreativeBlockEntity> PROPULSION_CREATIVE =
            (HbmSpace.REG.<StationPropulsionCreativeBlockEntity>be(
                    "propulsion_creative",
                    () ->
                            new BlockEntityType<>(
                                    StationPropulsionCreativeBlockEntity::new,
                                    Set.of(StationBlocksSpace.PROPULSION_CREATIVE.get()))));

    public static final Reg.BeHandle<HTR3BlockEntity> HTR3 =
            (HbmSpace.REG.<HTR3BlockEntity>be(
                            "machine_htr3",
                            () ->
                                    new BlockEntityType<>(
                                            HTR3BlockEntity::new,
                                            Set.of(StationBlocksSpace.MACHINE_HTR3.get()))))
                    .ticksServerAt(BlockEntityTicks.SERVER_WITH_SOUND)
                    .ticksClientAt(BlockEntityTicks.CLIENT);

    public static final Reg.BeHandle<HTRF4BlockEntity> HTRF4 =
            (HbmSpace.REG.<HTRF4BlockEntity>be(
                            "machine_htrf4",
                            () ->
                                    new BlockEntityType<>(
                                            HTRF4BlockEntity::new,
                                            Set.of(StationBlocksSpace.MACHINE_HTRF4.get()))))
                    .ticksServerAt(BlockEntityTicks.SERVER_WITH_SOUND)
                    .ticksClientAt(BlockEntityTicks.CLIENT);

    public static final Reg.BeHandle<HTRNeoBlockEntity> HTRF4_NEO =
            (HbmSpace.REG.<HTRNeoBlockEntity>be(
                            "machine_htrneo",
                            () ->
                                    new BlockEntityType<>(
                                            HTRNeoBlockEntity::new,
                                            Set.of(StationBlocksSpace.MACHINE_HTRF4NEO.get()))))
                    .ticksServerAt(BlockEntityTicks.SERVER_WITH_SOUND)
                    .ticksClientAt(BlockEntityTicks.CLIENT);

    public static final Reg.BeHandle<XenonThrusterBlockEntity> XENON_THRUSTER =
            (HbmSpace.REG.<XenonThrusterBlockEntity>be(
                            "xenon_thruster",
                            () ->
                                    new BlockEntityType<>(
                                            XenonThrusterBlockEntity::new,
                                            Set.of(
                                                    StationBlocksSpace.MACHINE_XENON_THRUSTER
                                                            .get()))))
                    .ticksServerAt(BlockEntityTicks.SERVER)
                    .ticksClientAt(BlockEntityTicks.CLIENT);

    public static final Reg.BeHandle<TransporterRocketBlockEntity> TRANSPORTER_ROCKET =
            (HbmSpace.REG.<TransporterRocketBlockEntity>be(
                            "transporter_rocket",
                            () ->
                                    new BlockEntityType<>(
                                            TransporterRocketBlockEntity::new,
                                            Set.of(StationBlocksSpace.TRANSPORTER_ROCKET.get()))))
                    .ticksServerAt(BlockEntityTicks.SERVER)
                    .ticksClientAt(BlockEntityTicks.CLIENT);

    public static final Reg.BeHandle<GasDockBlockEntity> GAS_DOCK =
            (HbmSpace.REG.<GasDockBlockEntity>be(
                            "gas_dock",
                            () ->
                                    new BlockEntityType<>(
                                            GasDockBlockEntity::new,
                                            Set.of(StationBlocksSpace.GAS_DOCK.get()))))
                    .ticksServerAt(BlockEntityTicks.SERVER)
                    .ticksClientAt(BlockEntityTicks.CLIENT);

    public static final Reg.BeHandle<SolarPanelBlockEntity> SOLAR_PANEL =
            (HbmSpace.REG.<SolarPanelBlockEntity>be(
                            "machine_solar",
                            () ->
                                    new BlockEntityType<>(
                                            SolarPanelBlockEntity::new,
                                            Set.of(StationBlocksSpace.MACHINE_SOLAR.get()))))
                    .ticksServerAt(BlockEntityTicks.SERVER);

    public static final Reg.BeHandle<RadiatorBlockEntity> RADIATOR =
            (HbmSpace.REG.<RadiatorBlockEntity>be(
                            "machine_radiator",
                            () ->
                                    new BlockEntityType<>(
                                            RadiatorBlockEntity::new,
                                            Set.of(StationBlocksSpace.MACHINE_RADIATOR.get()))))
                    .ticksServerAt(BlockEntityTicks.SERVER);

    private StationBlockEntitiesSpace() {}
}
