// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.entity;

import com.hbm.packet.PayloadHandlerThread;
import com.hbm.platform.NeoForgeNetwork;
import com.hbm.registration.Reg;
import com.hbmspace.HbmSpace;
import com.hbmspace.entity.missile.EntityCombatDropPod;
import com.hbmspace.entity.missile.EntityRideableRocket.EntityRideableRocketDummy;
import com.hbmspace.entity.missile.EntityRideableRocket;
import com.hbmspace.entity.missile.RocketTransferPayload;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

public final class RocketryEntitiesSpace {

    public static final Reg.EntityHandle<EntityRideableRocket> RIDEABLE_ROCKET =
            HbmSpace.REG.entity(
                    "entity_rideable_rocket",
                    () ->
                            EntityType.Builder.<EntityRideableRocket>of(
                                            EntityRideableRocket::new, MobCategory.MISC)
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

    public static final Reg.EntityHandle<EntityCombatDropPod> COMBAT_DROP_POD =
            HbmSpace.REG.entity(
                    "entity_combat_pod",
                    () ->
                            EntityType.Builder.<EntityCombatDropPod>of(
                                            EntityCombatDropPod::new, MobCategory.MISC)
                                    .noLootTable()
                                    .sized(0.25F, 0.25F)
                                    .fireImmune()
                                    .clientTrackingRange(63)
                                    .updateInterval(1)
                                    .build(key("entity_combat_pod")));

    static {
        NeoForgeNetwork.clientbound.add(
                new NeoForgeNetwork.Registration<>(
                        RocketTransferPayload.TYPE,
                        RocketTransferPayload.STREAM_CODEC,
                        RocketTransferPayload::handle,
                        PayloadHandlerThread.MAIN));
    }

    private static ResourceKey<EntityType<?>> key(String name) {
        return ResourceKey.create(
                Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(HbmSpace.MOD_ID, name));
    }

    private RocketryEntitiesSpace() {}
}
