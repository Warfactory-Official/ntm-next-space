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

import com.hbm.entity.ModEntities;
import com.hbm.entity.projectile.ArtilleryShellEntity;
import com.hbm.entity.projectile.BulletBaseMK4Entity;
import com.hbm.items.weapon.AmmoArtyItem;
import com.hbm.sound.ModSounds;
import com.hbm.sound.WorldSounds;
import com.hbmspace.entity.mob.MobWeaponsSpace;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;

public class BehemothGunGoal extends Goal {

    private final PathfinderMob owner;
    private final int delay;
    private final int switchAttackDistance;
    private final int reloadDelay;
    private LivingEntity target;
    private int timer;
    private boolean artilleryMode;
    private int reloadTimer;

    public BehemothGunGoal(
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
            if (artilleryMode) {
                fireArtilleryShell();
            } else {
                fireGatlingBarrage();
            }
            timer = delay;
        }
        owner.setYRot(owner.getYHeadRot());
    }

    private void fireGatlingBarrage() {
        double x = owner.getX() + Math.cos(Math.toRadians(owner.getYRot())) * 1.5D;
        double y = owner.getY() + 10.0D;
        double z = owner.getZ() + Math.sin(Math.toRadians(owner.getYRot())) * 1.5D;
        double dx = target.getX() - x;
        double dy = target.getY() + target.getEyeHeight() - y;
        double dz = target.getZ() - z;
        double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);

        BulletBaseMK4Entity bullet =
                new BulletBaseMK4Entity(
                        owner, MobWeaponsSpace.R762_AP_STILL, 0.5F, 0.01F, 0.0D, 0.0D, 0.0D);
        bullet.setPos(x, y, z);
        bullet.setFlightDirection(dx / distance * 0.5D, dy / distance * 0.5D, dz / distance * 0.5D);
        owner.level().addFreshEntity(bullet);
        WorldSounds.play(owner, ModSounds.GUN_MINIGUN_FIRE.get(), 15.0F, 1.0F);
    }

    private void fireArtilleryShell() {
        if (reloadTimer > 0) {
            reloadTimer--;
            return;
        }
        ArtilleryShellEntity shell =
                new ArtilleryShellEntity(ModEntities.ARTILLERY_SHELL.get(), owner.level());
        shell.setType(AmmoArtyItem.ArtilleryShellType.CHLORINE.ordinal());
        shell.setPos(owner.getX(), owner.getY() + 14.0D, owner.getZ());
        double motionY = 0.5D + owner.getRandom().nextDouble() * 0.5D;
        shell.setThrowableHeading(
                (target.getX() - owner.getX()) * 0.05D,
                motionY * 6.0D,
                (target.getZ() - owner.getZ()) * 0.05D,
                5.0F,
                0.0F);
        shell.setTarget(target.getX(), target.getY(), target.getZ());
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
        owner.level().addFreshEntity(shell);
        reloadTimer = reloadDelay;
    }
}
