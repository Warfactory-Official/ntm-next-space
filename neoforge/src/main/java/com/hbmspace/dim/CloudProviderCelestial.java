// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-FileCopyrightText: 2026 detree05
// SPDX-License-Identifier: LGPL-3.0-only

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
