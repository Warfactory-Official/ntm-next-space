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
import com.hbmspace.blockentity.machine.AirPumpBlockEntity;
import com.hbmspace.blockentity.machine.AirScrubberBlockEntity;
import com.hbmspace.blockentity.machine.AtmoTowerBlockEntity;
import com.hbmspace.blockentity.machine.AtmosphereEditorBlockEntity;
import com.hbmspace.blockentity.machine.AtmosphericCompressorBlockEntity;
import com.hbmspace.blockentity.machine.AtmosphericEmitterBlockEntity;
import java.util.Set;
import net.minecraft.world.level.block.entity.BlockEntityType;

public final class AtmosphereBlockEntitiesSpace {

    public static final Reg.BeHandle<AirPumpBlockEntity> AIR_PUMP =
            (HbmSpace.REG.<AirPumpBlockEntity>be(
                            "air_vent",
                            () ->
                                    new BlockEntityType<>(
                                            AirPumpBlockEntity::new,
                                            Set.of(AtmosphereBlocksSpace.AIR_VENT.get()))))
                    .ticksServerAt(BlockEntityTicks.SERVER)
                    .ticksClientAt(BlockEntityTicks.CLIENT);

    public static final Reg.BeHandle<AirScrubberBlockEntity> AIR_SCRUBBER =
            (HbmSpace.REG.<AirScrubberBlockEntity>be(
                            "air_scrubber",
                            () ->
                                    new BlockEntityType<>(
                                            AirScrubberBlockEntity::new,
                                            Set.of(AtmosphereBlocksSpace.AIR_SCRUBBER.get()))))
                    .ticksServerAt(BlockEntityTicks.SERVER)
                    .ticksClientAt(BlockEntityTicks.CLIENT);

    public static final Reg.BeHandle<AtmoTowerBlockEntity> ATMO_TOWER =
            (HbmSpace.REG.<AtmoTowerBlockEntity>be(
                            "atmospheric_tower",
                            () ->
                                    new BlockEntityType<>(
                                            AtmoTowerBlockEntity::new,
                                            Set.of(
                                                    AtmosphereBlocksSpace.MACHINE_ATMO_TOWER
                                                            .get()))))
                    .ticksServerAt(BlockEntityTicks.SERVER);

    public static final Reg.BeHandle<AtmosphericCompressorBlockEntity> ATMOSPHERIC_COMPRESSOR =
            (HbmSpace.REG.<AtmosphericCompressorBlockEntity>be(
                            "atmospheric_vent",
                            () ->
                                    new BlockEntityType<>(
                                            AtmosphericCompressorBlockEntity::new,
                                            Set.of(AtmosphereBlocksSpace.MACHINE_ATMO_VENT.get()))))
                    .ticksServerAt(BlockEntityTicks.SERVER)
                    .ticksClientAt(BlockEntityTicks.CLIENT);

    public static final Reg.BeHandle<AtmosphericEmitterBlockEntity> ATMOSPHERIC_EMITTER =
            (HbmSpace.REG.<AtmosphericEmitterBlockEntity>be(
                            "atmo_emitter",
                            () ->
                                    new BlockEntityType<>(
                                            AtmosphericEmitterBlockEntity::new,
                                            Set.of(
                                                    AtmosphereBlocksSpace.MACHINE_ATMO_EMITTER
                                                            .get()))))
                    .ticksServerAt(BlockEntityTicks.SERVER_WITH_SOUND)
                    .ticksClientAt(BlockEntityTicks.CLIENT);

    public static final Reg.BeHandle<AtmosphereEditorBlockEntity> ATMOSPHERE_EDITOR =
            (HbmSpace.REG.<AtmosphereEditorBlockEntity>be(
                            "atmosphere_editor",
                            () ->
                                    new BlockEntityType<>(
                                            AtmosphereEditorBlockEntity::new,
                                            Set.of(AtmosphereBlocksSpace.ATMOSPHERE_EDITOR.get()))))
                    .ticksServerAt(BlockEntityTicks.SERVER);

    private AtmosphereBlockEntitiesSpace() {}
}
