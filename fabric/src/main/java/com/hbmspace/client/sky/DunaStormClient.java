/*
 * SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
 * SPDX-FileCopyrightText: 2024-2025 George Paton
 * SPDX-License-Identifier: LGPL-3.0-only
 *
 * Copyright (C) 2026 movblock. All rights reserved.
 * Copyright (C) 2024-2025 George Paton. All rights reserved.
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

package com.hbmspace.client.sky;

import com.hbmspace.dim.WorldProviderCelestial;
import com.hbmspace.dim.duna.DunaDustStorm;
import com.hbmspace.dim.duna.WorldProviderDuna;
import com.hbmspace.particle.SpaceParticles;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public final class DunaStormClient {

    private static @Nullable ClientLevel level;
    private static float smoothed;

    private DunaStormClient() {}

    static void tick(Minecraft mc) {
        ClientLevel current = mc.level;
        if (current != level) {
            level = current;
            smoothed = 0;
        }
        if (current == null || !(WorldProviderCelestial.of(current) instanceof WorldProviderDuna))
            return;

        smoothed = (float) Mth.lerp(0.008D, smoothed, DunaDustStorm.clientIntensity);
        RandomSource rand = current.getRandom();
        Entity view = mc.getCameraEntity();
        if (view != null && smoothed >= 0.05F && rand.nextFloat() < smoothed) {
            Vec3 offset =
                    new Vec3(20, 0, 50)
                            .zRot((float) (rand.nextDouble() * Math.PI * 10))
                            .yRot((float) (rand.nextDouble() * Math.PI * 2 * 5));
            current.addParticle(
                    SpaceParticles.DUST_STORM.get(),
                    view.getX() + offset.x,
                    view.getY(),
                    view.getZ() + offset.z,
                    -4,
                    0,
                    0);
        }
    }

    public static float smoothed() {
        return smoothed;
    }

    static void clear() {
        level = null;
        smoothed = 0;
        DunaDustStorm.clientIntensity = 0;
    }
}
