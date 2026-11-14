/*
 * SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
 * SPDX-FileCopyrightText: 2024 JamesH_2
 * SPDX-License-Identifier: LGPL-3.0-only
 *
 * Copyright (C) 2026 movblock. All rights reserved.
 * Copyright (C) 2024 JamesH_2. All rights reserved.
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

package com.hbmspace.entity.mob.ai;

import com.hbmspace.sound.MobSoundsSpace;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;

public class StepTowardsTargetGoal extends Goal {

    private final PathfinderMob mob;
    private final double stepDistance;
    private final double speed;
    private final int stepDuration;
    private final int stepCooldown;
    private final double minDistance;
    private int stepTimer;

    public StepTowardsTargetGoal(
            PathfinderMob mob,
            double stepDistance,
            double speed,
            int stepDuration,
            int stepCooldown,
            double minDistance) {
        this.mob = mob;
        this.stepDistance = stepDistance;
        this.speed = speed;
        this.stepDuration = stepDuration;
        this.stepCooldown = stepCooldown;
        this.minDistance = minDistance;
    }

    @Override
    public boolean canUse() {
        return true;
    }

    @Override
    public void start() {
        stepTimer = 0;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        if (stepTimer > 0) {
            stepTimer--;
            return;
        }
        LivingEntity target = mob.getTarget();
        if (target != null && target.isAlive()) {
            double dx = target.getX() - mob.getX();
            double dz = target.getZ() - mob.getZ();
            double distance = Math.sqrt(dx * dx + dz * dz);
            mob.playSound(MobSoundsSpace.ENTITY_BEEP.get(), 1.0F, 1.0F);
            if (distance > minDistance) {
                mob.getNavigation()
                        .moveTo(
                                mob.getX() + dx / distance * stepDistance,
                                mob.getY(),
                                mob.getZ() + dz / distance * stepDistance,
                                speed);
            }
        } else {
            int x = Mth.floor(mob.getX()) + mob.getRandom().nextInt(10) - 5;
            int z = Mth.floor(mob.getZ()) + mob.getRandom().nextInt(10) - 5;
            mob.getNavigation().moveTo(x, Mth.floor(mob.getY()), z, speed);
        }
        stepTimer = stepDuration + stepCooldown;
    }
}
