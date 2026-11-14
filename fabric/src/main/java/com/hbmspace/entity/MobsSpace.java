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

import com.hbm.items.special.SpawnEggItem;
import com.hbm.registration.Reg;
import com.hbm.registration.Registrar;
import com.hbmspace.HbmSpace;
import com.hbmspace.entity.mob.BFAngelEntity;
import com.hbmspace.entity.mob.siege.SiegeCraftEntity;
import com.hbmspace.entity.mob.siege.SiegeUFOEntity;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.TypedEntityData;

public final class MobsSpace {
    public static final Reg.EntityHandle<SiegeUFOEntity> SIEGE_UFO =
            mob(
                    "entity_meme_ufo",
                    EntityType.Builder.of(SiegeUFOEntity::new, MobCategory.MONSTER)
                            .sized(1.5F, 1F));
    public static final Reg.EntityHandle<SiegeCraftEntity> SIEGE_CRAFT =
            mob(
                    "entity_micro_ufo",
                    EntityType.Builder.of(SiegeCraftEntity::new, MobCategory.MONSTER)
                            .sized(7F, 1F)
                            .fireImmune());
    public static final Reg.EntityHandle<BFAngelEntity> BF_ANGEL =
            mob(
                    "entity_bfa",
                    EntityType.Builder.of(BFAngelEntity::new, MobCategory.MONSTER)
                            .sized(5F, 4F)
                            .fireImmune());

    public static final Reg.Handle<SpawnEggItem> SIEGE_UFO_EGG = egg(SIEGE_UFO, 0x303030, 0x800000);
    public static final Reg.Handle<SpawnEggItem> BF_ANGEL_EGG = egg(BF_ANGEL, 0x204131, 0x75CE00);
    public static final Reg.Handle<SpawnEggItem> SIEGE_CRAFT_EGG =
            egg(SIEGE_CRAFT, 0x5B963E, 0xC0B286);

    static {
        Registrar.registerLivingAttributes(SIEGE_UFO, SiegeUFOEntity::createAttributes);
        Registrar.registerLivingAttributes(SIEGE_CRAFT, SiegeCraftEntity::createAttributes);
        Registrar.registerLivingAttributes(BF_ANGEL, BFAngelEntity::createAttributes);
    }

    private MobsSpace() {}

    private static <T extends Mob> Reg.EntityHandle<T> mob(
            String name, EntityType.Builder<T> builder) {
        return HbmSpace.REG.entity(
                name,
                () ->
                        builder.clientTrackingRange(5)
                                .updateInterval(3)
                                .build(
                                        ResourceKey.create(
                                                Registries.ENTITY_TYPE,
                                                Identifier.fromNamespaceAndPath(
                                                        HbmSpace.MOD_ID, name))));
    }

    private static Reg.Handle<SpawnEggItem> egg(
            Reg.EntityHandle<?> type, int primary, int secondary) {
        return HbmSpace.REG.item(
                type.id().getPath() + "_spawn_egg",
                SpawnEggItem::new,
                () ->
                        new Item.Properties()
                                .delayedComponent(
                                        DataComponents.ENTITY_DATA,
                                        context ->
                                                TypedEntityData.of(type.get(), new CompoundTag())));
    }
}
