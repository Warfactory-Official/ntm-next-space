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

package com.hbmspace.entity.mob;

import com.hbmspace.entity.MobEntitiesSpace;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.animal.squid.Squid;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

public class DepthSquidEntity extends Squid implements EntityVariantProvider {

    public enum DepthSquid {
        AQUA,
        BLACK,
        ORANGE,
        OURPLE,
        RED,
        SILVER,
        VICIOUS,
    }

    private static final EntityDataAccessor<Byte> TYPE =
            SynchedEntityData.defineId(DepthSquidEntity.class, EntityDataSerializers.BYTE);

    public DepthSquidEntity(EntityType<? extends DepthSquidEntity> type, Level level) {
        super(type, level);
        if (!level.isClientSide())
            entityData.set(TYPE, (byte) random.nextInt(DepthSquid.values().length));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(TYPE, (byte) 0);
    }

    @Override
    public DepthSquid getEnum() {
        return DepthSquid.values()[entityData.get(TYPE)];
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
    public boolean checkSpawnRules(LevelAccessor level, EntitySpawnReason spawnReason) {
        return getY() > 45.0D && getY() < 63.0D && super.checkSpawnRules(level, spawnReason);
    }

    @Override
    public @Nullable AgeableMob getBreedOffspring(ServerLevel level, AgeableMob partner) {
        return MobEntitiesSpace.DEPTH_SQUID.get().create(level, EntitySpawnReason.BREEDING);
    }

    @Override
    public @Nullable SpawnGroupData finalizeSpawn(
            ServerLevelAccessor level,
            DifficultyInstance difficulty,
            EntitySpawnReason spawnReason,
            @Nullable SpawnGroupData groupData) {
        return super.finalizeSpawn(
                level,
                difficulty,
                spawnReason,
                groupData != null ? groupData : new AgeableMob.AgeableMobGroupData(false));
    }
}
