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

package com.hbmspace.client.fog;

import com.mojang.blaze3d.systems.RenderSystem;
import net.caffeinemc.mods.sodium.client.util.FogParameters;
import net.minecraft.client.renderer.fog.FogData;
import net.minecraft.client.renderer.fog.FogRenderer;

public final class SodiumFogBridge {
    private SodiumFogBridge() {}

    public static void captureStored(FogRenderer renderer, FogData data) {
        capture(
                ((net.caffeinemc.mods.sodium.client.util.FogStorage) (renderer))
                        .sodium$getFogParameters(),
                ((com.hbmspace.client.fog.TerrainFogPayload) (data)),
                ((com.hbmspace.client.fog.TerrainFogWriter) (renderer)));
    }

    public static Object capture(
            Object original, TerrainFogPayload source, TerrainFogWriter writer) {
        FogParameters parameters = (FogParameters) original;
        boolean enabled = writer.hbmspace$fogEnabled();
        int flags = enabled ? source.hbmspace$flags() : 0;
        if ((flags & TerrainExpFog.EXP) != 0) {
            float cull =
                    (flags & TerrainExpFog.DARKNESS) != 0
                            ? Math.min(parameters.renderEnd(), parameters.environmentalEnd())
                            : parameters.renderEnd();
            ((com.hbmspace.client.fog.BoundTerrainFog) (java.lang.Object) (parameters))
                    .hbmspace$cullDistance(cull);
        }
        ((com.hbmspace.client.fog.TerrainFogPayload) (java.lang.Object) (parameters))
                .hbmspace$payload(enabled ? source.hbmspace$density() : 0F, flags);
        ((com.hbmspace.client.fog.BoundTerrainFog) (java.lang.Object) (parameters))
                .hbmspace$writer(writer);
        var buffer = writer.hbmspace$worldFog();
        ((com.hbmspace.client.fog.BoundTerrainFog) (java.lang.Object) (parameters))
                .hbmspace$binding(buffer.buffer(), buffer.offset(), buffer.length());
        return parameters;
    }

    public static Object project(Object original, BoundTerrainFog source) {
        TerrainFogWriter writer = source.hbmspace$writer();
        if (writer != null) {
            var actual = RenderSystem.getShaderFog();
            assert actual.buffer() == source.hbmspace$buffer()
                    && actual.offset() == source.hbmspace$offset()
                    && actual.length() == source.hbmspace$length();
            assert Float.floatToIntBits(writer.hbmspace$writtenDensity())
                    == Float.floatToIntBits(source.hbmspace$density());
            assert writer.hbmspace$writtenFlags() == source.hbmspace$flags();
        } else {
            assert source.hbmspace$density() == 0F && source.hbmspace$flags() == 0;
        }
        ((TerrainFogPayload) original)
                .hbmspace$payload(source.hbmspace$density(), source.hbmspace$flags());
        return original;
    }
}
