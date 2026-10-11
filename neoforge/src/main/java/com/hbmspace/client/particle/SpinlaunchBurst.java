// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-FileCopyrightText: 2025 George Paton
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.client.particle;

import com.hbm.particle.ParticleRocketFlame;
import com.hbmspace.handler.SpinlaunchPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.RandomSource;

public final class SpinlaunchBurst {

    private SpinlaunchBurst() {}

    public static void spawn(SpinlaunchPayload payload) {
        Minecraft mc = Minecraft.getInstance();
        if (!(mc.level instanceof ClientLevel level)) return;
        RandomSource rand = level.getRandom();

        for (int i = 0; i < payload.count; i++) {
            double ox = rand.nextDouble() * 2 - 1;
            double oy = rand.nextDouble() * 2 - 1;
            double oz = rand.nextDouble() * 2 - 1;
            double mult = 1.0 - rand.nextDouble() * 0.1;

            ParticleRocketFlame fx =
                    new ParticleRocketFlame(level, payload.x + ox, payload.y + oy, payload.z + oz)
                            .setScale(payload.scale);
            fx.setParticleSpeed(
                    payload.motionX * mult + ox,
                    payload.motionY * mult + oy,
                    payload.motionZ * mult + oz);
            if (payload.maxAge > 0) fx.setMaxAge(payload.maxAge + rand.nextInt(5));
            mc.particleEngine.add(fx);
        }
    }
}
