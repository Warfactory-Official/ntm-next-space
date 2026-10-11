// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.client.sky;

import com.hbm.api.client.ShaderPacks;
import com.hbm.api.client.WeatherTints;
import com.hbmspace.dim.WorldProviderCelestial;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.RenderPipelines;
import org.jspecify.annotations.Nullable;

public final class CelestialWeatherTint implements WeatherTints.Provider {

    public static void register() {
        WeatherTints.register(new CelestialWeatherTint());
    }

    @Override
    public WeatherTints.@Nullable Tint tint(ClientLevel level) {
        WorldProviderCelestial provider = WorldProviderCelestial.of(level);
        if (provider == null) return null;

        int rain = provider.getWeatherColor();
        int snow = provider.getSnowColor();
        if (ShaderPacks.inUse()) {
            return new WeatherTints.Tint(
                    RenderPipelines.WEATHER_DEPTH_WRITE,
                    RenderPipelines.WEATHER_NO_DEPTH_WRITE,
                    rain,
                    snow);
        }
        return new WeatherTints.Tint(
                SkyPipelines.WEATHER_TINTED,
                SkyPipelines.WEATHER_TINTED_NO_DEPTH_WRITE,
                rain,
                snow);
    }
}
