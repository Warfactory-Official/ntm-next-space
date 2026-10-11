// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-FileCopyrightText: 2024 JamesH_2
// SPDX-License-Identifier: LGPL-3.0-only

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
