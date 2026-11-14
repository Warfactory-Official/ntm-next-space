/*
 * SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
 * SPDX-FileCopyrightText: 2025 George Paton
 * SPDX-License-Identifier: LGPL-3.0-only
 *
 * Copyright (C) 2026 movblock. All rights reserved.
 * Copyright (C) 2025 George Paton. All rights reserved.
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

package com.hbmspace.entity.mob;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.animal.fish.WaterAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;

public class ScuttlecrabEntity extends WaterAnimal implements EntityVariantProvider {

    public enum Scuttlecrab {
        TROPICAL,
        CLAYINFUSED,
    }

    private static final EntityDataAccessor<Byte> TYPE =
            SynchedEntityData.defineId(ScuttlecrabEntity.class, EntityDataSerializers.BYTE);

    private float crabDirection = 1.0F;
    private int switchTimer;

    public ScuttlecrabEntity(EntityType<? extends ScuttlecrabEntity> type, Level level) {
        super(type, level);
        if (!level.isClientSide()) {
            entityData.set(
                    TYPE,
                    (byte)
                            (random.nextInt(8) == 0
                                            ? Scuttlecrab.CLAYINFUSED
                                            : Scuttlecrab.TROPICAL)
                                    .ordinal());
        }
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes().add(Attributes.MOVEMENT_SPEED, Math.sqrt(0.07D));
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new RandomStrollGoal(this, 1.0D));
        goalSelector.addGoal(1, new LookAtPlayerGoal(this, Player.class, 8.0F));
        goalSelector.addGoal(2, new RandomLookAroundGoal(this));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(TYPE, (byte) 0);
    }

    @Override
    public Scuttlecrab getEnum() {
        return Scuttlecrab.values()[entityData.get(TYPE)];
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
    protected boolean shouldTravelInFluid(FluidState fluidState) {
        return false;
    }

    @Override
    public void travel(Vec3 input) {
        switchTimer--;
        if (switchTimer <= 0) {
            crabDirection *= -1.0F;
            switchTimer = 20 + random.nextInt(80);
        }
        super.travel(new Vec3(input.z * crabDirection, input.y, input.x));
    }
}
