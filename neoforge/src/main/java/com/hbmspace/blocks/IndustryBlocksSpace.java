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
import com.hbmspace.HbmSpace;
import com.hbmspace.blocks.machine.AlkylationBlock;
import com.hbmspace.blocks.machine.CryoDistillBlock;
import com.hbmspace.blocks.machine.DishControlBlock;
import com.hbmspace.blocks.machine.HydrobayEndCell;
import com.hbmspace.blocks.machine.HydroponicBlock;
import com.hbmspace.blocks.machine.MagmaBlock;
import com.hbmspace.blocks.machine.TrollDisturberBlock;
import com.hbmspace.blocks.machine.VacuumCircuitBlock;
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

    public static final Reg.Handle<TrollDisturberBlock> TROLL_DISTURBER =
            HbmSpace.REG.blockItem(
                    "troll_disturber", TrollDisturberBlock::new, () -> iron(5.0F, 120.0F));

    public static final Reg.Handle<AlkylationBlock> MACHINE_ALKYLATION =
            HbmSpace.REG.blockItem(
                    "machine_alkylation",
                    AlkylationBlock::new,
                    () -> iron(5.0F, 6.0F).noOcclusion());

    public static final Reg.Handle<CryoDistillBlock> MACHINE_CRYO_DISTILL =
            HbmSpace.REG.blockItem(
                    "machine_cryo_distill",
                    CryoDistillBlock::new,
                    () -> iron(5.0F, 6.0F).noOcclusion());

    public static final Reg.Handle<VacuumCircuitBlock> MACHINE_VACUUM_CIRCUIT =
            HbmSpace.REG.blockItem(
                    "machine_vacuum_circuit",
                    VacuumCircuitBlock::new,
                    () -> iron(5.0F, 18.0F).noOcclusion());

    public static final Reg.Handle<DishControlBlock> DISH_CONTROL =
            HbmSpace.REG.blockItem(
                    "dish_control",
                    DishControlBlock::new,
                    () -> iron(5.0F, 6.0F).noOcclusion(),
                    (block, props) -> new DescBlockItem(block, props, 3));

    public static final Reg.Handle<MagmaBlock> MACHINE_MAGMA =
            HbmSpace.REG.blockItem(
                    "machine_magma",
                    MagmaBlock::new,
                    () -> iron(5.0F, 60.0F).noOcclusion(),
                    (block, props) -> new DescBlockItem(block, props, 1));

    public static final Reg.Handle<HydrobayEndCell> HYDROBAY_END_CELL =
            HbmSpace.REG.block(
                    "hydrobay_end_cell",
                    HydrobayEndCell::new,
                    () -> iron(5.0F, 6.0F).noOcclusion().dynamicShape());

    public static final Reg.Handle<HydroponicBlock> HYDROBAY =
            HbmSpace.REG.blockItem(
                    "hydrobay",
                    HydroponicBlock::new,
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
