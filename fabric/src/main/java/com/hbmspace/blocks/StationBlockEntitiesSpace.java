// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.blocks;

import com.hbm.blockentity.BlockEntityMachineBase;
import com.hbm.registration.Reg;
import com.hbmspace.HbmSpace;
import com.hbmspace.blockentity.machine.BlockEntityMachineGasDock;
import com.hbmspace.blockentity.machine.BlockEntityMachineHTR3;
import com.hbmspace.blockentity.machine.BlockEntityMachineHTRF4;
import com.hbmspace.blockentity.machine.BlockEntityMachineHTRNeo;
import com.hbmspace.blockentity.machine.BlockEntityMachineSolarPanel;
import com.hbmspace.blockentity.machine.BlockEntityOrbitalStation;
import com.hbmspace.blockentity.machine.BlockEntityOrbitalStationComputer;
import com.hbmspace.blockentity.machine.BlockEntityOrbitalStationLauncher;
import com.hbmspace.blockentity.machine.BlockEntityRadiator;
import com.hbmspace.blockentity.machine.BlockEntityStationPropulsionCreative;
import com.hbmspace.blockentity.machine.BlockEntityTransporterRocket;
import com.hbmspace.blockentity.machine.BlockEntityXenonThruster;
import java.util.Set;
import net.minecraft.world.level.block.entity.BlockEntityType;

public final class StationBlockEntitiesSpace {

    public static final Reg.BeHandle<BlockEntityOrbitalStation> ORBITAL_STATION =
            HbmSpace.REG
                    .be(
                            "orbital_station",
                            () ->
                                    new BlockEntityType<>(
                                            BlockEntityOrbitalStation::new,
                                            Set.of(
                                                    StationBlocksSpace.ORBITAL_STATION.get(),
                                                    StationBlocksSpace.ORBITAL_STATION_PORT.get())))
                    .ticks(BlockEntityMachineBase::tickClient, BlockEntityMachineBase::tickServer);

    public static final Reg.BeHandle<BlockEntityOrbitalStationLauncher> ORBITAL_STATION_LAUNCHER =
            HbmSpace.REG
                    .be(
                            "orbital_station_launcher",
                            () ->
                                    new BlockEntityType<>(
                                            BlockEntityOrbitalStationLauncher::new,
                                            Set.of(
                                                    StationBlocksSpace.ORBITAL_STATION_LAUNCHER
                                                            .get())))
                    .ticks(BlockEntityMachineBase::tickClient, BlockEntityMachineBase::tickServer);

    public static final Reg.BeHandle<BlockEntityOrbitalStationComputer> ORBITAL_STATION_COMPUTER =
            HbmSpace.REG
                    .be(
                            "orbital_station_computer",
                            () ->
                                    new BlockEntityType<>(
                                            BlockEntityOrbitalStationComputer::new,
                                            Set.of(
                                                    StationBlocksSpace.ORBITAL_STATION_COMPUTER
                                                            .get())))
                    .ticksServer(BlockEntityMachineBase::tickServer);

    public static final Reg.BeHandle<BlockEntityStationPropulsionCreative> PROPULSION_CREATIVE =
            HbmSpace.REG
                    .be(
                            "propulsion_creative",
                            () ->
                                    new BlockEntityType<>(
                                            BlockEntityStationPropulsionCreative::new,
                                            Set.of(StationBlocksSpace.PROPULSION_CREATIVE.get())))
                    .ticksServer(BlockEntityStationPropulsionCreative::tickServer);

    public static final Reg.BeHandle<BlockEntityMachineHTR3> HTR3 =
            HbmSpace.REG
                    .be(
                            "machine_htr3",
                            () ->
                                    new BlockEntityType<>(
                                            BlockEntityMachineHTR3::new,
                                            Set.of(StationBlocksSpace.MACHINE_HTR3.get())))
                    .ticks(BlockEntityMachineBase::tickClient, BlockEntityMachineBase::tickServer);

    public static final Reg.BeHandle<BlockEntityMachineHTRF4> HTRF4 =
            HbmSpace.REG
                    .be(
                            "machine_htrf4",
                            () ->
                                    new BlockEntityType<>(
                                            BlockEntityMachineHTRF4::new,
                                            Set.of(StationBlocksSpace.MACHINE_HTRF4.get())))
                    .ticks(BlockEntityMachineBase::tickClient, BlockEntityMachineBase::tickServer);

    public static final Reg.BeHandle<BlockEntityMachineHTRNeo> HTRF4_NEO =
            HbmSpace.REG
                    .be(
                            "machine_htrneo",
                            () ->
                                    new BlockEntityType<>(
                                            BlockEntityMachineHTRNeo::new,
                                            Set.of(StationBlocksSpace.MACHINE_HTRF4NEO.get())))
                    .ticks(BlockEntityMachineBase::tickClient, BlockEntityMachineBase::tickServer);

    public static final Reg.BeHandle<BlockEntityXenonThruster> XENON_THRUSTER =
            HbmSpace.REG
                    .be(
                            "xenon_thruster",
                            () ->
                                    new BlockEntityType<>(
                                            BlockEntityXenonThruster::new,
                                            Set.of(
                                                    StationBlocksSpace.MACHINE_XENON_THRUSTER
                                                            .get())))
                    .ticks(BlockEntityMachineBase::tickClient, BlockEntityMachineBase::tickServer);

    public static final Reg.BeHandle<BlockEntityTransporterRocket> TRANSPORTER_ROCKET =
            HbmSpace.REG
                    .be(
                            "transporter_rocket",
                            () ->
                                    new BlockEntityType<>(
                                            BlockEntityTransporterRocket::new,
                                            Set.of(StationBlocksSpace.TRANSPORTER_ROCKET.get())))
                    .ticks(BlockEntityMachineBase::tickClient, BlockEntityMachineBase::tickServer);

    public static final Reg.BeHandle<BlockEntityMachineGasDock> GAS_DOCK =
            HbmSpace.REG
                    .be(
                            "gas_dock",
                            () ->
                                    new BlockEntityType<>(
                                            BlockEntityMachineGasDock::new,
                                            Set.of(StationBlocksSpace.GAS_DOCK.get())))
                    .ticks(BlockEntityMachineBase::tickClient, BlockEntityMachineBase::tickServer);

    public static final Reg.BeHandle<BlockEntityMachineSolarPanel> SOLAR_PANEL =
            HbmSpace.REG
                    .be(
                            "machine_solar",
                            () ->
                                    new BlockEntityType<>(
                                            BlockEntityMachineSolarPanel::new,
                                            Set.of(StationBlocksSpace.MACHINE_SOLAR.get())))
                    .ticksServer(BlockEntityMachineBase::tickServer);

    public static final Reg.BeHandle<BlockEntityRadiator> RADIATOR =
            HbmSpace.REG
                    .be(
                            "machine_radiator",
                            () ->
                                    new BlockEntityType<>(
                                            BlockEntityRadiator::new,
                                            Set.of(StationBlocksSpace.MACHINE_RADIATOR.get())))
                    .ticksServer(BlockEntityMachineBase::tickServer);

    private StationBlockEntitiesSpace() {}
}
