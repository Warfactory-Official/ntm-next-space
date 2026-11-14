/*
 * SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
 * SPDX-FileCopyrightText: 2025 George Paton
 * SPDX-FileCopyrightText: 2024-2025 JamesH_2
 * SPDX-License-Identifier: LGPL-3.0-only
 *
 * Copyright (C) 2026 movblock. All rights reserved.
 * Copyright (C) 2025 George Paton. All rights reserved.
 * Copyright (C) 2024-2025 JamesH_2. All rights reserved.
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

import com.hbm.entity.projectile.BulletBaseMK4Entity;
import com.hbm.items.weapon.sedna.BulletConfig;
import com.hbm.items.weapon.sedna.factory.XFactory12ga;
import com.hbm.items.weapon.sedna.factory.XFactoryRocket;
import com.hbm.particle.helper.CasingCreator;
import com.hbm.sound.ModSounds;
import com.hbm.sound.WorldSounds;
import com.hbmspace.entity.mob.MobWeaponsSpace;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;

public class TankshellGoal extends Goal {

    private final PathfinderMob owner;
    private final int delay;
    private final int switchAttackDistance;
    private final int reloadDelay;
    private LivingEntity target;
    private int timer;
    private boolean artilleryMode = true;
    private int reloadTimer;

    public TankshellGoal(
            PathfinderMob owner, int delay, int switchAttackDistance, int reloadDelay) {
        this.owner = owner;
        this.delay = delay;
        this.timer = delay;
        this.switchAttackDistance = switchAttackDistance;
        this.reloadDelay = reloadDelay;
        this.reloadTimer = reloadDelay;
    }

    @Override
    public boolean canUse() {
        LivingEntity entity = owner.getTarget();
        if (entity == null) return false;
        target = entity;
        double dist = entity.position().distanceTo(owner.position());
        artilleryMode = dist > switchAttackDistance;
        return dist > 2.0D && dist < 50.0D;
    }

    @Override
    public boolean canContinueToUse() {
        return canUse() || !owner.getNavigation().isDone();
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        if (--timer <= 0) {
            if (artilleryMode) fire();
            timer = delay;
        }
        owner.setYRot(owner.getYHeadRot());
    }

    private void fire() {
        if (reloadTimer <= 0) {
            double zOffset = Math.sin(Math.toRadians(owner.getYHeadRot())) * 1.5D;
            double dx = target.getX() - owner.getX();
            double dy = target.getY() + target.getEyeHeight() - (owner.getY() + 10.0D);
            double dz = target.getZ() - (owner.getZ() + zOffset);
            double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
            dx /= distance;
            dy /= distance;
            dz /= distance;
            spawnCasing();

            RandomSource random = owner.getRandom();
            if (distance > 30.0D) {
                shoot(
                        XFactoryRocket.rocket_qd[owner.getHealth() < 30.0F ? 3 : 1],
                        dx * 0.5D,
                        dy * 0.1D,
                        dz * 0.5D);
            } else {
                for (int i = 0; i < 8; i++) {
                    shoot(
                            XFactory12ga.g12_explosive,
                            dx * (0.2D + 0.5D * random.nextDouble()),
                            dy * 0.1D,
                            dz * (0.2D + 0.5D * random.nextDouble()));
                }
            }
            WorldSounds.play(
                    owner.level(),
                    null,
                    owner.getX(),
                    owner.getY(),
                    owner.getZ(),
                    ModSounds.TURRET_JEREMY_FIRE.get(),
                    owner.getSoundSource(),
                    25.0F,
                    1.0F);
            reloadTimer = reloadDelay;
        } else {
            reloadTimer--;
        }

        if (reloadTimer == 20) {
            WorldSounds.play(
                    owner.level(),
                    null,
                    owner.getX(),
                    owner.getY(),
                    owner.getZ(),
                    ModSounds.TURRET_JEREMY_RELOAD.get(),
                    owner.getSoundSource(),
                    3.0F,
                    1.0F);
        }
    }

    private void shoot(BulletConfig config, double motionX, double motionY, double motionZ) {
        BulletBaseMK4Entity bullet =
                new BulletBaseMK4Entity(owner, config, 4.0F, 0.01F, 0.0D, 0.0D, 0.0D);
        bullet.setPos(owner.getX(), owner.getY() + 3.0D, owner.getZ());
        bullet.setFlightDirection(motionX, motionY, motionZ);
        owner.level().addFreshEntity(bullet);
    }

    private void spawnCasing() {
        RandomSource random = owner.getRandom();
        CasingCreator.composeEffect(
                owner.level(),
                owner.getX(),
                owner.getY(),
                owner.getZ(),
                owner.getYHeadRot(),
                0.0F,
                -1.0D,
                0.6D,
                0.0D,
                0.1D,
                (float) (random.nextGaussian() * 5.0D),
                (float) (random.nextGaussian() * 10.0D),
                MobWeaponsSpace.TANK_CASING.name(),
                false,
                0,
                0.0D,
                0);
    }
}
