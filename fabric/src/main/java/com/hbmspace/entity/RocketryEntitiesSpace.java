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
import com.hbmspace.entity.missile.CombatDropPodEntity;
import com.hbmspace.entity.missile.RideableRocketEntity.EntityRideableRocketDummy;
import com.hbmspace.entity.missile.RideableRocketEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

public final class RocketryEntitiesSpace {

    public static final Reg.EntityHandle<RideableRocketEntity> RIDEABLE_ROCKET =
            HbmSpace.REG.entity(
                    "entity_rideable_rocket",
                    () ->
                            EntityType.Builder.<RideableRocketEntity>of(
                                            RideableRocketEntity::new, MobCategory.MISC)
                                    .noLootTable()
                                    .sized(2F, 8F)
                                    .clientTrackingRange(63)
                                    .updateInterval(1)
                                    .build(key("entity_rideable_rocket")));

    public static final Reg.EntityHandle<EntityRideableRocketDummy> RIDEABLE_ROCKET_DUMMY =
            HbmSpace.REG.entity(
                    "entity_rideable_rocket_dummy",
                    () ->
                            EntityType.Builder.<EntityRideableRocketDummy>of(
                                            EntityRideableRocketDummy::new, MobCategory.MISC)
                                    .noLootTable()
                                    .noSave()
                                    .sized(4F, 2.5F)
                                    .clientTrackingRange(63)
                                    .updateInterval(1)
                                    .build(key("entity_rideable_rocket_dummy")));

    public static final Reg.EntityHandle<CombatDropPodEntity> COMBAT_DROP_POD =
            HbmSpace.REG.entity(
                    "entity_combat_pod",
                    () ->
                            EntityType.Builder.<CombatDropPodEntity>of(
                                            CombatDropPodEntity::new, MobCategory.MISC)
                                    .noLootTable()
                                    .sized(0.25F, 0.25F)
                                    .fireImmune()
                                    .clientTrackingRange(63)
                                    .updateInterval(1)
                                    .build(key("entity_combat_pod")));

    static {
    }

    private static ResourceKey<EntityType<?>> key(String name) {
        return ResourceKey.create(
                Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(HbmSpace.MOD_ID, name));
    }

    private RocketryEntitiesSpace() {}
}
