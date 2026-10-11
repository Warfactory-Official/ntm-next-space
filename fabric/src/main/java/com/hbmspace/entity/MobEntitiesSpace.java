// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.entity;

import com.hbm.items.special.ItemSpawnEgg;
import com.hbm.registration.FabricRegistrar;
import com.hbm.registration.Reg;
import com.hbmspace.HbmSpace;
import com.hbmspace.blocks.TerrainBlocksSpace;
import com.hbmspace.data.SpaceData;
import com.hbmspace.dim.moon.BiomeGenMoon;
import com.hbmspace.entity.mob.EntityCreeperFlesh;
import com.hbmspace.entity.mob.EntityDepthSquid;
import com.hbmspace.entity.mob.EntityFish;
import com.hbmspace.entity.mob.EntityMoonCow;
import com.hbmspace.entity.mob.EntityScrapFish;
import com.hbmspace.entity.mob.EntityScutterfish;
import com.hbmspace.entity.mob.EntityScuttlecrab;
import com.hbmspace.entity.mob.EntitySifterEel;
import com.hbmspace.entity.mob.EntityTankbot;
import com.hbmspace.entity.mob.EntityWarBehemoth;
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
    public static final Reg.EntityHandle<EntityWarBehemoth> WAR_BEHEMOTH =
            mob("entity_war_behemoth", EntityWarBehemoth::new, MobCategory.MONSTER, 1.75F, 6.35F);
    public static final Reg.EntityHandle<EntityTankbot> TANKBOT =
            mob(
                    "entity_tankbot",
                    () ->
                            EntityType.Builder.of(EntityTankbot::new, MobCategory.MONSTER)
                                    .sized(2.45F, 3.25F)
                                    .fireImmune());
    public static final Reg.EntityHandle<EntityCreeperFlesh> CREEPER_FLESH =
            mob(
                    "entity_mob_flesh_creeper",
                    EntityCreeperFlesh::new,
                    MobCategory.MONSTER,
                    0.6F,
                    1.7F);
    public static final Reg.EntityHandle<EntityMoonCow> MOON_COW =
            mob("entity_moon_cow", EntityMoonCow::new, MobCategory.CREATURE, 0.9F, 1.3F);
    public static final Reg.EntityHandle<EntityScutterfish> SCUTTERFISH =
            mob(
                    "entity_scutterfish",
                    EntityScutterfish::new,
                    MobCategory.WATER_CREATURE,
                    2.0F,
                    2.0F);
    public static final Reg.EntityHandle<EntityScuttlecrab> SCUTTLECRAB =
            mob(
                    "entity_scuttlecrab",
                    EntityScuttlecrab::new,
                    MobCategory.WATER_CREATURE,
                    0.6F,
                    1.8F);
    public static final Reg.EntityHandle<EntityDepthSquid> DEPTH_SQUID =
            mob(
                    "entity_depthsquid",
                    EntityDepthSquid::new,
                    MobCategory.WATER_CREATURE,
                    0.95F,
                    0.95F);
    public static final Reg.EntityHandle<EntityScrapFish> SCRAP_FISH =
            mob("entity_scrapfish", EntityScrapFish::new, MobCategory.WATER_CREATURE, 2.0F, 2.0F);
    public static final Reg.EntityHandle<EntitySifterEel> SIFTER_EEL =
            mob("entity_siftereel", EntitySifterEel::new, MobCategory.WATER_CREATURE, 2.0F, 2.0F);

    public static final List<Reg.Handle<ItemSpawnEgg>> EGGS =
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
        FabricRegistrar.registerLivingAttributes(WAR_BEHEMOTH, EntityWarBehemoth::createAttributes);
        FabricRegistrar.registerLivingAttributes(TANKBOT, EntityTankbot::createAttributes);
        FabricRegistrar.registerLivingAttributes(CREEPER_FLESH, Creeper::createAttributes);
        FabricRegistrar.registerLivingAttributes(MOON_COW, AbstractCow::createAttributes);
        FabricRegistrar.registerLivingAttributes(SCUTTERFISH, EntityFish::createAttributes);
        FabricRegistrar.registerLivingAttributes(SCUTTLECRAB, EntityScuttlecrab::createAttributes);
        FabricRegistrar.registerLivingAttributes(DEPTH_SQUID, Squid::createAttributes);
        FabricRegistrar.registerLivingAttributes(SCRAP_FISH, EntityFish::createAttributes);
        FabricRegistrar.registerLivingAttributes(SIFTER_EEL, EntityFish::createAttributes);
        FabricRegistrar.registerSpawnPlacement(
                WAR_BEHEMOTH,
                SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                MobEntitiesSpace::warMachineSpawns);
        FabricRegistrar.registerSpawnPlacement(
                TANKBOT,
                SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                MobEntitiesSpace::warMachineSpawns);
        FabricRegistrar.registerSpawnPlacement(
                CREEPER_FLESH,
                SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                MobEntitiesSpace::fleshCreeperSpawns);
        FabricRegistrar.registerSpawnPlacement(
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
                                .noLootTable()
                                .clientTrackingRange(5)
                                .updateInterval(3)
                                .build(
                                        ResourceKey.create(
                                                Registries.ENTITY_TYPE,
                                                Identifier.fromNamespaceAndPath(
                                                        HbmSpace.MOD_ID, name))));
    }

    private static <T extends Mob> void inWater(Reg.EntityHandle<T> type) {
        FabricRegistrar.registerSpawnPlacement(
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
            EntityType<EntityCreeperFlesh> type,
            ServerLevelAccessor level,
            EntitySpawnReason reason,
            BlockPos pos,
            RandomSource random) {
        return (reason != EntitySpawnReason.NATURAL
                        || level.getBlockState(pos.below()).is(TerrainBlocksSpace.TUMOR.get()))
                && Monster.checkMonsterSpawnRules(type, level, reason, pos, random);
    }

    private static boolean moonCowSpawns(
            EntityType<EntityMoonCow> type,
            ServerLevelAccessor level,
            EntitySpawnReason reason,
            BlockPos pos,
            RandomSource random) {
        if (!SpaceData.ENABLE_MUN_COWS.get() && level.getBiome(pos).is(BiomeGenMoon.biome.key))
            return false;
        return reason == EntitySpawnReason.CHUNK_GENERATION
                || Animal.checkAnimalSpawnRules(type, level, reason, pos, random);
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
