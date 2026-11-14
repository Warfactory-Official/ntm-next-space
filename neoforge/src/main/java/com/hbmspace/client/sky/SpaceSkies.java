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

import com.hbm.api.client.CustomSkies;
import com.hbmspace.dim.CelestialBody;
import com.hbmspace.dim.SolarSystemWorldSavedData;
import com.hbmspace.dim.SpaceDimensions;
import com.hbmspace.dim.WorldProviderCelestial;
import com.hbmspace.dim.trait.CBT_Invasion;
import com.hbmspace.dim.trait.CelestialBodyTrait;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.SkyRenderer;
import net.minecraft.client.renderer.state.level.SkyRenderState;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import org.jspecify.annotations.Nullable;

public final class SpaceSkies implements CustomSkies.Renderer {

    private final SkyProviderCelestial celestial = new SkyProviderCelestial();
    private final SkyProviderOrbit orbit = new SkyProviderOrbit();
    private final Map<ResourceKey<Level>, SkyProviderCelestial> bodies =
            Map.of(
                    SpaceDimensions.LAYTHE,
                    new SkyProviderLaytheSunset(),
                    SpaceDimensions.THATMO,
                    new SkyProviderThatmo());
    private @Nullable SkyProviderCelestial active;
    private @Nullable SkyDraws draws;

    public static void register() {
        CustomSkies.register(new SpaceSkies());
        CelestialClouds.register();
        CelestialWeatherTint.register();
        WeatherParticleTint.register();
    }

    @Override
    public boolean extract(
            ClientLevel level, float partialTicks, Camera camera, SkyRenderState state) {
        active = null;
        if (state.skybox != DimensionType.Skybox.OVERWORLD) return false;

        SkyProviderCelestial sky;
        if (WorldProviderCelestial.of(level) != null)
            sky = bodies.getOrDefault(level.dimension(), celestial);
        else if (CelestialBody.inOrbit(level)) sky = orbit;
        else return false;

        active = sky;
        sky.extract(level, partialTicks, camera, state);
        return true;
    }

    @Override
    public void render(SkyRenderer vanilla, SkyRenderState state) {
        if (draws == null) draws = new SkyDraws();
        Objects.requireNonNull(active).render(vanilla, state, draws);
        draws.endFrame();
    }

    @Override
    public void close() {
        celestial.close();
        orbit.close();
        bodies.values().forEach(SkyProviderCelestial::close);
        if (draws != null) {
            draws.close();
            draws = null;
        }
    }

    public static void clientTick() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.isPaused()) return;

        if (WorldProviderCelestial.of(mc.level) != null) {
            CelestialBody body = CelestialBody.getBody(mc.level);
            HashMap<Class<? extends CelestialBodyTrait>, CelestialBodyTrait> traits =
                    SolarSystemWorldSavedData.getClientTraits(body.name);
            if (traits != null
                    && traits.get(CBT_Invasion.class) instanceof CBT_Invasion invasion
                    && invasion.isInvading
                    && !invasion.paused
                    && mc.level.getRandom().nextInt(Math.max(1, 5 - invasion.wave)) == 0) {
                Meteors.addMeteor();
            }
        }

        Meteors.clientTick();
        DunaStormClient.tick(mc);
        EveFlashClient.tick(mc);
    }

    public static void disconnected() {
        Meteors.clear();
        DunaStormClient.clear();
        EveFlashClient.clear();
    }
}
