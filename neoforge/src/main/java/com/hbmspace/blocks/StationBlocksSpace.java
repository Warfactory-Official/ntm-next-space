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

import com.hbm.items.CreativeContents.Tab;
import com.hbm.items.CreativeContents;
import com.hbm.items.DescBlockItem;
import com.hbm.registration.Reg;
import com.hbm.util.RegistryUtil;
import com.hbmspace.HbmSpace;
import com.hbmspace.blocks.machine.GasDockBlock;
import com.hbmspace.blocks.machine.HTR3Block;
import com.hbmspace.blocks.machine.HTRF4Block;
import com.hbmspace.blocks.machine.HTRFNeoBlock;
import com.hbmspace.blocks.machine.OrbitalStationBlock;
import com.hbmspace.blocks.machine.OrbitalStationComputerBlock;
import com.hbmspace.blocks.machine.OrbitalStationLauncherBlock;
import com.hbmspace.blocks.machine.RadiatorBlock;
import com.hbmspace.blocks.machine.SolarBlock;
import com.hbmspace.blocks.machine.StationPropulsionCreativeBlock;
import com.hbmspace.blocks.machine.TransporterRocketBlock;
import com.hbmspace.blocks.machine.XenonThrusterBlock;
import com.hbmspace.client.model.SolarPanelModel;
import java.util.List;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

public final class StationBlocksSpace {
    private static final String[] PORT_GROUPS = {"ArmZN", "ArmZP", "ArmXP", "ArmXN", "Port"};

    private static BlockBehaviour.Properties iron(float hardness, float resistance) {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(hardness, resistance)
                .requiresCorrectToolForDrops()
                .noOcclusion();
    }

    private static <B extends OrbitalStationBlock> Reg.Handle<B> station(
            Reg.Handle<B> block, String texture) {
        return block;
    }

    public static final Reg.Handle<OrbitalStationBlock> ORBITAL_STATION =
            station(
                    HbmSpace.REG.blockItem(
                            "orbital_station",
                            props -> new OrbitalStationBlock(props, true),
                            () -> iron(-1.0F, Float.POSITIVE_INFINITY)),
                    "block/models/machines/docking_port");
    public static final Reg.Handle<OrbitalStationBlock> ORBITAL_STATION_PORT =
            station(
                    HbmSpace.REG.blockItem(
                            "orbital_station_port",
                            props -> new OrbitalStationBlock(props, false),
                            () -> iron(1.0F, 1.0F)),
                    "block/models/machines/docking_port");
    public static final Reg.Handle<OrbitalStationLauncherBlock> ORBITAL_STATION_LAUNCHER =
            station(
                    HbmSpace.REG.blockItem(
                            "orbital_station_launcher",
                            OrbitalStationLauncherBlock::new,
                            () -> iron(1.0F, 1.0F),
                            (block, props) -> new DescBlockItem(block, props, 1)),
                    "block/models/machines/docking_port_launcher");

    public static final Reg.Handle<OrbitalStationComputerBlock> ORBITAL_STATION_COMPUTER =
            HbmSpace.REG.blockItem(
                    "orbital_station_computer",
                    OrbitalStationComputerBlock::new,
                    () -> iron(1.0F, 1.0F));

    public static final Reg.Handle<StationPropulsionCreativeBlock> PROPULSION_CREATIVE =
            HbmSpace.REG.blockItem(
                    "propulsion_creative",
                    StationPropulsionCreativeBlock::new,
                    () ->
                            BlockBehaviour.Properties.of()
                                    .mapColor(MapColor.METAL)
                                    .strength(1.0F)
                                    .requiresCorrectToolForDrops());

    public static final Reg.Handle<HTR3Block> MACHINE_HTR3 =
            HbmSpace.REG.blockItem(
                    "machine_htr3",
                    HTR3Block::new,
                    () -> iron(5.0F, 60.0F),
                    (block, props) -> new DescBlockItem(block, props, 1));

    public static final Reg.Handle<HTRF4Block> MACHINE_HTRF4 =
            HbmSpace.REG.blockItem("machine_htrf4", HTRF4Block::new, () -> iron(5.0F, 60.0F));

    public static final Reg.Handle<HTRFNeoBlock> MACHINE_HTRF4NEO =
            HbmSpace.REG.blockItem(
                    "machine_htrf4neo",
                    HTRFNeoBlock::new,
                    () -> iron(5.0F, 60.0F),
                    (block, props) -> new DescBlockItem(block, props, 1));

    public static final Reg.Handle<XenonThrusterBlock> MACHINE_XENON_THRUSTER =
            HbmSpace.REG.blockItem(
                    "machine_xenon_thruster", XenonThrusterBlock::new, () -> iron(5.0F, 60.0F));

    public static final Reg.Handle<TransporterRocketBlock> TRANSPORTER_ROCKET =
            HbmSpace.REG.blockItem(
                    "transporter_rocket", TransporterRocketBlock::new, () -> iron(1.0F, 1.0F));

    public static final Reg.Handle<GasDockBlock> GAS_DOCK =
            HbmSpace.REG.blockItem("gas_dock", GasDockBlock::new, () -> iron(5.0F, 6.0F));

    public static final Reg.Handle<SolarBlock> MACHINE_SOLAR =
            HbmSpace.REG
                    .blockItem("machine_solar", SolarBlock::new, () -> iron(5.0F, 6.0F))
                    .bakedBy(() -> new SolarPanelModel());

    public static final Reg.Handle<RadiatorBlock> MACHINE_RADIATOR =
            HbmSpace.REG.blockItem("machine_radiator", RadiatorBlock::new, () -> iron(5.0F, 6.0F));

    static {
        for (Reg.Handle<? extends Block> thruster :
                List.of(MACHINE_HTR3, MACHINE_HTRF4, MACHINE_HTRF4NEO, MACHINE_XENON_THRUSTER)) {
            CreativeContents.insertAfter(
                    Tab.MACHINES,
                    RegistryUtil.id("machine_lpw2"),
                    () -> new ItemStack(thruster.get()));
        }
        for (Reg.Handle<? extends Block> station :
                List.of(
                        TRANSPORTER_ROCKET,
                        ORBITAL_STATION_PORT,
                        ORBITAL_STATION_LAUNCHER,
                        ORBITAL_STATION_COMPUTER,
                        PROPULSION_CREATIVE)) {
            CreativeContents.insertBefore(
                    Tab.MACHINES,
                    RegistryUtil.id("drone_patrol"),
                    () -> new ItemStack(station.get()));
        }
        CreativeContents.insertAfter(
                Tab.MACHINES,
                RegistryUtil.id("machine_hephaestus"),
                () -> new ItemStack(MACHINE_SOLAR.get()));
        CreativeContents.insertAfter(
                Tab.MACHINES,
                RegistryUtil.id("machine_condenser_powered"),
                () -> new ItemStack(MACHINE_RADIATOR.get()));
        CreativeContents.insertAfter(
                Tab.MISSILES, RegistryUtil.id("radar_screen"), () -> new ItemStack(GAS_DOCK.get()));
    }

    private StationBlocksSpace() {}
}
