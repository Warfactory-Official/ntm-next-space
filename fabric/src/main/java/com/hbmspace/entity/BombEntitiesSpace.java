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

package com.hbmspace.entity;

import com.hbm.registration.Reg;
import com.hbmspace.HbmSpace;
import com.hbmspace.entity.logic.AntimatterBlastEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

public final class BombEntitiesSpace {
    public static final Reg.EntityHandle<AntimatterBlastEntity> ANTIMATTER_BLAST =
            HbmSpace.REG.entity(
                    "entity_antimatter",
                    () ->
                            EntityType.Builder.<AntimatterBlastEntity>of(
                                            AntimatterBlastEntity::new, MobCategory.MISC)
                                    .noLootTable()
                                    .sized(0.6F, 1.8F)
                                    .clientTrackingRange(63)
                                    .updateInterval(1)
                                    .build(
                                            ResourceKey.create(
                                                    Registries.ENTITY_TYPE,
                                                    Identifier.fromNamespaceAndPath(
                                                            HbmSpace.MOD_ID,
                                                            "entity_antimatter"))));

    private BombEntitiesSpace() {}
}
