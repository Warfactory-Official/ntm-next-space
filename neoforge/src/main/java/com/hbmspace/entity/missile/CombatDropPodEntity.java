/*
 * SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
 * SPDX-FileCopyrightText: 2026 JamesH_2
 * SPDX-License-Identifier: LGPL-3.0-only
 *
 * Copyright (C) 2026 movblock. All rights reserved.
 * Copyright (C) 2026 JamesH_2. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License version 3 only,
 * as published by the Free Software Foundation.
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this code. If not, see <https://www.gnu.org/licenses/>.
 *
 * Under GPLv3 §7(c) and §7(e), these additional terms apply to LGPL-covered
 * material authored by movblock:
 *
 * - the license grants no rights to the name "NTM NEXT: Space";
 * - a modified version must not be named NTM NEXT: Space or presented as
 *   the official release, and must be marked as different from it.
 *
 * Accurately identifying a work as derived from NTM NEXT: Space does not
 * violate these additional terms, provided it is clearly distinguished from
 * the official release and does not imply endorsement. Ports are encouraged
 * to acknowledge NTM NEXT: Space as their upstream project and provide a
 * link to it.
 */

package com.hbmspace.entity.missile;

import com.hbm.explosion.BlastSmoke;
import com.hbm.sound.WorldSounds;
import com.hbmspace.blockentity.bomb.CombatDropPodBlockEntity;
import com.hbmspace.blocks.RocketryBlocksSpace;
import com.hbmspace.sound.RocketrySoundsSpace;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

public class CombatDropPodEntity extends Entity {

    private static final EntityDataAccessor<Integer> COLOR =
            SynchedEntityData.defineId(CombatDropPodEntity.class, EntityDataSerializers.INT);

    private @Nullable CompoundTag entityType;
    private int amount;
    private int color;

    public CombatDropPodEntity(EntityType<? extends CombatDropPodEntity> type, Level level) {
        super(type, level);
    }

    public void setPayload(CompoundTag entityData, int amount, int color) {
        this.entityType = entityData;
        this.amount = amount;
        setColor(color);
    }

    public void setColor(int color) {
        this.color = color;
        entityData.set(COLOR, color);
    }

    public int getColor() {
        return entityData.get(COLOR);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(COLOR, 0);
    }

    @Override
    public boolean fireImmune() {
        return true;
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        return false;
    }

    @Override
    public void tick() {
        setDeltaMovement(0, -1, 0);
        this.xo = this.xOld = getX();
        this.yo = this.yOld = getY();
        this.zo = this.zOld = getZ();

        for (int i = 0; i < 4; i++) {
            BlockPos pos = BlockPos.containing(getX(), getY() + 1, getZ());
            if (!level().isClientSide() && !level().getBlockState(pos).isAir()) {
                land(pos);
                return;
            }
            setPos(getX(), getY() - 1, getZ());
        }
    }

    private void land(BlockPos pos) {
        BlastSmoke.cloud(level(), getX(), getY() + 1, getZ(), 50);
        WorldSounds.play(
                level(),
                null,
                getX(),
                getY(),
                getZ(),
                RocketrySoundsSpace.HATCH_IMPACT.get(),
                SoundSource.BLOCKS,
                10.0F,
                0.5F + random.nextFloat() * 0.5F);

        BlockState pod = RocketryBlocksSpace.COMBAT_DROP.get().defaultBlockState();
        level().setBlockAndUpdate(pos, pod);
        if (entityType != null
                && level().getBlockEntity(pos) instanceof CombatDropPodBlockEntity capsule) {
            capsule.setPayload(entityType, amount, color);
        }
        discard();
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return distance < 500000;
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        entityType = input.read("entityType", CompoundTag.CODEC).orElse(null);
        amount = input.getIntOr("amount", 0);
        setColor(input.getIntOr("color", 0));
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        output.putInt("color", color);
        output.putInt("amount", amount);
        if (entityType != null) output.store("entityType", CompoundTag.CODEC, entityType);
    }
}
