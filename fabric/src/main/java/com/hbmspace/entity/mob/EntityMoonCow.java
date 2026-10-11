// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.entity.mob;

import com.hbm.api.environment.ISuffocationImmune;
import com.hbm.items.ModItems;
import com.hbmspace.entity.MobEntitiesSpace;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.animal.cow.AbstractCow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import org.jspecify.annotations.Nullable;

public class EntityMoonCow extends AbstractCow implements ISuffocationImmune {

    public EntityMoonCow(EntityType<? extends EntityMoonCow> type, Level level) {
        super(type, level);
        moveControl = new MoonWalkControl(this);
    }

    @Override
    public @Nullable EntityMoonCow getBreedOffspring(ServerLevel level, AgeableMob partner) {
        return MobEntitiesSpace.MOON_COW.get().create(level, EntitySpawnReason.BREEDING);
    }

    @Override
    public EntityDimensions getDefaultDimensions(Pose pose) {
        return getType().getDimensions().scale(getAgeScale());
    }

    @Override
    protected float getSoundVolume() {
        return 0.1F;
    }

    @Override
    public boolean checkSpawnRules(LevelAccessor level, EntitySpawnReason spawnReason) {
        return spawnReason == EntitySpawnReason.CHUNK_GENERATION
                || super.checkSpawnRules(level, spawnReason);
    }

    @Override
    protected void dropCustomDeathLoot(
            ServerLevel level, DamageSource source, boolean killedByPlayer) {
        super.dropCustomDeathLoot(level, source, killedByPlayer);
        int looting = MobDrops.looting(level, source);
        int leather = random.nextInt(3) + random.nextInt(1 + looting);
        for (int i = 0; i < leather; i++) spawnAtLocation(level, new ItemStack(Items.LEATHER));
        int cheese = random.nextInt(3) + 1 + random.nextInt(1 + looting);
        for (int i = 0; i < cheese; i++)
            spawnAtLocation(level, new ItemStack(ModItems.CHEESE.get()));
    }
}
