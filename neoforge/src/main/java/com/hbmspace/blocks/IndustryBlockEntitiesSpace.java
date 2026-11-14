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
import com.hbmspace.blockentity.machine.AlkylationBlockEntity;
import com.hbmspace.blockentity.machine.CryoDistillBlockEntity;
import com.hbmspace.blockentity.machine.DishControlBlockEntity;
import com.hbmspace.blockentity.machine.HydroponicBlockEntity;
import com.hbmspace.blockentity.machine.MagmaBlockEntity;
import com.hbmspace.blockentity.machine.VacuumCircuitBlockEntity;
import java.util.Set;
import net.minecraft.world.level.block.entity.BlockEntityType;

public final class IndustryBlockEntitiesSpace {

    public static final Reg.BeHandle<AlkylationBlockEntity> ALKYLATION =
            (HbmSpace.REG.<AlkylationBlockEntity>be(
                            "machine_alkylation",
                            () ->
                                    new BlockEntityType<>(
                                            AlkylationBlockEntity::new,
                                            Set.of(IndustryBlocksSpace.MACHINE_ALKYLATION.get()))))
                    .ticksServerAt(BlockEntityTicks.SERVER);

    public static final Reg.BeHandle<CryoDistillBlockEntity> CRYO_DISTILL =
            (HbmSpace.REG.<CryoDistillBlockEntity>be(
                            "machine_cryo_distill",
                            () ->
                                    new BlockEntityType<>(
                                            CryoDistillBlockEntity::new,
                                            Set.of(
                                                    IndustryBlocksSpace.MACHINE_CRYO_DISTILL
                                                            .get()))))
                    .ticksServerAt(BlockEntityTicks.SERVER);

    public static final Reg.BeHandle<VacuumCircuitBlockEntity> VACUUM_CIRCUIT =
            (HbmSpace.REG.<VacuumCircuitBlockEntity>be(
                            "machine_vacuum_circuit",
                            () ->
                                    new BlockEntityType<>(
                                            VacuumCircuitBlockEntity::new,
                                            Set.of(
                                                    IndustryBlocksSpace.MACHINE_VACUUM_CIRCUIT
                                                            .get()))))
                    .ticksServerAt(BlockEntityTicks.SERVER);

    public static final Reg.BeHandle<DishControlBlockEntity> DISH_CONTROL =
            (HbmSpace.REG.<DishControlBlockEntity>be(
                    "dish_control",
                    () ->
                            new BlockEntityType<>(
                                    DishControlBlockEntity::new,
                                    Set.of(IndustryBlocksSpace.DISH_CONTROL.get()))));

    public static final Reg.BeHandle<MagmaBlockEntity> MAGMA =
            (HbmSpace.REG.<MagmaBlockEntity>be(
                            "machine_magma",
                            () ->
                                    new BlockEntityType<>(
                                            MagmaBlockEntity::new,
                                            Set.of(IndustryBlocksSpace.MACHINE_MAGMA.get()))))
                    .ticksServerAt(BlockEntityTicks.SERVER)
                    .ticksClientAt(BlockEntityTicks.CLIENT);

    public static final Reg.BeHandle<HydroponicBlockEntity> HYDROBAY =
            (HbmSpace.REG.<HydroponicBlockEntity>be(
                            "hydrobay",
                            () ->
                                    new BlockEntityType<>(
                                            HydroponicBlockEntity::new,
                                            Set.of(IndustryBlocksSpace.HYDROBAY.get()))))
                    .ticksServerAt(BlockEntityTicks.SERVER);

    private IndustryBlockEntitiesSpace() {}
}
