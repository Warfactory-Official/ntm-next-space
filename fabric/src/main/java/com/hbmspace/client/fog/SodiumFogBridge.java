// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.client.fog;

import com.mojang.blaze3d.systems.RenderSystem;
import net.caffeinemc.mods.sodium.client.util.FogParameters;
import net.caffeinemc.mods.sodium.client.util.FogStorage;
import net.minecraft.client.renderer.fog.FogData;
import net.minecraft.client.renderer.fog.FogRenderer;

public final class SodiumFogBridge {
    private SodiumFogBridge() {}

    public static void captureStored(FogRenderer renderer, FogData data) {
        capture(
                ((FogStorage) renderer).sodium$getFogParameters(),
                (TerrainFogPayload) data,
                (TerrainFogWriter) renderer);
    }

    public static Object capture(
            Object original, TerrainFogPayload source, TerrainFogWriter writer) {
        FogParameters parameters = (FogParameters) original;
        BoundTerrainFog snapshot = (BoundTerrainFog) (Object) parameters;
        boolean enabled = writer.hbmspace$fogEnabled();
        int flags = enabled ? source.hbmspace$flags() : 0;
        if ((flags & TerrainExpFog.EXP) != 0) {
            float cull =
                    (flags & TerrainExpFog.DARKNESS) != 0
                            ? Math.min(parameters.renderEnd(), parameters.environmentalEnd())
                            : parameters.renderEnd();
            snapshot.hbmspace$cullDistance(cull);
        }
        snapshot.hbmspace$payload(enabled ? source.hbmspace$density() : 0F, flags);
        snapshot.hbmspace$writer(writer);
        var buffer = writer.hbmspace$worldFog();
        snapshot.hbmspace$binding(buffer.buffer(), buffer.offset(), buffer.length());
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
