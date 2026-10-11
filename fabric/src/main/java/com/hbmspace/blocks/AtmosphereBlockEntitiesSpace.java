// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.blocks;

import com.hbm.blockentity.BlockEntityMachineBase;
import com.hbm.registration.Reg;
import com.hbmspace.HbmSpace;
import com.hbmspace.blockentity.machine.BlockEntityAirPump;
import com.hbmspace.blockentity.machine.BlockEntityAirScrubber;
import com.hbmspace.blockentity.machine.BlockEntityAtmoTower;
import com.hbmspace.blockentity.machine.BlockEntityAtmosphereEditor;
import com.hbmspace.blockentity.machine.BlockEntityAtmosphericCompressor;
import com.hbmspace.blockentity.machine.BlockEntityAtmosphericEmitter;
import java.util.Set;
import net.minecraft.world.level.block.entity.BlockEntityType;

public final class AtmosphereBlockEntitiesSpace {

    public static final Reg.BeHandle<BlockEntityAirPump> AIR_PUMP =
            HbmSpace.REG
                    .be(
                            "air_vent",
                            () ->
                                    new BlockEntityType<>(
                                            BlockEntityAirPump::new,
                                            Set.of(AtmosphereBlocksSpace.AIR_VENT.get())))
                    .ticks(BlockEntityMachineBase::tickClient, BlockEntityMachineBase::tickServer);

    public static final Reg.BeHandle<BlockEntityAirScrubber> AIR_SCRUBBER =
            HbmSpace.REG
                    .be(
                            "air_scrubber",
                            () ->
                                    new BlockEntityType<>(
                                            BlockEntityAirScrubber::new,
                                            Set.of(AtmosphereBlocksSpace.AIR_SCRUBBER.get())))
                    .ticks(BlockEntityMachineBase::tickClient, BlockEntityMachineBase::tickServer);

    public static final Reg.BeHandle<BlockEntityAtmoTower> ATMO_TOWER =
            HbmSpace.REG
                    .be(
                            "atmospheric_tower",
                            () ->
                                    new BlockEntityType<>(
                                            BlockEntityAtmoTower::new,
                                            Set.of(AtmosphereBlocksSpace.MACHINE_ATMO_TOWER.get())))
                    .ticksServer(BlockEntityMachineBase::tickServer);

    public static final Reg.BeHandle<BlockEntityAtmosphericCompressor> ATMOSPHERIC_COMPRESSOR =
            HbmSpace.REG
                    .be(
                            "atmospheric_vent",
                            () ->
                                    new BlockEntityType<>(
                                            BlockEntityAtmosphericCompressor::new,
                                            Set.of(AtmosphereBlocksSpace.MACHINE_ATMO_VENT.get())))
                    .ticks(BlockEntityMachineBase::tickClient, BlockEntityMachineBase::tickServer);

    public static final Reg.BeHandle<BlockEntityAtmosphericEmitter> ATMOSPHERIC_EMITTER =
            HbmSpace.REG
                    .be(
                            "atmo_emitter",
                            () ->
                                    new BlockEntityType<>(
                                            BlockEntityAtmosphericEmitter::new,
                                            Set.of(
                                                    AtmosphereBlocksSpace.MACHINE_ATMO_EMITTER
                                                            .get())))
                    .ticks(BlockEntityMachineBase::tickClient, BlockEntityMachineBase::tickServer);

    public static final Reg.BeHandle<BlockEntityAtmosphereEditor> ATMOSPHERE_EDITOR =
            HbmSpace.REG
                    .be(
                            "atmosphere_editor",
                            () ->
                                    new BlockEntityType<>(
                                            BlockEntityAtmosphereEditor::new,
                                            Set.of(AtmosphereBlocksSpace.ATMOSPHERE_EDITOR.get())))
                    .ticksServer(BlockEntityMachineBase::tickServer);

    private AtmosphereBlockEntitiesSpace() {}
}
