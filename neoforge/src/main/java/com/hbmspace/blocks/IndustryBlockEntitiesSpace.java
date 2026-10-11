// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.blocks;

import com.hbm.blockentity.BlockEntityMachineBase;
import com.hbm.registration.Reg;
import com.hbmspace.HbmSpace;
import com.hbmspace.blockentity.machine.BlockEntityDishControl;
import com.hbmspace.blockentity.machine.BlockEntityHydroponic;
import com.hbmspace.blockentity.machine.BlockEntityMachineAlkylation;
import com.hbmspace.blockentity.machine.BlockEntityMachineCryoDistill;
import com.hbmspace.blockentity.machine.BlockEntityMachineMagma;
import com.hbmspace.blockentity.machine.BlockEntityMachineVacuumCircuit;
import java.util.Set;
import net.minecraft.world.level.block.entity.BlockEntityType;

public final class IndustryBlockEntitiesSpace {

    public static final Reg.BeHandle<BlockEntityMachineAlkylation> ALKYLATION =
            HbmSpace.REG
                    .be(
                            "machine_alkylation",
                            () ->
                                    new BlockEntityType<>(
                                            BlockEntityMachineAlkylation::new,
                                            Set.of(IndustryBlocksSpace.MACHINE_ALKYLATION.get())))
                    .ticksServer(BlockEntityMachineBase::tickServer);

    public static final Reg.BeHandle<BlockEntityMachineCryoDistill> CRYO_DISTILL =
            HbmSpace.REG
                    .be(
                            "machine_cryo_distill",
                            () ->
                                    new BlockEntityType<>(
                                            BlockEntityMachineCryoDistill::new,
                                            Set.of(IndustryBlocksSpace.MACHINE_CRYO_DISTILL.get())))
                    .ticksServer(BlockEntityMachineBase::tickServer);

    public static final Reg.BeHandle<BlockEntityMachineVacuumCircuit> VACUUM_CIRCUIT =
            HbmSpace.REG
                    .be(
                            "machine_vacuum_circuit",
                            () ->
                                    new BlockEntityType<>(
                                            BlockEntityMachineVacuumCircuit::new,
                                            Set.of(
                                                    IndustryBlocksSpace.MACHINE_VACUUM_CIRCUIT
                                                            .get())))
                    .ticksServer(BlockEntityMachineBase::tickServer);

    public static final Reg.BeHandle<BlockEntityDishControl> DISH_CONTROL =
            HbmSpace.REG
                    .be(
                            "dish_control",
                            () ->
                                    new BlockEntityType<>(
                                            BlockEntityDishControl::new,
                                            Set.of(IndustryBlocksSpace.DISH_CONTROL.get())))
                    .ticksServer(BlockEntityMachineBase::tickServer);

    public static final Reg.BeHandle<BlockEntityMachineMagma> MAGMA =
            HbmSpace.REG
                    .be(
                            "machine_magma",
                            () ->
                                    new BlockEntityType<>(
                                            BlockEntityMachineMagma::new,
                                            Set.of(IndustryBlocksSpace.MACHINE_MAGMA.get())))
                    .ticks(BlockEntityMachineBase::tickClient, BlockEntityMachineBase::tickServer);

    public static final Reg.BeHandle<BlockEntityHydroponic> HYDROBAY =
            HbmSpace.REG
                    .be(
                            "hydrobay",
                            () ->
                                    new BlockEntityType<>(
                                            BlockEntityHydroponic::new,
                                            Set.of(IndustryBlocksSpace.HYDROBAY.get())))
                    .ticksServer(BlockEntityMachineBase::tickServer);

    private IndustryBlockEntitiesSpace() {}
}
