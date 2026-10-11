// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.entity;

import com.hbm.registration.Reg;
import com.hbmspace.HbmSpace;
import com.hbmspace.entity.logic.EntityAntimatterBlast;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

public final class BombEntitiesSpace {
    public static final Reg.EntityHandle<EntityAntimatterBlast> ANTIMATTER_BLAST =
            HbmSpace.REG.entity(
                    "entity_antimatter",
                    () ->
                            EntityType.Builder.<EntityAntimatterBlast>of(
                                            EntityAntimatterBlast::new, MobCategory.MISC)
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
