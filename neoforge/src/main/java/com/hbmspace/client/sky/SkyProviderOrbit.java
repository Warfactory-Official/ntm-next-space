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

import com.hbmspace.dim.CelestialBody;
import com.hbmspace.dim.SolarSystem;
import com.hbmspace.dim.orbit.OrbitalStation.StationState;
import com.hbmspace.dim.orbit.OrbitalStation;
import com.hbmspace.dim.orbit.WorldProviderOrbit;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.SkyRenderer;
import net.minecraft.client.renderer.state.level.SkyRenderState;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionfc;
import org.jspecify.annotations.Nullable;

public class SkyProviderOrbit extends SkyProviderCelestial {

    private static final float ORBITAL_TILT = 80;

    private OrbitalStation station;
    private @Nullable CelestialBody lastBody;
    private double progress;
    private float starBrightness;

    @Override
    public void extract(
            ClientLevel level, float partialTicks, Camera camera, SkyRenderState state) {
        this.level = level;
        this.partialTicks = partialTicks;
        station = OrbitalStation.clientStation;
        body = station.orbiting;
        progress = station.getTransferProgress(partialTicks);

        WorldProviderOrbit.Sky sky = WorldProviderOrbit.getSky(level, partialTicks, station);
        metrics = sky.metrics();
        solarAngle = getCelestialAngle(sky.celestialAngle(), partialTicks);
        starBrightness = WorldProviderOrbit.getStarBrightness(station, sky.eclipse(), partialTicks);
        sunBrightness = WorldProviderOrbit.getSunBrightness(sky.eclipse());
        extractViewer(camera, partialTicks);
    }

    @Override
    public void render(SkyRenderer vanilla, SkyRenderState state, SkyDraws draws) {
        float siderealAngle =
                (float) SolarSystem.calculateSiderealAngle(level, partialTicks, station.orbiting);
        float celestialPhase = (1 - (solarAngle + 0.5F) % 1) * 2 - 1;

        renderStars(draws, starBrightness, solarAngle + siderealAngle, ORBITAL_TILT);

        PoseStack pose = new PoseStack();
        pose.rotate((Quaternionfc) Axis.XP.rotationDegrees(ORBITAL_TILT));
        pose.rotate((Quaternionfc) Axis.YP.rotationDegrees(-90.0F));
        pose.rotate((Quaternionfc) Axis.XP.rotationDegrees(solarAngle * 360.0F));

        double sunSize =
                SolarSystem.calculateSunSize(station.orbiting) * SolarSystem.SUN_RENDER_SCALE;
        if (station.state != StationState.ORBIT) {
            double sunTargetSize =
                    SolarSystem.calculateSunSize(station.target) * SolarSystem.SUN_RENDER_SCALE;
            sunSize = sunSize + progress * (sunTargetSize - sunSize);
        }
        double coronaSize = sunSize * (3 - smoothstep(Math.abs(celestialPhase), 0.7, 0.8));

        renderSun(draws, pose, station.orbiting.getStar(), sunSize, coronaSize, 1, 0);

        CelestialBody orbiting =
                station.state != StationState.ORBIT && progress > 0.5
                        ? station.target
                        : station.orbiting;
        renderCelestials(
                draws,
                pose,
                metrics,
                Vec3.ZERO,
                1,
                1,
                orbiting,
                SolarSystem.MAX_APPARENT_SIZE_ORBIT);

        draws.flush();
    }

    private float getCelestialAngle(float celestialAngle, float partialTicks) {
        if (station.state == StationState.ORBIT) return celestialAngle;

        float solar = celestialAngle * 360.0F - 180.0F;

        if (station.state != StationState.ARRIVING) lastBody = station.orbiting;

        double unscaled = station.getUnscaledProgress(partialTicks);
        float travelAngle =
                lastBody == null
                        ? 0
                        : -(float)
                                SolarSystem.calculateSingleAngle(metrics, lastBody, station.target);
        travelAngle = Mth.wrapDegrees(travelAngle + 90.0F);

        return switch (station.state) {
            case TRANSFER -> (travelAngle + 180.0F) / 360.0F;
            case LEAVING ->
                    ((float) WorldProviderOrbit.clerp(unscaled, solar, travelAngle) + 180.0F)
                            / 360.0F;
            default ->
                    ((float) WorldProviderOrbit.clerp(unscaled, travelAngle, solar) + 180.0F)
                            / 360.0F;
        };
    }

    private static double smoothstep(double t, double edge0, double edge1) {
        t = Mth.clamp((t - edge0) / (edge1 - edge0), 0.0, 1.0);
        return t * t * (3.0 - 2.0 * t);
    }
}
