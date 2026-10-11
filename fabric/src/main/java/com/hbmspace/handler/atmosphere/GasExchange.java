// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.handler.atmosphere;

import com.hbmspace.dim.CelestialBody;
import com.hbmspace.dim.trait.CelestialBodyTrait;
import java.util.Map;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.material.Fluid;
import org.jspecify.annotations.Nullable;

public final class GasExchange {

    private GasExchange() {}

    public static void release(ServerLevel level, @Nullable Fluid fluid, double mB) {
        if (fluid != null && AtmosphereText.isGas(fluid)) CelestialBody.emitGas(level, fluid, mB);
    }

    public static void capture(ServerLevel level, @Nullable Fluid fluid, double mB) {
        if (fluid != null && AtmosphereText.isGas(fluid))
            CelestialBody.consumeGas(level, fluid, mB);
    }

    public static void release(
            Map<Class<? extends CelestialBodyTrait>, CelestialBodyTrait> traits,
            Fluid fluid,
            double mB) {
        if (AtmosphereText.isGas(fluid)) CelestialBody.emitGas(traits, fluid, mB);
    }

    public static void capture(
            Map<Class<? extends CelestialBodyTrait>, CelestialBodyTrait> traits,
            Fluid fluid,
            double mB) {
        if (AtmosphereText.isGas(fluid)) CelestialBody.consumeGas(traits, fluid, mB);
    }
}
