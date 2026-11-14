/*
 * SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright (C) 2026 movblock. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * You may not use this file under the Apache License except in
 * compliance with that License. You may obtain a copy at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Apache License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License for the specific language governing permissions
 * and limitations under the License.
 */

package com.hbmspace.client.particle;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.util.RandomSource;

public final class RedDustBurst {

    private RedDustBurst() {}

    public static void spawn(double x, double y, double z, int count, double motion) {
        Minecraft mc = Minecraft.getInstance();
        if (!(mc.level instanceof ClientLevel level)) return;
        RandomSource rand = level.getRandom();

        for (int i = 0; i < count; i++) {
            double mX = rand.nextGaussian() * motion;
            double mY = rand.nextGaussian() * motion;
            double mZ = rand.nextGaussian() * motion;
            Particle fx =
                    mc.particleEngine.createParticle(
                            DustParticleOptions.REDSTONE, x, y, z, 0, 0, 0);
            if (fx != null) fx.setParticleSpeed(mX, mY, mZ);
        }
    }
}
