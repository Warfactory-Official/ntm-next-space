// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.entity.mob;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class EntitySifterEel extends EntityFish implements IEntityEnumMulti {

    public enum SifterEel {
        PLAIN,
        FAST,
        EXOTIC,
        PHASED,
        ELEMENTAL,
        PERFECT,
    }

    private static final EntityDataAccessor<Byte> TYPE =
            SynchedEntityData.defineId(EntitySifterEel.class, EntityDataSerializers.BYTE);

    public EntitySifterEel(EntityType<? extends EntitySifterEel> type, Level level) {
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
