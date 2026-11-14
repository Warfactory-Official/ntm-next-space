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

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class SifterEelEntity extends FishEntity implements EntityVariantProvider {

    public enum SifterEel {
        PLAIN,
        FAST,
        EXOTIC,
        PHASED,
        ELEMENTAL,
        PERFECT,
    }

    private static final EntityDataAccessor<Byte> TYPE =
            SynchedEntityData.defineId(SifterEelEntity.class, EntityDataSerializers.BYTE);

    public SifterEelEntity(EntityType<? extends SifterEelEntity> type, Level level) {
        super(type, level);
        if (!level.isClientSide())
            entityData.set(TYPE, (byte) random.nextInt(SifterEel.values().length));
    }

    @Override
    protected double swimSpeed() {
        return 1.8D;
    }

    @Override
    protected float watchDistance() {
        return 8.0F;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(TYPE, (byte) 0);
    }

    @Override
    public SifterEel getEnum() {
        return SifterEel.values()[entityData.get(TYPE)];
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
}
