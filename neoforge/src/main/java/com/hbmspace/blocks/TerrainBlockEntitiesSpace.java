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
import com.hbmspace.blockentity.deco.GeysirDCMBlockEntity;
import com.hbmspace.blockentity.deco.GeysirSpaceBlockEntity;
import com.hbmspace.blockentity.deco.LightningVolcanoBlockEntity;
import com.hbmspace.blockentity.machine.AlgaeFilmBlockEntity;
import java.util.Set;
import net.minecraft.world.level.block.entity.BlockEntityType;

public final class TerrainBlockEntitiesSpace {

    public static final Reg.BeHandle<GeysirSpaceBlockEntity> GEYSIR =
            (HbmSpace.REG.<GeysirSpaceBlockEntity>be(
                            "geysir",
                            () ->
                                    new BlockEntityType<>(
                                            GeysirSpaceBlockEntity::new,
                                            Set.of(TerrainBlocksSpace.GEYSIR_VOLCANIC.get()))))
                    .ticksServerAt(BlockEntityTicks.SERVER);

    public static final Reg.BeHandle<LightningVolcanoBlockEntity> LIGHTNING_VOLCANO =
            (HbmSpace.REG.<LightningVolcanoBlockEntity>be(
                            "lightning_volcano",
                            () ->
                                    new BlockEntityType<>(
                                            LightningVolcanoBlockEntity::new,
                                            Set.of(TerrainBlocksSpace.GEYSIR_ELECTRIC.get()))))
                    .ticksServerAt(BlockEntityTicks.SERVER)
                    .ticksClientAt(BlockEntityTicks.CLIENT);

    public static final Reg.BeHandle<GeysirDCMBlockEntity> GEYSIR_DCM =
            (HbmSpace.REG.<GeysirDCMBlockEntity>be(
                            "geysir_dcm",
                            () ->
                                    new BlockEntityType<>(
                                            GeysirDCMBlockEntity::new,
                                            Set.of(TerrainBlocksSpace.GEYSIR_CHLORIC.get()))))
                    .ticksServerAt(BlockEntityTicks.SERVER)
                    .ticksClientAt(BlockEntityTicks.CLIENT);

    public static final Reg.BeHandle<AlgaeFilmBlockEntity> ALGAE_FILM =
            (HbmSpace.REG.<AlgaeFilmBlockEntity>be(
                            "algae_film",
                            () ->
                                    new BlockEntityType<>(
                                            AlgaeFilmBlockEntity::new,
                                            Set.of(TerrainBlocksSpace.ALGAE_FILM.get()))))
                    .ticksServerAt(BlockEntityTicks.SERVER);

    private TerrainBlockEntitiesSpace() {}
}
