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

package com.hbmspace.bulk;

import com.hbm.blockentity.MachineBlockEntity;
import com.hbm.blocks.TickingEntityBlock;
import com.hbm.blocks.multiblock.MultiblockCellBlock;
import com.hbm.blocks.multiblock.MultiblockCoreBlock;
import com.hbm.capability.CapabilityBlock;
import com.hbmspace.blockentity.bomb.LaunchPadRocketBlockEntity;
import com.hbmspace.blockentity.bomb.NukeAntimatterBlockEntity;
import com.hbmspace.blockentity.machine.AirPumpBlockEntity;
import com.hbmspace.blockentity.machine.AirScrubberBlockEntity;
import com.hbmspace.blockentity.machine.AlgaeFilmBlockEntity;
import com.hbmspace.blockentity.machine.AlkylationBlockEntity;
import com.hbmspace.blockentity.machine.AtmoTowerBlockEntity;
import com.hbmspace.blockentity.machine.AtmosphereEditorBlockEntity;
import com.hbmspace.blockentity.machine.AtmosphericCompressorBlockEntity;
import com.hbmspace.blockentity.machine.AtmosphericEmitterBlockEntity;
import com.hbmspace.blockentity.machine.ChungusSpaceBlockEntity;
import com.hbmspace.blockentity.machine.CondenserPoweredSpaceBlockEntity;
import com.hbmspace.blockentity.machine.CryoDistillBlockEntity;
import com.hbmspace.blockentity.machine.DishControlBlockEntity;
import com.hbmspace.blockentity.machine.DriveProcessorBlockEntity;
import com.hbmspace.blockentity.machine.DysonConverterAnatmogenesisBlockEntity;
import com.hbmspace.blockentity.machine.DysonConverterHEBlockEntity;
import com.hbmspace.blockentity.machine.DysonConverterTUBlockEntity;
import com.hbmspace.blockentity.machine.DysonLauncherBlockEntity;
import com.hbmspace.blockentity.machine.DysonReceiverBlockEntity;
import com.hbmspace.blockentity.machine.GasDockBlockEntity;
import com.hbmspace.blockentity.machine.HTR3BlockEntity;
import com.hbmspace.blockentity.machine.HTRF4BlockEntity;
import com.hbmspace.blockentity.machine.HTRNeoBlockEntity;
import com.hbmspace.blockentity.machine.HydroponicBlockEntity;
import com.hbmspace.blockentity.machine.LPW2SpaceBlockEntity;
import com.hbmspace.blockentity.machine.MagmaBlockEntity;
import com.hbmspace.blockentity.machine.MilkReformerBlockEntity;
import com.hbmspace.blockentity.machine.OrbitalStationBlockEntity;
import com.hbmspace.blockentity.machine.OrbitalStationComputerBlockEntity;
import com.hbmspace.blockentity.machine.OrbitalStationLauncherBlockEntity;
import com.hbmspace.blockentity.machine.RadiatorBlockEntity;
import com.hbmspace.blockentity.machine.RocketAssemblyBlockEntity;
import com.hbmspace.blockentity.machine.SolarPanelBlockEntity;
import com.hbmspace.blockentity.machine.StardarBlockEntity;
import com.hbmspace.blockentity.machine.TransporterBaseBlockEntity;
import com.hbmspace.blockentity.machine.TransporterRocketBlockEntity;
import com.hbmspace.blockentity.machine.VacuumCircuitBlockEntity;
import com.hbmspace.blockentity.machine.WarControllerBlockEntity;
import com.hbmspace.blockentity.machine.XenonThrusterBlockEntity;
import com.hbmspace.blocks.GeysierDCMBlock;
import com.hbmspace.blocks.VolcanoV2Block;
import com.hbmspace.blocks.bomb.CombatDropPod;
import com.hbmspace.blocks.bomb.LaunchPadRocket;
import com.hbmspace.blocks.bomb.NukeAntimatter;
import com.hbmspace.blocks.generic.GeysirVolcanicBlock;
import com.hbmspace.blocks.machine.AirPumpBlock;
import com.hbmspace.blocks.machine.AirScrubberBlock;
import com.hbmspace.blocks.machine.AlgaeFilmBlock;
import com.hbmspace.blocks.machine.AlkylationBlock;
import com.hbmspace.blocks.machine.AtmoTower;
import com.hbmspace.blocks.machine.AtmosphereEditorBlock;
import com.hbmspace.blocks.machine.AtmosphericCompressorBlock;
import com.hbmspace.blocks.machine.AtmosphericEmitterBlock;
import com.hbmspace.blocks.machine.CryoDistillBlock;
import com.hbmspace.blocks.machine.DishControlBlock;
import com.hbmspace.blocks.machine.DriveProcessorBlock;
import com.hbmspace.blocks.machine.DysonConverterAnatmogenesisBlock;
import com.hbmspace.blocks.machine.DysonConverterHEBlock;
import com.hbmspace.blocks.machine.DysonConverterTUBlock;
import com.hbmspace.blocks.machine.DysonLauncherBlock;
import com.hbmspace.blocks.machine.DysonReceiverBlock;
import com.hbmspace.blocks.machine.GasDockBlock;
import com.hbmspace.blocks.machine.HTR3Block;
import com.hbmspace.blocks.machine.HTRF4Block;
import com.hbmspace.blocks.machine.HTRFNeoBlock;
import com.hbmspace.blocks.machine.HydrobayEndCell;
import com.hbmspace.blocks.machine.HydroponicBlock;
import com.hbmspace.blocks.machine.MagmaBlock;
import com.hbmspace.blocks.machine.MilkReformerBlock;
import com.hbmspace.blocks.machine.OrbitalStationBlock;
import com.hbmspace.blocks.machine.OrbitalStationComputerBlock;
import com.hbmspace.blocks.machine.OrbitalStationLauncherBlock;
import com.hbmspace.blocks.machine.RadiatorBlock;
import com.hbmspace.blocks.machine.RocketAssemblyBlock;
import com.hbmspace.blocks.machine.SolarBlock;
import com.hbmspace.blocks.machine.StardarBlock;
import com.hbmspace.blocks.machine.TransporterRocketBlock;
import com.hbmspace.blocks.machine.VacuumCircuitBlock;
import com.hbmspace.blocks.machine.WarController;
import com.hbmspace.blocks.machine.XenonThrusterBlock;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public final class BulkBindings implements com.hbm.world.bulk.BulkBindings {
    public Class<?>[] types() {
        return new Class<?>[] {
            LaunchPadRocketBlockEntity.class,
            NukeAntimatterBlockEntity.class,
            AirPumpBlockEntity.class,
            AirScrubberBlockEntity.class,
            AlgaeFilmBlockEntity.class,
            AlkylationBlockEntity.class,
            AtmoTowerBlockEntity.class,
            AtmosphereEditorBlockEntity.class,
            AtmosphericCompressorBlockEntity.class,
            AtmosphericEmitterBlockEntity.class,
            ChungusSpaceBlockEntity.class,
            CondenserPoweredSpaceBlockEntity.class,
            CryoDistillBlockEntity.class,
            DishControlBlockEntity.class,
            DriveProcessorBlockEntity.class,
            DysonConverterAnatmogenesisBlockEntity.class,
            DysonConverterHEBlockEntity.class,
            DysonConverterTUBlockEntity.class,
            DysonLauncherBlockEntity.class,
            DysonReceiverBlockEntity.class,
            GasDockBlockEntity.class,
            HTR3BlockEntity.class,
            HTRF4BlockEntity.class,
            HTRNeoBlockEntity.class,
            HydroponicBlockEntity.class,
            LPW2SpaceBlockEntity.class,
            MagmaBlockEntity.class,
            MilkReformerBlockEntity.class,
            OrbitalStationBlockEntity.class,
            OrbitalStationComputerBlockEntity.class,
            OrbitalStationLauncherBlockEntity.class,
            RadiatorBlockEntity.class,
            RocketAssemblyBlockEntity.class,
            SolarPanelBlockEntity.class,
            StardarBlockEntity.class,
            TransporterBaseBlockEntity.class,
            TransporterRocketBlockEntity.class,
            VacuumCircuitBlockEntity.class,
            WarControllerBlockEntity.class,
            XenonThrusterBlockEntity.class,
            GeysierDCMBlock.class,
            VolcanoV2Block.class,
            CombatDropPod.class,
            LaunchPadRocket.class,
            NukeAntimatter.class,
            GeysirVolcanicBlock.class,
            AirPumpBlock.class,
            AirScrubberBlock.class,
            AlgaeFilmBlock.class,
            AlkylationBlock.class,
            AtmoTower.class,
            AtmosphereEditorBlock.class,
            AtmosphericCompressorBlock.class,
            AtmosphericEmitterBlock.class,
            CryoDistillBlock.class,
            DishControlBlock.class,
            DriveProcessorBlock.class,
            DysonConverterAnatmogenesisBlock.class,
            DysonConverterHEBlock.class,
            DysonConverterTUBlock.class,
            DysonLauncherBlock.class,
            DysonReceiverBlock.class,
            GasDockBlock.class,
            HTR3Block.class,
            HTRF4Block.class,
            HTRFNeoBlock.class,
            HydrobayEndCell.class,
            HydroponicBlock.class,
            MagmaBlock.class,
            MilkReformerBlock.class,
            OrbitalStationBlock.class,
            OrbitalStationComputerBlock.class,
            OrbitalStationLauncherBlock.class,
            RadiatorBlock.class,
            RocketAssemblyBlock.class,
            SolarBlock.class,
            StardarBlock.class,
            TransporterRocketBlock.class,
            VacuumCircuitBlock.class,
            WarController.class,
            XenonThrusterBlock.class,
        };
    }

    public boolean relevant(int kind, BlockState state) {
        switch (kind) {
            case 40, 41, 42, 44, 45, 51, 71 -> {
                boolean result = false;
                result |= ((TickingEntityBlock) state.getBlock()).bulkTickingRelevant(state);
                return result;
            }
            case 43,
                    47,
                    49,
                    50,
                    52,
                    53,
                    54,
                    56,
                    58,
                    60,
                    62,
                    63,
                    64,
                    65,
                    67,
                    68,
                    69,
                    70,
                    72,
                    73,
                    75,
                    77,
                    78,
                    80 -> {
                boolean result = false;
                result |= ((MultiblockCoreBlock) state.getBlock()).bulkCoreRelevant(state);
                result |= ((TickingEntityBlock) state.getBlock()).bulkTickingRelevant(state);
                result |= ((CapabilityBlock) state.getBlock()).bulkCapabilityRelevant(state);
                return result;
            }
            case 46, 48, 79 -> {
                boolean result = false;
                result |= ((TickingEntityBlock) state.getBlock()).bulkTickingRelevant(state);
                result |= ((CapabilityBlock) state.getBlock()).bulkCapabilityRelevant(state);
                return result;
            }
            case 55, 57, 61, 74, 76 -> {
                boolean result = false;
                result |= ((MultiblockCoreBlock) state.getBlock()).bulkCoreRelevant(state);
                result |= ((TickingEntityBlock) state.getBlock()).bulkTickingRelevant(state);
                return result;
            }
            case 59 -> {
                boolean result = false;
                result |= ((MultiblockCoreBlock) state.getBlock()).bulkCoreRelevant(state);
                return result;
            }
            case 66 -> {
                boolean result = false;
                result |= ((MultiblockCellBlock) state.getBlock()).bulkCellRelevant(state);
                return result;
            }
            default -> {
                return com.hbm.world.bulk.BulkBindings.super.relevant(kind, state);
            }
        }
    }

    public boolean cell(int kind, BlockState state) {
        switch (kind) {
            case 43,
                    47,
                    49,
                    50,
                    52,
                    53,
                    54,
                    55,
                    56,
                    57,
                    58,
                    59,
                    60,
                    61,
                    62,
                    63,
                    64,
                    65,
                    67,
                    68,
                    69,
                    70,
                    72,
                    73,
                    74,
                    75,
                    76,
                    77,
                    78,
                    80 -> {
                boolean result = false;
                result |= ((MultiblockCoreBlock) state.getBlock()).bulkSnapshotCell(state);
                return result;
            }
            case 66 -> {
                boolean result = false;
                result |= ((MultiblockCellBlock) state.getBlock()).bulkSnapshotCell(state);
                return result;
            }
            default -> {
                return com.hbm.world.bulk.BulkBindings.super.cell(kind, state);
            }
        }
    }

    public boolean capture(
            int kind,
            ServerLevel level,
            BlockPos pos,
            BlockState state,
            CompoundTag data,
            Consumer<BlockPos> cells) {
        switch (kind) {
            case 43,
                    47,
                    49,
                    50,
                    52,
                    53,
                    54,
                    55,
                    56,
                    57,
                    58,
                    59,
                    60,
                    61,
                    62,
                    63,
                    64,
                    65,
                    67,
                    68,
                    69,
                    70,
                    72,
                    73,
                    74,
                    75,
                    76,
                    77,
                    78,
                    80 -> {
                boolean result = false;
                result |=
                        ((MultiblockCoreBlock) state.getBlock())
                                .captureBulkCells(level, pos, state, data, cells);
                return result;
            }
            default -> {
                return com.hbm.world.bulk.BulkBindings.super.capture(
                        kind, level, pos, state, data, cells);
            }
        }
    }

    public void blockPublished(
            int kind, ServerLevel level, BlockPos pos, BlockState state, CompoundTag data) {
        switch (kind) {
            case 43,
                    47,
                    49,
                    50,
                    52,
                    53,
                    54,
                    55,
                    56,
                    57,
                    58,
                    59,
                    60,
                    61,
                    62,
                    63,
                    64,
                    65,
                    67,
                    68,
                    69,
                    70,
                    72,
                    73,
                    74,
                    75,
                    76,
                    77,
                    78,
                    80 -> {
                ((MultiblockCoreBlock) state.getBlock()).publishBulkCells(level, pos, state, data);
                return;
            }
            default -> {
                com.hbm.world.bulk.BulkBindings.super.blockPublished(kind, level, pos, state, data);
            }
        }
    }

    public void clearInventory(int kind, BlockEntity be) {
        switch (kind) {
            case 0,
                    1,
                    2,
                    3,
                    4,
                    5,
                    6,
                    7,
                    8,
                    9,
                    10,
                    11,
                    12,
                    13,
                    14,
                    15,
                    16,
                    17,
                    18,
                    19,
                    20,
                    21,
                    22,
                    23,
                    24,
                    25,
                    26,
                    27,
                    28,
                    29,
                    30,
                    31,
                    32,
                    33,
                    34,
                    35,
                    36,
                    37,
                    38,
                    39 -> {
                ((MachineBlockEntity) be).clearBulkInventory();
                return;
            }
            default -> {
                com.hbm.world.bulk.BulkBindings.super.clearInventory(kind, be);
            }
        }
    }

    public void forgetDrops(int kind, BlockEntity be) {
        switch (kind) {
            case 0,
                    1,
                    2,
                    3,
                    4,
                    5,
                    6,
                    7,
                    8,
                    9,
                    10,
                    11,
                    12,
                    13,
                    14,
                    15,
                    16,
                    17,
                    18,
                    19,
                    20,
                    21,
                    22,
                    23,
                    24,
                    25,
                    26,
                    27,
                    28,
                    29,
                    30,
                    31,
                    32,
                    33,
                    34,
                    35,
                    36,
                    37,
                    38,
                    39 -> {
                ((MachineBlockEntity) be).forgetBulkDrops();
                return;
            }
            default -> {
                com.hbm.world.bulk.BulkBindings.super.forgetDrops(kind, be);
            }
        }
    }
}
