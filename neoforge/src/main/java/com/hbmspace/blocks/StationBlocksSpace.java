// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.blocks;

import com.hbm.items.CreativeContents.Tab;
import com.hbm.items.CreativeContents;
import com.hbm.items.DescBlockItem;
import com.hbm.lib.Library;
import com.hbm.registration.Reg;
import com.hbmspace.HbmSpace;
import com.hbmspace.blocks.machine.BlockOrbitalStation;
import com.hbmspace.blocks.machine.BlockOrbitalStationComputer;
import com.hbmspace.blocks.machine.BlockOrbitalStationLauncher;
import com.hbmspace.blocks.machine.BlockStationPropulsionCreative;
import com.hbmspace.blocks.machine.BlockTransporterRocket;
import com.hbmspace.blocks.machine.MachineGasDock;
import com.hbmspace.blocks.machine.MachineHTR3;
import com.hbmspace.blocks.machine.MachineHTRF4;
import com.hbmspace.blocks.machine.MachineHTRFNeo;
import com.hbmspace.blocks.machine.MachineRadiator;
import com.hbmspace.blocks.machine.MachineSolar;
import com.hbmspace.blocks.machine.MachineXenonThruster;
import com.hbmspace.client.model.SolarPanelModel;
import java.util.List;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

public final class StationBlocksSpace {

    private static final String STEEL = "hbm:block/block_steel";
    private static final String PORT_MESH = "models/machines/docking_port.obj";
    private static final String[] PORT_GROUPS = {"ArmZN", "ArmZP", "ArmXP", "ArmXN", "Port"};

    private static BlockBehaviour.Properties iron(float hardness, float resistance) {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(hardness, resistance)
                .requiresCorrectToolForDrops()
                .noOcclusion();
    }

    private static <B extends BlockOrbitalStation> Reg.Handle<B> station(
            Reg.Handle<B> block, String texture) {
        return block;
    }

    public static final Reg.Handle<BlockOrbitalStation> ORBITAL_STATION =
            station(
                    HbmSpace.REG.blockItem(
                            "orbital_station",
                            props -> new BlockOrbitalStation(props, true),
                            () -> iron(-1.0F, Float.POSITIVE_INFINITY)),
                    "block/models/machines/docking_port");
    public static final Reg.Handle<BlockOrbitalStation> ORBITAL_STATION_PORT =
            station(
                    HbmSpace.REG.blockItem(
                            "orbital_station_port",
                            props -> new BlockOrbitalStation(props, false),
                            () -> iron(1.0F, 1.0F)),
                    "block/models/machines/docking_port");
    public static final Reg.Handle<BlockOrbitalStationLauncher> ORBITAL_STATION_LAUNCHER =
            station(
                    HbmSpace.REG.blockItem(
                            "orbital_station_launcher",
                            BlockOrbitalStationLauncher::new,
                            () -> iron(1.0F, 1.0F),
                            (block, props) -> new DescBlockItem(block, props, 1)),
                    "block/models/machines/docking_port_launcher");

    public static final Reg.Handle<BlockOrbitalStationComputer> ORBITAL_STATION_COMPUTER =
            HbmSpace.REG.blockItem(
                    "orbital_station_computer",
                    BlockOrbitalStationComputer::new,
                    () -> iron(1.0F, 1.0F));

    public static final Reg.Handle<BlockStationPropulsionCreative> PROPULSION_CREATIVE =
            HbmSpace.REG.blockItem(
                    "propulsion_creative",
                    BlockStationPropulsionCreative::new,
                    () ->
                            BlockBehaviour.Properties.of()
                                    .mapColor(MapColor.METAL)
                                    .strength(1.0F)
                                    .requiresCorrectToolForDrops());

    public static final Reg.Handle<MachineHTR3> MACHINE_HTR3 =
            HbmSpace.REG.blockItem(
                    "machine_htr3",
                    MachineHTR3::new,
                    () -> iron(5.0F, 60.0F),
                    (block, props) -> new DescBlockItem(block, props, 1));

    public static final Reg.Handle<MachineHTRF4> MACHINE_HTRF4 =
            HbmSpace.REG.blockItem("machine_htrf4", MachineHTRF4::new, () -> iron(5.0F, 60.0F));

    public static final Reg.Handle<MachineHTRFNeo> MACHINE_HTRF4NEO =
            HbmSpace.REG.blockItem(
                    "machine_htrf4neo",
                    MachineHTRFNeo::new,
                    () -> iron(5.0F, 60.0F),
                    (block, props) -> new DescBlockItem(block, props, 1));

    public static final Reg.Handle<MachineXenonThruster> MACHINE_XENON_THRUSTER =
            HbmSpace.REG.blockItem(
                    "machine_xenon_thruster", MachineXenonThruster::new, () -> iron(5.0F, 60.0F));

    public static final Reg.Handle<BlockTransporterRocket> TRANSPORTER_ROCKET =
            HbmSpace.REG.blockItem(
                    "transporter_rocket", BlockTransporterRocket::new, () -> iron(1.0F, 1.0F));

    public static final Reg.Handle<MachineGasDock> GAS_DOCK =
            HbmSpace.REG.blockItem("gas_dock", MachineGasDock::new, () -> iron(5.0F, 6.0F));

    private static final String SOLAR_MESH = "models/machines/solar_panel.obj";
    private static final String SOLAR_SHEET = "block/models/machines/solar_panel";

    public static final Reg.Handle<MachineSolar> MACHINE_SOLAR =
            HbmSpace.REG
                    .blockItem("machine_solar", MachineSolar::new, () -> iron(5.0F, 6.0F))
                    .bakedBy(() -> new SolarPanelModel());

    public static final Reg.Handle<MachineRadiator> MACHINE_RADIATOR =
            HbmSpace.REG.blockItem(
                    "machine_radiator", MachineRadiator::new, () -> iron(5.0F, 6.0F));

    static {
        for (Reg.Handle<? extends Block> thruster :
                List.of(MACHINE_HTR3, MACHINE_HTRF4, MACHINE_HTRF4NEO, MACHINE_XENON_THRUSTER)) {
            CreativeContents.insertAfter(
                    Tab.MACHINES, Library.id("machine_lpw2"), () -> new ItemStack(thruster.get()));
        }
        for (Reg.Handle<? extends Block> station :
                List.of(
                        TRANSPORTER_ROCKET,
                        ORBITAL_STATION_PORT,
                        ORBITAL_STATION_LAUNCHER,
                        ORBITAL_STATION_COMPUTER,
                        PROPULSION_CREATIVE)) {
            CreativeContents.insertBefore(
                    Tab.MACHINES, Library.id("drone_patrol"), () -> new ItemStack(station.get()));
        }
        CreativeContents.insertAfter(
                Tab.MACHINES,
                Library.id("machine_hephaestus"),
                () -> new ItemStack(MACHINE_SOLAR.get()));
        CreativeContents.insertAfter(
                Tab.MACHINES,
                Library.id("machine_condenser_powered"),
                () -> new ItemStack(MACHINE_RADIATOR.get()));
        CreativeContents.insertAfter(
                Tab.MISSILES, Library.id("radar_screen"), () -> new ItemStack(GAS_DOCK.get()));
    }

    private StationBlocksSpace() {}
}
