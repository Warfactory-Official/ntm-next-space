/*
 * SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright (C) 2026 movblock. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * You may not use this file under the Apache License except in
 * compliance with that License. You may obtain a copy at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Apache License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License for the specific language governing permissions
 * and limitations under the License.
 */

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
