// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-FileCopyrightText: 2025 George Paton
// SPDX-License-Identifier: LGPL-3.0-only

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
