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

package com.hbmspace.client.sky;

import com.hbmspace.dim.CelestialBody;
import com.hbmspace.dim.WorldProviderCelestial;
import com.hbmspace.dim.duna.WorldProviderDuna;
import com.hbmspace.dim.laythe.WorldProviderLaythe;
import com.hbmspace.dim.trait.CBT_Atmosphere;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.ARGB;
import net.minecraft.world.level.material.FogType;
import net.minecraft.world.phys.Vec3;

public final class CelestialFogTint {
    private CelestialFogTint() {}

    public static float density(ClientLevel level, Camera camera) {
        WorldProviderCelestial provider = WorldProviderCelestial.of(level);
        if (provider instanceof WorldProviderLaythe && camera.getFluidInCamera() == FogType.WATER)
            return .03F;
        if (provider instanceof WorldProviderDuna) {
            float storm = DunaStormClient.smoothed();
            if (storm >= .25F) return storm * storm * .075F;
        }
        CBT_Atmosphere atmosphere = CelestialBody.getTrait(level, CBT_Atmosphere.class);
        if (provider == null || atmosphere == null) return 0F;
        float pressure = (float) atmosphere.getPressure();
        return pressure <= 2F ? 0F : pressure * pressure * .002F;
    }

    public static Vec3 tint(float red, float green, float blue, int fog, float density) {
        float amount = density * 30F;
        if (amount <= .01F) return new Vec3(red, green, blue);
        return new Vec3(
                blend(red, ARGB.redFloat(fog), amount),
                blend(green, ARGB.greenFloat(fog), amount),
                blend(blue, ARGB.blueFloat(fog), amount));
    }

    private static float blend(double start, double end, double amount) {
        if (amount > 1D) return (float) end;
        return (float) (start + amount * (end - start));
    }
}
