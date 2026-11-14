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

package com.hbmspace.dim;

import com.hbm.api.environment.AtmosphereLayers;
import com.hbm.handler.ImpactWorldHandler;
import com.hbm.saveddata.TomSaveData;
import com.hbmspace.dim.orbit.OrbitalStation;
import com.hbmspace.dim.orbit.WorldProviderOrbit;
import com.hbmspace.dim.trait.CBT_Atmosphere;
import com.hbmspace.handler.atmosphere.ChunkAtmosphereHandler;
import com.hbmspace.handler.atmosphere.ChunkAtmosphereManager;
import java.util.Objects;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.attribute.EnvironmentAttributeSystem;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.MoonPhase;
import net.minecraft.world.phys.Vec3;

public final class CelestialEnvironment implements AtmosphereLayers.Provider {

    @Override
    public boolean claims(Level level) {
        return WorldProviderCelestial.of(level) != null || CelestialBody.inOrbit(level);
    }

    @Override
    public void addLayers(EnvironmentAttributeSystem.Builder builder, Level level) {
        builder.addPositionalLayer(
                EnvironmentAttributes.WATER_EVAPORATES,
                (value, pos, biomes) -> evaporates(level, BlockPos.containing(pos)));
        if (CelestialBody.inOrbit(level)) {
            if (level.isClientSide()) addOrbitLayers(builder, level);
            return;
        }

        Inputs inputs = new Inputs(Objects.requireNonNull(WorldProviderCelestial.of(level)), level);
        WorldProviderCelestial provider = inputs.provider;

        builder.addPositionalLayer(
                EnvironmentAttributes.SKY_COLOR,
                (value, pos, biomes) -> provider.getSkyColor(level, value, inputs.sky(pos)));
        builder.addPositionalLayer(
                EnvironmentAttributes.FOG_COLOR,
                (value, pos, biomes) -> provider.getFogColor(level, inputs.sky(pos), pos.y));
        builder.addPositionalLayer(
                EnvironmentAttributes.SUNRISE_SUNSET_COLOR,
                (value, pos, biomes) -> provider.getSunriseColor(level, value, inputs.sky(pos)));
        builder.addTimeBasedLayer(
                EnvironmentAttributes.STAR_BRIGHTNESS,
                (value, tick) -> provider.getStarBrightness(value, inputs.dust()));
        builder.addPositionalLayer(
                EnvironmentAttributes.SKY_LIGHT_FACTOR,
                (value, pos, biomes) -> provider.getSunBrightness(value, inputs.sky(pos)));
        builder.addTimeBasedLayer(
                EnvironmentAttributes.CLOUD_COLOR, (value, tick) -> provider.getCloudColor(value));
        builder.addTimeBasedLayer(
                EnvironmentAttributes.MOON_PHASE, (value, tick) -> inputs.moonPhase(tick));
        provider.addLayers(builder, level);
    }

    private static boolean evaporates(Level level, BlockPos pos) {
        if (level instanceof ServerLevel server && server.getServer().isSameThread()) {
            ChunkAtmosphereHandler air = ChunkAtmosphereManager.proxy;
            if (air.hasLiquidPressure(air.getAtmosphere(server, pos))) return false;
        }
        if (CelestialBody.inOrbit(level)) return true;
        CBT_Atmosphere atmosphere = CelestialBody.getTrait(level, CBT_Atmosphere.class);
        return atmosphere == null || atmosphere.getPressure() <= 0.2F;
    }

    private static void addOrbitLayers(EnvironmentAttributeSystem.Builder builder, Level level) {
        OrbitInputs inputs = new OrbitInputs(level);
        builder.addTimeBasedLayer(
                EnvironmentAttributes.STAR_BRIGHTNESS,
                (value, tick) ->
                        WorldProviderOrbit.getStarBrightness(
                                OrbitalStation.clientStation, inputs.eclipse(), 0));
        builder.addTimeBasedLayer(
                EnvironmentAttributes.SKY_LIGHT_FACTOR,
                (value, tick) -> WorldProviderOrbit.getSunBrightness(inputs.eclipse()));
    }

    private static final class OrbitInputs {

        final Level level;

        private long eclipseTime = Long.MIN_VALUE;
        private double eclipse;

        OrbitInputs(Level level) {
            this.level = level;
        }

        double eclipse() {
            long time = level.getGameTime();
            if (time != eclipseTime) {
                eclipse =
                        WorldProviderOrbit.getSky(level, 0, OrbitalStation.clientStation).eclipse();
                eclipseTime = time;
            }
            return eclipse;
        }
    }

    private static final class Inputs {

        final WorldProviderCelestial provider;
        final Level level;

        private long eclipseTime = Long.MIN_VALUE;
        private double eclipse;
        private int moonTick = Integer.MIN_VALUE;
        private MoonPhase moonPhase = MoonPhase.FULL_MOON;

        Inputs(WorldProviderCelestial provider, Level level) {
            this.provider = provider;
            this.level = level;
        }

        WorldProviderCelestial.Sky sky(Vec3 pos) {
            long time = level.getGameTime();
            if (time != eclipseTime) {
                eclipse = provider.getEclipseAmount(level);
                eclipseTime = time;
            }

            float solarAngle =
                    level.environmentAttributes().getValue(EnvironmentAttributes.SUN_ANGLE, pos)
                            / 360.0F;
            return new WorldProviderCelestial.Sky(solarAngle, eclipse, dust(), fire());
        }

        MoonPhase moonPhase(int tick) {
            if (tick != moonTick) {
                moonPhase = provider.getMoonPhase(level);
                moonTick = tick;
            }
            return moonPhase;
        }

        float dust() {
            if (level instanceof ServerLevel server) {
                TomSaveData data = TomSaveData.published(server);
                return data == null ? 0 : data.dust;
            }
            return ImpactWorldHandler.getDustForClient(level);
        }

        float fire() {
            if (level instanceof ServerLevel server) {
                TomSaveData data = TomSaveData.published(server);
                return data == null ? 0 : data.fire;
            }
            return ImpactWorldHandler.getFireForClient(level);
        }
    }
}
