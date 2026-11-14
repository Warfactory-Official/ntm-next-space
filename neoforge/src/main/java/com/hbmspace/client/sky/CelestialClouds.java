/*
 * SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
 * SPDX-FileCopyrightText: 2026 detree05
 * SPDX-License-Identifier: LGPL-3.0-only
 *
 * Copyright (C) 2026 movblock. All rights reserved.
 * Copyright (C) 2026 detree05. All rights reserved.
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

import com.hbm.api.client.CloudLayers;
import com.hbmspace.dim.CelestialBody;
import com.hbmspace.dim.WorldProviderCelestial;
import com.hbmspace.dim.trait.CBT_Atmosphere;
import net.minecraft.client.CloudStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.CloudRenderer;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.ARGB;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public final class CelestialClouds implements CloudLayers.Renderer {

    private static final float[] LAYER_HEIGHT_OFFSETS = {0.0F, 12.0F, 24.0F};
    private static final float[] LAYER_ALPHA_MULTIPLIERS = {1.0F, 0.84F, 0.76F};
    private static final double[] LAYER_SCROLL_MULTIPLIERS = {1.0D, 0.84D, 0.7D};
    private static final double LAYER_POSITION_RANGE = 16384.0D;
    private static final double SCROLL_SPEED = 0.03D;

    private final @Nullable Layer[] layers = new Layer[LAYER_HEIGHT_OFFSETS.length];

    public static void register() {
        CloudLayers.register(new CelestialClouds());
    }

    @Override
    public void render(
            int color,
            CloudStatus status,
            float bottomY,
            int range,
            Vec3 camera,
            long gameTime,
            float partialTicks) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null || WorldProviderCelestial.of(level) == null) return;

        int count =
                WorldProviderCelestial.getCloudLayerCount(
                        CelestialBody.getBody(level).getTrait(CBT_Atmosphere.class));
        long dimension = level.dimension().identifier().hashCode();
        double time = gameTime + partialTicks;

        for (int i = 1; i < count && i < layers.length; i++) {
            if (layers[i] == null) layers[i] = new Layer();

            double shiftX =
                    (LAYER_SCROLL_MULTIPLIERS[i] - 1.0D) * time * SCROLL_SPEED
                            + offset(dimension, i, 0x4F1BBCDCBFA54001L);
            double shiftZ = offset(dimension, i, 0x2C1B3C6D5E7F8103L);
            int alpha = Math.round(ARGB.alpha(color) * LAYER_ALPHA_MULTIPLIERS[i]);

            layers[i].render(
                    ARGB.color(alpha, color),
                    status,
                    bottomY + LAYER_HEIGHT_OFFSETS[i],
                    range,
                    camera.add(shiftX, 0.0D, shiftZ),
                    gameTime,
                    partialTicks);
        }
    }

    @Override
    public void reload() {
        close();
    }

    @Override
    public void markForRebuild() {
        for (Layer layer : layers) {
            if (layer != null) layer.markForRebuild();
        }
    }

    @Override
    public void endFrame() {
        for (Layer layer : layers) {
            if (layer != null) layer.endFrame();
        }
    }

    @Override
    public void close() {
        for (int i = 0; i < layers.length; i++) {
            if (layers[i] != null) {
                layers[i].close();
                layers[i] = null;
            }
        }
    }

    private static double offset(long dimension, int layer, long salt) {
        long mixed =
                mix(
                        (dimension * 0x9E3779B97F4A7C15L)
                                ^ ((long) (layer + 1) * 0xC2B2AE3D27D4EB4FL)
                                ^ salt);
        double normalized = (double) (mixed & ((1L << 53) - 1)) / (double) (1L << 53);
        return (normalized - 0.5D) * LAYER_POSITION_RANGE;
    }

    private static long mix(long value) {
        value ^= value >>> 33;
        value *= 0xff51afd7ed558ccdL;
        value ^= value >>> 33;
        value *= 0xc4ceb9fe1a85ec53L;
        value ^= value >>> 33;
        return value;
    }

    private static final class Layer extends CloudRenderer {
        Layer() {
            ResourceManager resources = Minecraft.getInstance().getResourceManager();
            apply(prepare(resources, Profiler.get()), resources, Profiler.get());
        }
    }
}
