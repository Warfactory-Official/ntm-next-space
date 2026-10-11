// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.handler;

import com.hbmspace.potion.HbmPotionSpace;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public final class SlipperyMotion {
    private SlipperyMotion() {}

    public static void tick(LivingEntity entity) {
        if (entity instanceof Player player) {
            if (!entity.level().isClientSide()
                    || !player.isLocalPlayer()
                    || player.getAbilities().flying) return;
        } else if (entity.level().isClientSide()) {
            return;
        }
        if (!entity.onGround()
                || !entity.hasEffect(
                        BuiltInRegistries.MOB_EFFECT.wrapAsHolder(HbmPotionSpace.SLIPPERY.get())))
            return;
        Vec3 motion = entity.getDeltaMovement();
        double x = motion.x;
        double z = motion.z;
        if (x == 0D && z == 0D) return;

        double angle = Math.atan2(z, x);
        x += (Math.cos(angle) * 0.6D - x) * 0.1D;
        z += (Math.sin(angle) * 0.6D - z) * 0.1D;
        if (entity.zza == 0F && entity.xxa == 0F) {
            x *= 0.9D;
            z *= 0.9D;
            double total = Math.sqrt(x * x + z * z);
            double smoothing = total * 0.02D;
            x -= x / total * smoothing;
            z -= z / total * smoothing;
        }
        entity.setDeltaMovement(x, motion.y, z);
    }
}
