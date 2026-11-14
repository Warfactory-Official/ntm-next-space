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
import com.hbmspace.blocks.TerrainBlocksSpace;
import com.hbmspace.data.SpaceData;
import com.hbmspace.dim.moon.BiomeGenMoon;
import com.hbmspace.entity.mob.CreeperFleshEntity;
import com.hbmspace.entity.mob.DepthSquidEntity;
import com.hbmspace.entity.mob.FishEntity;
import com.hbmspace.entity.mob.MoonCowEntity;
import com.hbmspace.entity.mob.ScrapFishEntity;
import com.hbmspace.entity.mob.ScutterfishEntity;
import com.hbmspace.entity.mob.ScuttlecrabEntity;
import com.hbmspace.entity.mob.SifterEelEntity;
import com.hbmspace.entity.mob.TankbotEntity;
import com.hbmspace.entity.mob.WarBehemothEntity;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.cow.AbstractCow;
import net.minecraft.world.entity.animal.squid.Squid;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.TypedEntityData;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.levelgen.Heightmap;

public final class MobEntitiesSpace {
    public static final Reg.EntityHandle<WarBehemothEntity> WAR_BEHEMOTH =
            mob(
                    "entity_war_behemoth",
                    () ->
                            EntityType.Builder.of(WarBehemothEntity::new, MobCategory.MONSTER)
                                    .sized(1.75F, 6.35F)
                                    .noLootTable());
    public static final Reg.EntityHandle<TankbotEntity> TANKBOT =
            mob(
                    "entity_tankbot",
                    () ->
                            EntityType.Builder.of(TankbotEntity::new, MobCategory.MONSTER)
                                    .sized(2.45F, 3.25F)
                                    .fireImmune()
                                    .noLootTable());
    public static final Reg.EntityHandle<CreeperFleshEntity> CREEPER_FLESH =
            mob(
                    "entity_mob_flesh_creeper",
                    CreeperFleshEntity::new,
                    MobCategory.MONSTER,
                    0.6F,
                    1.7F);
    public static final Reg.EntityHandle<MoonCowEntity> MOON_COW =
            mob("entity_moon_cow", MoonCowEntity::new, MobCategory.CREATURE, 0.9F, 1.3F);
    public static final Reg.EntityHandle<ScutterfishEntity> SCUTTERFISH =
            mob(
                    "entity_scutterfish",
                    ScutterfishEntity::new,
                    MobCategory.WATER_CREATURE,
                    2.0F,
                    2.0F);
    public static final Reg.EntityHandle<ScuttlecrabEntity> SCUTTLECRAB =
            mob(
                    "entity_scuttlecrab",
                    ScuttlecrabEntity::new,
                    MobCategory.WATER_CREATURE,
                    0.6F,
                    1.8F);
    public static final Reg.EntityHandle<DepthSquidEntity> DEPTH_SQUID =
            mob(
                    "entity_depthsquid",
                    DepthSquidEntity::new,
                    MobCategory.WATER_CREATURE,
                    0.95F,
                    0.95F);
    public static final Reg.EntityHandle<ScrapFishEntity> SCRAP_FISH =
            mob("entity_scrapfish", ScrapFishEntity::new, MobCategory.WATER_CREATURE, 2.0F, 2.0F);
    public static final Reg.EntityHandle<SifterEelEntity> SIFTER_EEL =
            mob(
                    "entity_siftereel",
                    () ->
                            EntityType.Builder.of(SifterEelEntity::new, MobCategory.WATER_CREATURE)
                                    .sized(2.0F, 2.0F)
                                    .noLootTable());

    public static final List<Reg.Handle<SpawnEggItem>> EGGS =
            List.of(
                    egg(WAR_BEHEMOTH, 0x204131, 0x75CE00),
                    egg(TANKBOT, 0x204131, 0x75CE00),
                    egg(CREEPER_FLESH, 0xFF0000, 0xD65337),
                    egg(MOON_COW, 0xECD376, 0x262A44),
                    egg(SCUTTERFISH, 0xC8C9CD, 0x858894),
                    egg(SCUTTLECRAB, 0xF17951, 0xEDDABB),
                    egg(DEPTH_SQUID, 0x00B4DF, 0x016085),
                    egg(SCRAP_FISH, 0xDF9835, 0x510E13),
                    egg(SIFTER_EEL, 0x5B963E, 0xC0B286));

    static {
        Registrar.registerLivingAttributes(WAR_BEHEMOTH, WarBehemothEntity::createAttributes);
        Registrar.registerLivingAttributes(TANKBOT, TankbotEntity::createAttributes);
        Registrar.registerLivingAttributes(CREEPER_FLESH, Creeper::createAttributes);
        Registrar.registerLivingAttributes(MOON_COW, AbstractCow::createAttributes);
        Registrar.registerLivingAttributes(SCUTTERFISH, FishEntity::createAttributes);
        Registrar.registerLivingAttributes(SCUTTLECRAB, ScuttlecrabEntity::createAttributes);
        Registrar.registerLivingAttributes(DEPTH_SQUID, Squid::createAttributes);
        Registrar.registerLivingAttributes(SCRAP_FISH, FishEntity::createAttributes);
        Registrar.registerLivingAttributes(SIFTER_EEL, FishEntity::createAttributes);
        Registrar.registerSpawnPlacement(
                WAR_BEHEMOTH,
                SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                MobEntitiesSpace::warMachineSpawns);
        Registrar.registerSpawnPlacement(
                TANKBOT,
                SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                MobEntitiesSpace::warMachineSpawns);
        Registrar.registerSpawnPlacement(
                CREEPER_FLESH,
                SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                MobEntitiesSpace::fleshCreeperSpawns);
        Registrar.registerSpawnPlacement(
                MOON_COW,
                SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                MobEntitiesSpace::moonCowSpawns);
        inWater(SCUTTERFISH);
        inWater(SCUTTLECRAB);
        inWater(DEPTH_SQUID);
        inWater(SCRAP_FISH);
        inWater(SIFTER_EEL);
    }

    private MobEntitiesSpace() {}

    private static <T extends Mob> Reg.EntityHandle<T> mob(
            String name,
            EntityType.EntityFactory<T> factory,
            MobCategory category,
            float width,
            float height) {
        return mob(name, () -> EntityType.Builder.of(factory, category).sized(width, height));
    }

    private static <T extends Mob> Reg.EntityHandle<T> mob(
            String name, Supplier<EntityType.Builder<T>> builder) {
        return HbmSpace.REG.entity(
                name,
                () ->
                        builder.get()
                                .clientTrackingRange(5)
                                .updateInterval(3)
                                .build(
                                        ResourceKey.create(
                                                Registries.ENTITY_TYPE,
                                                Identifier.fromNamespaceAndPath(
                                                        HbmSpace.MOD_ID, name))));
    }

    private static <T extends Mob> void inWater(Reg.EntityHandle<T> type) {
        Registrar.registerSpawnPlacement(
                type,
                SpawnPlacementTypes.IN_WATER,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                (entityType, level, reason, pos, random) -> true);
    }

    private static <T extends Monster> boolean warMachineSpawns(
            EntityType<T> type,
            ServerLevelAccessor level,
            EntitySpawnReason reason,
            BlockPos pos,
            RandomSource random) {
        return reason == EntitySpawnReason.CHUNK_GENERATION
                || Monster.checkMonsterSpawnRules(type, level, reason, pos, random);
    }

    private static boolean fleshCreeperSpawns(
            EntityType<CreeperFleshEntity> type,
            ServerLevelAccessor level,
            EntitySpawnReason reason,
            BlockPos pos,
            RandomSource random) {
        return (reason != EntitySpawnReason.NATURAL
                        || level.getBlockState(pos.below()).is(TerrainBlocksSpace.TUMOR.get()))
                && Monster.checkMonsterSpawnRules(type, level, reason, pos, random);
    }

    private static boolean moonCowSpawns(
            EntityType<MoonCowEntity> type,
            ServerLevelAccessor level,
            EntitySpawnReason reason,
            BlockPos pos,
            RandomSource random) {
        if (!SpaceData.ENABLE_MUN_COWS.get() && level.getBiome(pos).is(BiomeGenMoon.biome.key))
            return false;
        return reason == EntitySpawnReason.CHUNK_GENERATION
                || Animal.checkAnimalSpawnRules(type, level, reason, pos, random);
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
