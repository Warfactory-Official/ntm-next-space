// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.entity;

import com.hbm.registration.Reg;
import com.hbmspace.HbmSpace;
import com.hbmspace.entity.effect.EntityDepress;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

public final class AtmosphereEntitiesSpace {

    public static final Reg.EntityHandle<EntityDepress> DEPRESS =
            HbmSpace.REG.entity(
                    "entity_depress",
                    () ->
                            EntityType.Builder.<EntityDepress>of(
                                            EntityDepress::new, MobCategory.MISC)
                                    .noLootTable()
                                    .fireImmune()
                                    .clientTrackingRange(16)
                                    .updateInterval(1)
                                    .build(
                                            ResourceKey.create(
                                                    Registries.ENTITY_TYPE,
                                                    Identifier.fromNamespaceAndPath(
                                                            HbmSpace.MOD_ID, "entity_depress"))));

    private AtmosphereEntitiesSpace() {}
}
