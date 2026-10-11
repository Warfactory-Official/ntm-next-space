// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-FileCopyrightText: 2026 detree05
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.render.util;

import com.hbm.util.GameTime;
import com.hbmspace.client.sky.PlanetInfo;
import com.hbmspace.dim.CelestialBody;
import com.hbmspace.dim.CloudProviderCelestial;
import com.hbmspace.dim.WorldProviderCelestial;
import com.hbmspace.dim.trait.CBT_Atmosphere.FluidEntry;
import com.hbmspace.dim.trait.CBT_Atmosphere;
import com.hbmspace.dim.trait.CBT_Weather;
import com.hbmspace.handler.CelestialNukeShockHandler.ShockStatus;
import com.hbmspace.handler.CelestialNukeShockHandler;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public final class AtmosphereRenderUtil {

    private static final float OPAQUE_ATMOSPHERE_PRESSURE = 5.0F;
    private static final float CLOUDY_ATMOSPHERE_PRESSURE = 0.5F;
    private static final float DENSE_HAZE_PRESSURE = 3.0F;
    private static final float CLOUD_STORM_SMOOTHING_RATE = 6.0F;
    private static final String EVE_BODY_NAME = "eve";
    private static final float EVE_STRIKE_WINDOW = 8.0F;
    private static final float EVE_STRIKE_FADE_TIME = 5.0F;
    private static final float EVE_STRIKE_DELAY_RANGE = 2.4F;
    private static final float EVE_STRIKE_GATE_THRESHOLD = 0.82F;
    private static final double EVE_HASH_MULTIPLIER = 43758.5453123D;

    private static final Map<String, CloudStormFadeState> CLOUD_STORM_FADE_STATES = new HashMap<>();

    public static final int ATMOSPHERE_STYLE_CLEAR = 0;
    public static final int ATMOSPHERE_STYLE_CLOUDS = 1;
    public static final int ATMOSPHERE_STYLE_HAZE = 2;
    public static final int ATMOSPHERE_STYLE_GAS_BANDS = 3;
    public static final int LIGHTNING_MODE_STANDARD = 0;
    public static final int LIGHTNING_MODE_EVE = 1;

    private static final class CloudStormFadeState {

        private float darkness;
        private long lastSampleTime;

        private CloudStormFadeState(float darkness, long lastSampleTime) {
            this.darkness = darkness;
            this.lastSampleTime = lastSampleTime;
        }
    }

    private AtmosphereRenderUtil() {}

    private static float smoothCloudStormDarkness(CelestialBody body, float targetDarkness) {
        long now = GameTime.now();
        CloudStormFadeState state = CLOUD_STORM_FADE_STATES.get(body.name);
        if (state == null) {
            CLOUD_STORM_FADE_STATES.put(body.name, new CloudStormFadeState(targetDarkness, now));
            return targetDarkness;
        }

        float deltaSeconds = Mth.clamp((now - state.lastSampleTime) / 1000.0F, 0.0F, 0.5F);
        float blend = Mth.clamp(deltaSeconds * CLOUD_STORM_SMOOTHING_RATE, 0.0F, 1.0F);
        state.darkness += (targetDarkness - state.darkness) * blend;
        state.lastSampleTime = now;

        return state.darkness;
    }

    public static float getAtmosphereGlowAlpha(CelestialBody body) {
        if (body.gas != null) {
            return 0.35F;
        }

        CBT_Atmosphere atmosphere = body.getTrait(CBT_Atmosphere.class);
        if (atmosphere != null) {
            float pressure = Mth.clamp((float) atmosphere.getPressure(), 0.0F, 3.0F);
            if (pressure <= 0.02F) {
                return 0.0F;
            }
            return Mth.clamp(0.08F + pressure * 0.16F, 0.08F, 0.5F);
        }

        return 0.0F;
    }

    public static float getAtmosphereSurfaceAlpha(CelestialBody body) {
        return getAtmosphereDensity(body);
    }

    public static float getAtmospherePressure(CelestialBody body) {
        if (body.gas != null) {
            return OPAQUE_ATMOSPHERE_PRESSURE;
        }

        CBT_Atmosphere atmosphere = body.getTrait(CBT_Atmosphere.class);
        if (atmosphere == null) {
            return 0.0F;
        }

        return Math.max(0.0F, (float) atmosphere.getPressure());
    }

    public static float getAtmosphereDensity(CelestialBody body) {
        return Mth.clamp(getAtmospherePressure(body) / OPAQUE_ATMOSPHERE_PRESSURE, 0.0F, 1.0F);
    }

    public static int getAtmosphereStyle(CelestialBody body) {
        if (body.gas != null) {
            return ATMOSPHERE_STYLE_GAS_BANDS;
        }

        float pressure = getAtmospherePressure(body);
        if (pressure > DENSE_HAZE_PRESSURE) {
            return ATMOSPHERE_STYLE_HAZE;
        }
        if (pressure >= CLOUDY_ATMOSPHERE_PRESSURE) {
            return ATMOSPHERE_STYLE_CLOUDS;
        }

        return ATMOSPHERE_STYLE_CLEAR;
    }

    public static Vec3 getBodyAtmosphereColor(CelestialBody body) {
        if (body.gas != null) {
            return WorldProviderCelestial.getAtmosphereFluidColor(body.gas);
        }

        CBT_Atmosphere atmosphere = body.getTrait(CBT_Atmosphere.class);
        if (atmosphere != null && !atmosphere.fluids.isEmpty()) {
            double totalPressure = 0.0D;
            double r = 0.0D;
            double g = 0.0D;
            double b = 0.0D;

            for (FluidEntry entry : atmosphere.fluids) {
                if (entry.pressure <= 0.0D) {
                    continue;
                }

                Vec3 fluidColor = WorldProviderCelestial.getAtmosphereFluidColor(entry.fluid);
                r += fluidColor.x * entry.pressure;
                g += fluidColor.y * entry.pressure;
                b += fluidColor.z * entry.pressure;
                totalPressure += entry.pressure;
            }

            if (totalPressure > 0.0D) {
                return new Vec3(
                        Mth.clamp(r / totalPressure, 0.0D, 1.0D),
                        Mth.clamp(g / totalPressure, 0.0D, 1.0D),
                        Mth.clamp(b / totalPressure, 0.0D, 1.0D));
            }
        }

        return new Vec3(1.0D, 1.0D, 1.0D);
    }

    public static Vec3 getBodyCloudColor(CelestialBody body) {
        Vec3 baseClouds = new Vec3(1.0D, 1.0D, 1.0D);

        if (body.gas != null) {
            Vec3 gasColor = getBodyAtmosphereColor(body);
            double gasPeak = Math.max(gasColor.x, Math.max(gasColor.y, gasColor.z));
            if (gasPeak <= 0.0D) {
                return new Vec3(1.0D, 1.0D, 1.0D);
            }

            return new Vec3(
                    Mth.clamp(gasColor.x / gasPeak, 0.0D, 1.0D),
                    Mth.clamp(gasColor.y / gasPeak, 0.0D, 1.0D),
                    Mth.clamp(gasColor.z / gasPeak, 0.0D, 1.0D));
        }

        CBT_Atmosphere atmosphere = body.getTrait(CBT_Atmosphere.class);
        return CloudProviderCelestial.getTintedCloudColor(atmosphere, baseClouds);
    }

    public static float getBodyCloudTintStrength(CelestialBody body) {
        if (body.gas != null) {
            return 1.0F;
        }

        CBT_Atmosphere atmosphere = body.getTrait(CBT_Atmosphere.class);
        return CloudProviderCelestial.getCloudTintStrength(atmosphere);
    }

    public static float getBodyCloudStormDarkness(CelestialBody body, float partialTicks) {
        CBT_Weather weather = body.getTrait(CBT_Weather.class);
        float targetDarkness = 0.0F;

        if (weather != null && CBT_Weather.supportsWeather(body)) {
            float rainStrength = weather.getRainStrength(partialTicks);
            float thunderStrength = weather.getThunderStrength(partialTicks);
            float rainStateDarkness = weather.raining ? 0.22F : 0.0F;
            float thunderStateDarkness = weather.thundering ? 0.48F : 0.0F;
            float animatedDarkness = rainStrength * 0.35F + thunderStrength * 0.22F;
            targetDarkness =
                    Mth.clamp(
                            Math.max(rainStateDarkness + thunderStateDarkness, animatedDarkness),
                            0.0F,
                            0.9F);
        }

        return smoothCloudStormDarkness(body, targetDarkness);
    }

    public static float getBodyCloudLightningStrength(CelestialBody body, float partialTicks) {
        CBT_Weather weather = body.getTrait(CBT_Weather.class);
        if (weather == null || !CBT_Weather.supportsWeather(body) || !weather.canSpawnLightning) {
            return 0.0F;
        }

        float lightningActivity = CBT_Weather.getLightningActivityFactor(body);
        if (isEveLightningBody(body)) {
            return Mth.clamp(lightningActivity, 0.0F, 1.0F);
        }

        return Mth.clamp(weather.getThunderStrength(partialTicks) * lightningActivity, 0.0F, 1.0F);
    }

    public static int getBodyLightningMode(CelestialBody body) {
        return isEveLightningBody(body) ? LIGHTNING_MODE_EVE : LIGHTNING_MODE_STANDARD;
    }

    public static float getBodyEveFlashStrength(CelestialBody body, float atmosphereTime) {
        if (!isEveLightningBody(body)) {
            return 0.0F;
        }

        return getEveFlashStrength(atmosphereTime);
    }

    private static boolean isEveLightningBody(CelestialBody body) {
        return EVE_BODY_NAME.equals(body.name);
    }

    private static float getEveFlashStrength(float atmosphereTime) {
        float eventIndex = Mth.floor(atmosphereTime / EVE_STRIKE_WINDOW);
        float eventTime = atmosphereTime - eventIndex * EVE_STRIKE_WINDOW;
        float strikeDelay = eveHash(eventIndex, 41.9F) * EVE_STRIKE_DELAY_RANGE;
        if (eventTime < strikeDelay) {
            return 0.0F;
        }

        if (eveHash(eventIndex, 313.7F) < EVE_STRIKE_GATE_THRESHOLD) {
            return 0.0F;
        }

        return Mth.clamp(getEveStrikeFade(Math.max(eventTime - strikeDelay, 0.0F)), 0.0F, 1.0F);
    }

    private static float getEveStrikeFade(float eventTime) {
        float strikeIntro = smoothstep(0.0F, 0.14F, eventTime);
        float strikeFade = 1.0F - smoothstep(0.16F, EVE_STRIKE_FADE_TIME, eventTime);
        return strikeIntro * strikeFade;
    }

    private static float eveHash(float x, float y) {
        double value = Math.sin(x * 127.1D + y * 311.7D) * EVE_HASH_MULTIPLIER;
        return frac(value);
    }

    private static float smoothstep(float edge0, float edge1, float value) {
        if (edge0 == edge1) {
            return value < edge0 ? 0.0F : 1.0F;
        }

        float t = Mth.clamp((value - edge0) / (edge1 - edge0), 0.0F, 1.0F);
        return t * t * (3.0F - 2.0F * t);
    }

    private static float frac(double value) {
        return (float) (value - Math.floor(value));
    }

    public static void applyNukeShockUniforms(
            PlanetInfo info, List<ShockStatus> shocks, double currentTime) {
        int shockCount = Math.min(shocks.size(), CelestialNukeShockHandler.MAX_ACTIVE_SHOCKS);
        info.nukeShockCount = shockCount;

        for (int i = 0; i < shockCount; i++) {
            ShockStatus shock = shocks.get(i);
            info.nukeShockTime[i] = (float) (currentTime - shock.time);
            info.nukeShockCenterX[i] = shock.centerX;
            info.nukeShockCenterY[i] = shock.centerY;
            info.nukeShockStrength[i] = shock.strength;
        }
    }
}
