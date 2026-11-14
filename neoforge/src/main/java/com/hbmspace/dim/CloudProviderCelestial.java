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

package com.hbmspace.dim;

import com.hbm.inventory.fluid.NTMFluids;
import com.hbmspace.dim.trait.CBT_Atmosphere.FluidEntry;
import com.hbmspace.dim.trait.CBT_Atmosphere;
import net.minecraft.util.Mth;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public final class CloudProviderCelestial {

    private CloudProviderCelestial() {}

    private static boolean isNeutralCloudFluid(Fluid fluid) {
        return fluid == NTMFluids.byName("EARTHAIR")
                || fluid == NTMFluids.OXYGEN
                || fluid == NTMFluids.byName("NITROGEN")
                || fluid == NTMFluids.byName("DUNAAIR")
                || fluid == NTMFluids.CARBONDIOXIDE;
    }

    private static double @Nullable [] getCloudTintData(@Nullable CBT_Atmosphere atmosphere) {
        if (atmosphere == null || atmosphere.fluids.isEmpty()) {
            return null;
        }

        double totalPressure = 0.0D;
        double tintPressure = 0.0D;
        double tintR = 0.0D;
        double tintG = 0.0D;
        double tintB = 0.0D;

        for (FluidEntry entry : atmosphere.fluids) {
            if (entry.pressure <= 0.0D) {
                continue;
            }

            totalPressure += entry.pressure;

            if (isNeutralCloudFluid(entry.fluid)) {
                continue;
            }

            Vec3 fluidColor = WorldProviderCelestial.getAtmosphereFluidColor(entry.fluid);
            tintPressure += entry.pressure;
            tintR += fluidColor.x * entry.pressure;
            tintG += fluidColor.y * entry.pressure;
            tintB += fluidColor.z * entry.pressure;
        }

        if (totalPressure <= 0.0D || tintPressure <= 0.0D) {
            return null;
        }

        tintR /= tintPressure;
        tintG /= tintPressure;
        tintB /= tintPressure;

        double tintPeak = Math.max(tintR, Math.max(tintG, tintB));
        if (tintPeak <= 0.0D) {
            return null;
        }

        double tintStrength = Mth.clamp(tintPressure / totalPressure, 0.0D, 0.65D);
        return new double[] {
            tintStrength,
            Mth.clamp(tintR / tintPeak, 0.0D, 1.0D),
            Mth.clamp(tintG / tintPeak, 0.0D, 1.0D),
            Mth.clamp(tintB / tintPeak, 0.0D, 1.0D)
        };
    }

    public static float getCloudTintStrength(@Nullable CBT_Atmosphere atmosphere) {
        double[] tintData = getCloudTintData(atmosphere);
        return tintData != null ? (float) tintData[0] : 0.0F;
    }

    public static Vec3 getTintedCloudColor(@Nullable CBT_Atmosphere atmosphere, Vec3 clouds) {
        double[] tintData = getCloudTintData(atmosphere);
        if (tintData == null) {
            return clouds;
        }

        double cloudLuma = clouds.x * 0.299D + clouds.y * 0.587D + clouds.z * 0.114D;
        double cloudBrightness = Mth.clamp(cloudLuma * 0.9D, 0.0D, 1.0D);
        double tintStrength = tintData[0];
        double cloudColorR = Mth.clamp(tintData[1] * cloudBrightness, 0.0D, 1.0D);
        double cloudColorG = Mth.clamp(tintData[2] * cloudBrightness, 0.0D, 1.0D);
        double cloudColorB = Mth.clamp(tintData[3] * cloudBrightness, 0.0D, 1.0D);

        return new Vec3(
                Mth.clamp(clouds.x + (cloudColorR - clouds.x) * tintStrength, 0.0D, 1.0D),
                Mth.clamp(clouds.y + (cloudColorG - clouds.y) * tintStrength, 0.0D, 1.0D),
                Mth.clamp(clouds.z + (cloudColorB - clouds.z) * tintStrength, 0.0D, 1.0D));
    }
}
