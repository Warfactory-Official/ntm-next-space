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

import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.animal.fish.WaterAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public abstract class FishEntity extends WaterAnimal {

    private @Nullable Vec3 swimTarget;

    protected FishEntity(EntityType<? extends FishEntity> type, Level level) {
        super(type, level);
        getNavigation().setCanFloat(true);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 15.0D)
                .add(Attributes.FOLLOW_RANGE, 10.0D);
    }

    protected abstract double swimSpeed();

    protected abstract float watchDistance();

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new LookAtPlayerGoal(this, Player.class, watchDistance()));
        goalSelector.addGoal(1, new RandomLookAroundGoal(this));
        goalSelector.addGoal(2, new RandomStrollGoal(this, swimSpeed()));
    }

    @Override
    protected void customServerAiStep(ServerLevel level) {
        super.customServerAiStep(level);
        if (!isInWater()) getJumpControl().jump();
    }

    @Override
    public void travel(Vec3 input) {
        if (isInWater()) {
            Vec3 motion = getDeltaMovement();
            setDeltaMovement(motion.x, motion.y * 0.8F, motion.z);
            move(MoverType.SELF, getDeltaMovement());
            return;
        }
        setXRot(getXRot() + 0.2F);
        setYRot(0.0F);
        if (onGround()) {
            push(
                    0.4D * random.nextDouble() - 0.4D * random.nextDouble(),
                    0.0D,
                    0.4D * random.nextDouble() - 0.4D * random.nextDouble());
        }
        super.travel(input);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (level().isClientSide() || !isEffectiveAi()) return;

        if (swimTarget == null || random.nextInt(200) == 0 || !isInWater()) {
            double x = getX() + (random.nextDouble() - 0.5D) * 20.0D;
            double y =
                    Mth.clamp(
                            getY() + (random.nextDouble() - 0.5D) * 8.0D,
                            level().getMinY(),
                            level().getMaxY());
            double z = getZ() + (random.nextDouble() - 0.5D) * 20.0D;
            swimTarget = new Vec3(x, y, z);
        }

        Vec3 toTarget = swimTarget.subtract(position());
        double distance = toTarget.length();
        setDeltaMovement(
                getDeltaMovement()
                        .add(
                                toTarget.x / distance * 0.01D
                                        + (random.nextDouble() - 0.5D) * 0.02D,
                                toTarget.y / distance * 0.01D
                                        + (random.nextDouble() - 0.5D) * 0.02D,
                                toTarget.z / distance * 0.01D
                                        + (random.nextDouble() - 0.5D) * 0.02D));

        Vec3 motion = getDeltaMovement();
        float yaw = (float) (Math.atan2(motion.z, motion.x) * (180.0D / Math.PI)) - 90.0F;
        float pitch =
                (float)
                        -(Math.atan2(motion.y, Math.sqrt(motion.x * motion.x + motion.z * motion.z))
                                * (180.0D / Math.PI));
        setYRot(Mth.approachDegrees(getYRot(), yaw, 10.0F));
        setXRot(Mth.approachDegrees(getXRot(), pitch, 10.0F));
    }
}
