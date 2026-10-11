// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.entity.mob;

import com.hbm.itempool.ItemPools;
import com.hbm.lib.Library;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;

public class EntityScrapFish extends EntityFish implements IEntityEnumMulti {

    public enum ScrapFish {
        STEEL,
        ALUMINIUM,
        ISOTOPE,
        CADMIUM,
        TECH,
        BLOOD,
        HORROR,
    }

    private static final EntityDataAccessor<Byte> TYPE =
            SynchedEntityData.defineId(EntityScrapFish.class, EntityDataSerializers.BYTE);
    private static final ResourceKey<LootTable> MACHINE_PARTS =
            ResourceKey.create(
                    Registries.LOOT_TABLE,
                    Library.id("chests/" + ItemPools.slug(ItemPools.POOL_MACHINE_PARTS)));

    public EntityScrapFish(EntityType<? extends EntityScrapFish> type, Level level) {
        super(type, level);
        if (!level.isClientSide())
            entityData.set(TYPE, (byte) random.nextInt(ScrapFish.values().length));
    }

    @Override
    protected double swimSpeed() {
        return 0.8D;
    }

    @Override
    protected float watchDistance() {
        return 4.0F;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(TYPE, (byte) 0);
    }

    @Override
    public ScrapFish getEnum() {
        return ScrapFish.values()[entityData.get(TYPE)];
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putByte("type", entityData.get(TYPE));
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        entityData.set(TYPE, input.getByteOr("type", entityData.get(TYPE)));
    }

    @Override
    protected void dropCustomDeathLoot(
            ServerLevel level, DamageSource source, boolean killedByPlayer) {
        super.dropCustomDeathLoot(level, source, killedByPlayer);
        LootTable parts = level.getServer().reloadableRegistries().getLootTable(MACHINE_PARTS);
        int stacks = random.nextInt(3 + MobDrops.looting(level, source)) + 1;
        for (int i = 0; i < stacks; i++) {
            for (ItemStack stack :
                    parts.getRandomItems(
                            new LootParams.Builder(level).create(LootContextParamSets.EMPTY))) {
                spawnAtLocation(level, stack);
            }
        }
    }
}
