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

import com.hbm.api.environment.SuffocationImmune;
import com.hbmspace.entity.MobEntitiesSpace;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.animal.cow.AbstractCow;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import org.jspecify.annotations.Nullable;

public class MoonCowEntity extends AbstractCow implements SuffocationImmune {

    public MoonCowEntity(EntityType<? extends MoonCowEntity> type, Level level) {
        super(type, level);
        moveControl = new MoonWalkControl(this);
    }

    @Override
    public @Nullable MoonCowEntity getBreedOffspring(ServerLevel level, AgeableMob partner) {
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
}
