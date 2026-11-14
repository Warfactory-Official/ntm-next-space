/*
 * SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
 * SPDX-FileCopyrightText: 2025 George Paton
 * SPDX-License-Identifier: LGPL-3.0-only
 *
 * Copyright (C) 2026 movblock. All rights reserved.
 * Copyright (C) 2025 George Paton. All rights reserved.
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

package com.hbmspace.client.particle;

import com.hbm.particle.RocketFlameParticle;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.RandomSource;

public final class SpinlaunchBurst {

    private SpinlaunchBurst() {}

    public static void spawn(
            double x,
            double y,
            double z,
            int count,
            float scale,
            double motionX,
            double motionY,
            double motionZ,
            int maxAge) {
        Minecraft mc = Minecraft.getInstance();
        if (!(mc.level instanceof ClientLevel level)) return;
        RandomSource rand = level.getRandom();

        for (int i = 0; i < count; i++) {
            double ox = rand.nextDouble() * 2 - 1;
            double oy = rand.nextDouble() * 2 - 1;
            double oz = rand.nextDouble() * 2 - 1;
            double mult = 1.0 - rand.nextDouble() * 0.1;

            RocketFlameParticle fx =
                    new RocketFlameParticle(level, x + ox, y + oy, z + oz).setScale(scale);
            fx.setParticleSpeed(motionX * mult + ox, motionY * mult + oy, motionZ * mult + oz);
            if (maxAge > 0) fx.setMaxAge(maxAge + rand.nextInt(5));
            mc.particleEngine.add(fx);
        }
    }
}
