// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.blocks;

import com.hbm.items.CreativeContents.Tab;
import com.hbm.items.CreativeContents;
import com.hbm.items.DescBlockItem;
import com.hbm.registration.Reg;
import com.hbmspace.HbmSpace;
import com.hbmspace.blocks.machine.HydrobayEndCell;
import com.hbmspace.blocks.machine.MachineAlkylation;
import com.hbmspace.blocks.machine.MachineCryoDistill;
import com.hbmspace.blocks.machine.MachineDishControl;
import com.hbmspace.blocks.machine.MachineHydroponic;
import com.hbmspace.blocks.machine.MachineMagma;
import com.hbmspace.blocks.machine.MachineTrollDisturber;
import com.hbmspace.blocks.machine.MachineVacuumCircuit;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

public final class IndustryBlocksSpace {
    private static BlockBehaviour.Properties iron(float hardness, float resistance) {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(hardness, resistance)
                .requiresCorrectToolForDrops();
    }

    public static final Reg.Handle<MachineTrollDisturber> TROLL_DISTURBER =
            HbmSpace.REG.blockItem(
                    "troll_disturber", MachineTrollDisturber::new, () -> iron(5.0F, 120.0F));

    public static final Reg.Handle<MachineAlkylation> MACHINE_ALKYLATION =
            HbmSpace.REG.blockItem(
                    "machine_alkylation",
                    MachineAlkylation::new,
                    () -> iron(5.0F, 6.0F).noOcclusion());

    public static final Reg.Handle<MachineCryoDistill> MACHINE_CRYO_DISTILL =
            HbmSpace.REG.blockItem(
                    "machine_cryo_distill",
                    MachineCryoDistill::new,
                    () -> iron(5.0F, 6.0F).noOcclusion());

    public static final Reg.Handle<MachineVacuumCircuit> MACHINE_VACUUM_CIRCUIT =
            HbmSpace.REG.blockItem(
                    "machine_vacuum_circuit",
                    MachineVacuumCircuit::new,
                    () -> iron(5.0F, 18.0F).noOcclusion());

    public static final Reg.Handle<MachineDishControl> DISH_CONTROL =
            HbmSpace.REG.blockItem(
                    "dish_control",
                    MachineDishControl::new,
                    () -> iron(5.0F, 6.0F).noOcclusion(),
                    (block, props) -> new DescBlockItem(block, props, 3));

    public static final Reg.Handle<MachineMagma> MACHINE_MAGMA =
            HbmSpace.REG.blockItem(
                    "machine_magma",
                    MachineMagma::new,
                    () -> iron(5.0F, 60.0F).noOcclusion(),
                    (block, props) -> new DescBlockItem(block, props, 1));

    public static final Reg.Handle<HydrobayEndCell> HYDROBAY_END_CELL =
            HbmSpace.REG.block(
                    "hydrobay_end_cell",
                    HydrobayEndCell::new,
                    () -> iron(5.0F, 6.0F).noOcclusion().dynamicShape());

    public static final Reg.Handle<MachineHydroponic> HYDROBAY =
            HbmSpace.REG.blockItem(
                    "hydrobay",
                    MachineHydroponic::new,
                    () -> iron(5.0F, 6.0F).noOcclusion(),
                    (block, props) -> new DescBlockItem(block, props, 2));

    static {
        CreativeContents.insertAfter(
                Tab.MACHINES,
                Identifier.fromNamespaceAndPath(HbmSpace.MOD_ID, "machine_drive_processor"),
                () -> new ItemStack(MACHINE_ALKYLATION.get()));
        CreativeContents.insertAfter(
                Tab.MACHINES,
                Identifier.fromNamespaceAndPath(HbmSpace.MOD_ID, "machine_milk_reformer"),
                () -> new ItemStack(MACHINE_CRYO_DISTILL.get()));
        CreativeContents.insertAfter(
                Tab.MACHINES,
                Identifier.fromNamespaceAndPath("hbm", "machine_soldering_station"),
                () -> new ItemStack(MACHINE_VACUUM_CIRCUIT.get()));
        CreativeContents.insertAfter(
                Tab.MACHINES,
                Identifier.fromNamespaceAndPath("hbm", "machine_combustion_engine"),
                () -> new ItemStack(DISH_CONTROL.get()));
        CreativeContents.insertAfter(
                Tab.MACHINES,
                Identifier.fromNamespaceAndPath(HbmSpace.MOD_ID, "machine_xenon_thruster"),
                () -> new ItemStack(MACHINE_MAGMA.get()));
        CreativeContents.insertAfter(
                Tab.MACHINES,
                Identifier.fromNamespaceAndPath(HbmSpace.MOD_ID, "algae_film"),
                () -> new ItemStack(HYDROBAY.get()));
    }

    private IndustryBlocksSpace() {}
}
