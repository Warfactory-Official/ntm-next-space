/*
 * SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
 * SPDX-FileCopyrightText: 2024-2026 George Paton
 * SPDX-License-Identifier: LGPL-3.0-only
 *
 * Copyright (C) 2026 movblock. All rights reserved.
 * Copyright (C) 2024-2026 George Paton. All rights reserved.
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

package com.hbmspace.dim.orbit;

import com.hbmspace.dim.CelestialBody;
import com.hbmspace.dim.SolarSystem.AstroMetric;
import com.hbmspace.dim.SolarSystem;
import com.hbmspace.dim.WorldProviderCelestial;
import com.hbmspace.dim.orbit.OrbitalStation.StationState;
import com.hbmspace.dim.trait.CBT_Destroyed;
import com.hbmspace.util.AstronomyUtil;
import java.util.List;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;

public final class WorldProviderOrbit {
    private static final float ORBITAL_PERIOD = 7200;

    private WorldProviderOrbit() {}

    public static float getOrbitalAltitude(CelestialBody body) {
        return getAltitudeForPeriod(body.massKg, ORBITAL_PERIOD);
    }

    private static float getAltitudeForPeriod(float massKg, float period) {
        return (float)
                Math.cbrt(
                        (AstronomyUtil.GRAVITATIONAL_CONSTANT * massKg * (period * period))
                                / (4 * Math.PI * Math.PI));
    }

    public static float getSunPower(Level level, int x, int z) {
        OrbitalStation station = OrbitalStation.getStationFromPosition(x, z);

        double progress = station.getTransferProgress(0);
        float sunPower = station.orbiting.getSunPower();
        if (progress > 0) {
            double start = sunPower;
            return (float) (start + progress * (station.target.getSunPower() - start));
        }

        double eclipseAmount = station.getEclipseAmount(level);

        return sunPower * (1 - (float) eclipseAmount);
    }

    public record Sky(List<AstroMetric> metrics, float celestialAngle, double eclipse) {}

    public static Sky getSky(Level level, float partialTicks, OrbitalStation station) {
        double sunSize = SolarSystem.calculateSunSize(station.orbiting);
        double progress = station.getTransferProgress(partialTicks);

        List<AstroMetric> metrics;
        if (station.state == StationState.ORBIT) {
            double altitude = getOrbitalAltitude(station.orbiting);
            metrics =
                    SolarSystem.calculateMetricsFromSatellite(
                            level, partialTicks, station.orbiting, altitude);
        } else {
            double fromAlt = getOrbitalAltitude(station.orbiting);
            double toAlt = getOrbitalAltitude(station.target);
            metrics =
                    SolarSystem.calculateMetricsBetweenSatelliteOrbits(
                            level,
                            partialTicks,
                            station.orbiting,
                            station.target,
                            fromAlt,
                            toAlt,
                            progress);

            double sunTargetSize = SolarSystem.calculateSunSize(station.target);
            sunSize = sunSize + progress * (sunTargetSize - sunSize);
        }

        float angle =
                (float)
                        SolarSystem.calculateSingleAngle(
                                level,
                                partialTicks,
                                metrics,
                                station.orbiting,
                                getOrbitalAltitude(station.orbiting));
        if (progress > 0) {
            angle =
                    (float)
                            clerp(
                                    progress,
                                    angle,
                                    (float)
                                            SolarSystem.calculateSingleAngle(
                                                    level,
                                                    partialTicks,
                                                    metrics,
                                                    station.target,
                                                    getOrbitalAltitude(station.target)));
        }

        double eclipse =
                WorldProviderCelestial.getEclipseFactor(
                        metrics, sunSize, SolarSystem.MAX_APPARENT_SIZE_ORBIT);
        return new Sky(metrics, 0.5F - (angle / 360.0F), eclipse);
    }

    public static float getStarBrightness(
            OrbitalStation station, double eclipse, float partialTicks) {
        float distanceStart = 9_000_000;
        float distanceEnd = 30_000_000;

        double progress = station.getTransferProgress(partialTicks);
        float semiMajorAxisKm = station.orbiting.getPlanet().semiMajorAxisKm;
        if (progress > 0) {
            float target = station.target.getPlanet().semiMajorAxisKm;
            semiMajorAxisKm = (float) (semiMajorAxisKm + progress * (target - semiMajorAxisKm));
        }

        float distanceFactor =
                Mth.clamp(
                        (semiMajorAxisKm - distanceStart) / (distanceEnd - distanceStart), 0F, 1F);

        return Mth.clamp((float) eclipse, distanceFactor, 1F);
    }

    public static float getSunBrightness(double eclipse) {
        if (SolarSystem.kerbol.hasTrait(CBT_Destroyed.class)) return 0;
        return 1.0F - (float) eclipse;
    }

    public static double clerp(double delta, double start, double end) {
        double angle = ((((end - start) % 360) + 540) % 360) - 180;
        return start + angle * delta;
    }
}
