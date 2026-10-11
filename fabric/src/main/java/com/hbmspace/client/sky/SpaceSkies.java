// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.client.sky;

import com.hbm.api.client.CustomSkies;
import com.hbmspace.dim.CelestialBody;
import com.hbmspace.dim.SolarSystemWorldSavedData;
import com.hbmspace.dim.SpaceDimensions;
import com.hbmspace.dim.WorldProviderCelestial;
import com.hbmspace.dim.trait.CBT_Invasion;
import com.hbmspace.dim.trait.CelestialBodyTrait;
import java.util.ArrayList;
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
        if (draws != null) {
            draws.close();
            draws = null;
        }
    }

    public static void clientTick() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.isPaused()) return;

        for (CelestialBody body : CelestialBody.getAllBodies()) {
            HashMap<Class<? extends CelestialBodyTrait>, CelestialBodyTrait> traits =
                    SolarSystemWorldSavedData.getClientTraits(body.name);
            if (traits != null) {
                for (CelestialBodyTrait trait : new ArrayList<>(traits.values())) {
                    trait.update(true, body);
                    if (trait instanceof CBT_Invasion invasion
                            && invasion.isInvading
                            && mc.level.getRandom().nextInt(Math.max(1, 5 - invasion.wave)) == 0) {
                        Meteors.addMeteor();
                    }
                }
            }
        }

        Meteors.clientTick();
        DunaStormClient.tick(mc);
        EveFlashClient.tick(mc);
    }
}
