// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.entity;

import com.hbm.items.special.ItemSpawnEgg;
import com.hbm.packet.PayloadHandlerThread;
import com.hbm.platform.NeoForgeNetwork;
import com.hbm.registration.NeoForgeRegistrar;
import com.hbm.registration.Reg;
import com.hbmspace.HbmSpace;
import com.hbmspace.entity.mob.EntityBFAngel;
import com.hbmspace.entity.mob.RedDustBurstPayload;
import com.hbmspace.entity.mob.siege.EntitySiegeCraft;
import com.hbmspace.entity.mob.siege.EntitySiegeUFO;
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
    public static final Reg.EntityHandle<EntitySiegeUFO> SIEGE_UFO =
            mob(
                    "entity_meme_ufo",
                    EntityType.Builder.of(EntitySiegeUFO::new, MobCategory.MONSTER)
                            .sized(1.5F, 1F));
    public static final Reg.EntityHandle<EntitySiegeCraft> SIEGE_CRAFT =
            mob(
                    "entity_micro_ufo",
                    EntityType.Builder.of(EntitySiegeCraft::new, MobCategory.MONSTER)
                            .sized(7F, 1F)
                            .fireImmune());
    public static final Reg.EntityHandle<EntityBFAngel> BF_ANGEL =
            mob(
                    "entity_bfa",
                    EntityType.Builder.of(EntityBFAngel::new, MobCategory.MONSTER)
                            .sized(5F, 4F)
                            .fireImmune());

    public static final Reg.Handle<ItemSpawnEgg> SIEGE_UFO_EGG = egg(SIEGE_UFO, 0x303030, 0x800000);
    public static final Reg.Handle<ItemSpawnEgg> BF_ANGEL_EGG = egg(BF_ANGEL, 0x204131, 0x75CE00);
    public static final Reg.Handle<ItemSpawnEgg> SIEGE_CRAFT_EGG =
            egg(SIEGE_CRAFT, 0x5B963E, 0xC0B286);

    static {
        NeoForgeRegistrar.registerLivingAttributes(SIEGE_UFO, EntitySiegeUFO::createAttributes);
        NeoForgeRegistrar.registerLivingAttributes(SIEGE_CRAFT, EntitySiegeCraft::createAttributes);
        NeoForgeRegistrar.registerLivingAttributes(BF_ANGEL, EntityBFAngel::createAttributes);
        NeoForgeNetwork.clientbound.add(
                new NeoForgeNetwork.Registration<>(
                        RedDustBurstPayload.TYPE,
                        RedDustBurstPayload.STREAM_CODEC,
                        RedDustBurstPayload::handle,
                        PayloadHandlerThread.MAIN));
    }

    private MobsSpace() {}

    private static <T extends Mob> Reg.EntityHandle<T> mob(
            String name, EntityType.Builder<T> builder) {
        return HbmSpace.REG.entity(
                name,
                () ->
                        builder.noLootTable()
                                .clientTrackingRange(5)
                                .updateInterval(3)
                                .build(
                                        ResourceKey.create(
                                                Registries.ENTITY_TYPE,
                                                Identifier.fromNamespaceAndPath(
                                                        HbmSpace.MOD_ID, name))));
    }

    private static Reg.Handle<ItemSpawnEgg> egg(
            Reg.EntityHandle<?> type, int primary, int secondary) {
        return HbmSpace.REG.item(
                type.id().getPath() + "_spawn_egg",
                ItemSpawnEgg::new,
                () ->
                        new Item.Properties()
                                .delayedComponent(
                                        DataComponents.ENTITY_DATA,
                                        context ->
                                                TypedEntityData.of(type.get(), new CompoundTag())));
    }
}
