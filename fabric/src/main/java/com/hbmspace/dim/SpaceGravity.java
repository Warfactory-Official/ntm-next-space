// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-FileCopyrightText: 2025 George Paton
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.dim;

import com.hbmspace.util.AstronomyUtil;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.allay.Allay;
import net.minecraft.world.entity.animal.chicken.Chicken;
import net.minecraft.world.entity.animal.happyghast.HappyGhast;
import net.minecraft.world.entity.monster.Ghast;
import net.minecraft.world.entity.monster.Phantom;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public final class SpaceGravity {

    private SpaceGravity() {}

    public static void onLivingUpdate(LivingEntity entity) {
        if (entity instanceof Player player && player.getAbilities().flying) return;
        if (flies(entity)) return;

        float gravity = CelestialBody.getGravity(entity);
        Vec3 motion = entity.getDeltaMovement();
        double motionY = motion.y;

        if (gravity == 0) {
            motionY /= 0.98F;
            motionY += (AstronomyUtil.STANDARD_GRAVITY / 20F);

            if (entity instanceof Player player) {
                if (player.isShiftKeyDown()) motionY -= 0.01F;
                if (player.isJumping()) motionY += 0.01F;
            } else if (entity instanceof Chicken) {
                motionY = 0;
            }

            motionY *= 0.91F;
        } else if (!entity.isInWater()
                && entity.tickCount > 20
                && (gravity < 1.5F || gravity > 1.7F)) {
            if (gravity < 0.2F) gravity = 0.2F;

            if ((gravity < 1.5F || motionY < 0) && !(entity instanceof Chicken)) {
                motionY /= 0.98F;
                motionY += (AstronomyUtil.STANDARD_GRAVITY / 20F);
                motionY -= (gravity / 20F);
                motionY *= 0.98F;
            } else {
                return;
            }
        } else {
            return;
        }

        entity.setDeltaMovement(motion.x, motionY, motion.z);
    }

    private static boolean flies(LivingEntity entity) {
        return entity.isNoGravity()
                || entity instanceof Ghast
                || entity instanceof HappyGhast
                || entity instanceof Allay
                || entity instanceof Phantom;
    }

    public static double scaleFall(LivingEntity entity, double distance) {
        float gravity = CelestialBody.getGravity(entity);

        if (gravity < 0.3F) {
            return 0;
        } else if (gravity < 1.5F) {
            return distance * (gravity / AstronomyUtil.STANDARD_GRAVITY);
        }

        return distance;
    }
}
