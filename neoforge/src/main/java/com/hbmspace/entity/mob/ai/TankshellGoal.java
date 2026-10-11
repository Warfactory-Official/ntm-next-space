// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.entity.mob.ai;

import com.hbm.entity.projectile.EntityBulletBaseMK4;
import com.hbm.items.weapon.sedna.BulletConfig;
import com.hbm.items.weapon.sedna.factory.XFactory12ga;
import com.hbm.items.weapon.sedna.factory.XFactoryRocket;
import com.hbm.particle.helper.CasingCreator;
import com.hbm.sound.ModSounds;
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
            owner.level()
                    .playSound(
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
            owner.level()
                    .playSound(
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
        EntityBulletBaseMK4 bullet =
                new EntityBulletBaseMK4(owner, config, 4.0F, 0.01F, 0.0D, 0.0D, 0.0D);
        bullet.setPos(owner.getX(), owner.getY() + 3.0D, owner.getZ());
        bullet.setDeltaMovement(motionX, motionY, motionZ);
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
                MobWeaponsSpace.TANK_CASING.getName(),
                false,
                0,
                0.0D,
                0);
    }
}
