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

package com.hbmspace.client.render;

import com.hbmspace.dim.CelestialBody;
import com.hbmspace.dim.SolarSystem.OrreryMetric;
import com.hbmspace.dim.SolarSystem;
import java.util.List;
import net.minecraft.util.ARGB;
import net.minecraft.world.level.Level;

public final class OrreryPronter {
    public static final float LIT = 0.8F;
    public static final float SUN_UV_MIN = 0.375F;
    public static final float SUN_UV_MAX = 0.625F;

    private static final float[][] CUBE = {
        {-.5F, .5F, -.5F}, {.5F, .5F, -.5F}, {.5F, -.5F, -.5F}, {-.5F, -.5F, -.5F},
        {-.5F, .5F, .5F}, {-.5F, .5F, -.5F}, {-.5F, -.5F, -.5F}, {-.5F, -.5F, .5F},
        {.5F, .5F, .5F}, {-.5F, .5F, .5F}, {-.5F, -.5F, .5F}, {.5F, -.5F, .5F},
        {.5F, .5F, -.5F}, {.5F, .5F, .5F}, {.5F, -.5F, .5F}, {.5F, -.5F, -.5F},
        {-.5F, -.5F, -.5F}, {.5F, -.5F, -.5F}, {.5F, -.5F, .5F}, {-.5F, -.5F, .5F},
        {.5F, .5F, -.5F}, {-.5F, .5F, -.5F}, {-.5F, .5F, .5F}, {.5F, .5F, .5F}
    };
    public static final int CUBE_VERTICES = CUBE.length;

    private OrreryPronter() {}

    public record Scene(CelestialBody sun, List<OrreryMetric> metrics) {}

    public static Scene scene(Level level, float partialTicks) {
        return new Scene(
                CelestialBody.getStar(level),
                SolarSystem.calculatePositionsOrrery(level, partialTicks));
    }

    public static double scale(CelestialBody sun) {
        return 1 / Math.min(sun.radiusKm, SolarSystem.ORRERY_MAX_RADIUS);
    }

    public static double bodyScale(CelestialBody body) {
        double bodyScale = body.radiusKm;
        if (bodyScale < 2_000) bodyScale = (bodyScale / 63) * (bodyScale / 63) + 1_000;
        return bodyScale;
    }

    public static void cubeVertex(int index, float uvMin, float uvMax, float[] out) {
        float[] position = CUBE[index];
        int corner = index & 3;
        out[0] = position[0];
        out[1] = position[1];
        out[2] = position[2];
        out[3] = corner == 0 || corner == 3 ? uvMax : uvMin;
        out[4] = corner < 2 ? uvMax : uvMin;
    }

    public static int orbitColor(CelestialBody body) {
        return ARGB.colorFromFloat(
                1.0F, body.color[0] * LIT, body.color[1] * LIT, body.color[2] * LIT);
    }
}
