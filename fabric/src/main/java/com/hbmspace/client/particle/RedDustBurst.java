// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.client.particle;

import com.hbmspace.entity.mob.RedDustBurstPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.util.RandomSource;

public final class RedDustBurst {

    private RedDustBurst() {}

    public static void spawn(RedDustBurstPayload payload) {
        Minecraft mc = Minecraft.getInstance();
        if (!(mc.level instanceof ClientLevel level)) return;
        RandomSource rand = level.getRandom();

        for (int i = 0; i < payload.count; i++) {
            double mX = rand.nextGaussian() * payload.motion;
            double mY = rand.nextGaussian() * payload.motion;
            double mZ = rand.nextGaussian() * payload.motion;
            Particle fx =
                    mc.particleEngine.createParticle(
                            DustParticleOptions.REDSTONE, payload.x, payload.y, payload.z, 0, 0, 0);
            if (fx != null) fx.setParticleSpeed(mX, mY, mZ);
        }
    }
}
