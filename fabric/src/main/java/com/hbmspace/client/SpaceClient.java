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

package com.hbmspace.client;

import com.google.common.base.Suppliers;
import com.hbm.client.ClientRegistry;
import com.hbm.client.model.Meshes;
import com.hbm.client.render.BlockEntityShading;
import com.hbm.client.render.BomberStyles;
import com.hbm.integration.recipeviewer.RecipePages;
import com.hbm.platform.Platform;
import com.hbmspace.HbmSpace;
import com.hbmspace.blocks.StationBlockEntitiesSpace;
import com.hbmspace.blocks.TerrainBlockEntitiesSpace;
import com.hbmspace.blocks.TerrainBlocksSpace;
import com.hbmspace.client.fog.DistantHorizonsFog;
import com.hbmspace.client.gui.MilkReformerScreen;
import com.hbmspace.client.gui.OrbitalStationComputerScreen;
import com.hbmspace.client.gui.OrbitalStationLauncherScreen;
import com.hbmspace.client.gui.TransporterLinkerScreen;
import com.hbmspace.client.gui.TransporterRocketScreen;
import com.hbmspace.client.gui.WarControllerScreen;
import com.hbmspace.client.particle.DuoDecParticle;
import com.hbmspace.client.particle.DustParticle;
import com.hbmspace.client.particle.GlowParticle;
import com.hbmspace.client.render.AtmosphereRenders;
import com.hbmspace.client.render.HTRNeoRenderer;
import com.hbmspace.client.render.OrreryRenderTypes;
import com.hbmspace.client.render.RocketryRenders;
import com.hbmspace.client.render.SkyRenders;
import com.hbmspace.client.render.StationRenders;
import com.hbmspace.client.render.TerrainRenders;
import com.hbmspace.client.render.TrinketFiguresSpace;
import com.hbmspace.client.render.Vol2Renderer;
import com.hbmspace.client.render.XenonThrusterRenderer;
import com.hbmspace.client.render.mob.MobRenders;
import com.hbmspace.client.render.mob.SiegeRenders;
import com.hbmspace.client.sky.CelestialLightmapTint;
import com.hbmspace.client.sky.SkyPipelines;
import com.hbmspace.client.sky.SpaceSkies;
import com.hbmspace.dim.CelestialBody;
import com.hbmspace.dim.SolarSystemWorldSavedData;
import com.hbmspace.dim.orbit.OrbitalStation;
import com.hbmspace.entity.AtmosphereEntitiesSpace;
import com.hbmspace.handler.PlayerAuras;
import com.hbmspace.integration.recipeviewer.AlkylationPage;
import com.hbmspace.integration.recipeviewer.AtmosphereExtractionPage;
import com.hbmspace.integration.recipeviewer.AtmosphericChemistryPage;
import com.hbmspace.integration.recipeviewer.BedrockDrillingPage;
import com.hbmspace.integration.recipeviewer.DairyPage;
import com.hbmspace.inventory.container.ModMenusSpace;
import com.hbmspace.inventory.container.StationMenusSpace;
import com.hbmspace.inventory.container.WarMenusSpace;
import com.hbmspace.particle.SpaceParticles;
import com.hbmspace.util.AstronomyUtil;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.ParticleGroup;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.entity.NoopRenderer;
import net.minecraft.client.renderer.state.gui.pip.PictureInPictureRenderState;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;

public final class SpaceClient {

    public static final Identifier GUN_CLIENT_SETUP_ID =
            Identifier.fromNamespaceAndPath(HbmSpace.MOD_ID, "gun_client_setup");

    private SpaceClient() {}

    public static void setupGunClient() {
        MobRenders.registerBulletDrawers();
        SiegeRenders.registerBulletDrawers();
    }

    public static void init() {
        if (Platform.isModLoaded("distanthorizons")) DistantHorizonsFog.install();
        PlayerAuras.CLIENT_START = DuodecAura::playerTick;
        BomberStyles.register(
                9,
                Suppliers.memoize(
                        () ->
                                Meshes.load(
                                        Identifier.fromNamespaceAndPath(
                                                HbmSpace.MOD_ID, "models/737.obj"))),
                Identifier.fromNamespaceAndPath(
                        HbmSpace.MOD_ID, "textures/models/airlinerplaceholder.png"),
                30F / 3.1F,
                180F);
        RecipePages.addon(new DairyPage());
        TrinketFiguresSpace.register();
        StationEngineEffects.install();
        FloraClientEffects.install();
        RecipePages.addon(new AtmosphereExtractionPage());
        RecipePages.addon(new BedrockDrillingPage());
        RecipePages.addon(new AtmosphericChemistryPage());
        RecipePages.addon(new AlkylationPage());
        IndustryClient.init();
        SpaceSkies.register();
        InvasionWarning.install();
        SiegeRenders.registerSocketCentrepieces();
    }

    public static void clientTick() {
        SpaceSkies.clientTick();
        StationHum.clientTick();
    }

    public static void disconnected() {
        SpaceSkies.disconnected();
        SolarSystemWorldSavedData.updateClientTraits(null);
        OrbitalStation.clientStation = new OrbitalStation(CelestialBody.getBody(Level.OVERWORLD));
        OrbitalStation.orbitingStations.clear();
        StationHum.disconnected();
        AstronomyUtil.TIME_MULTIPLIER = 1;
    }

    public static void registerFlywheelVisualizers() {
        AtmosphereRenders.registerVisualizers();
        StationRenders.registerVisualizers();
        SkyRenders.registerVisualizers();
        RocketryRenders.registerVisualizers();
        MobRenders.registerVisualizers();
        IndustryClient.registerVisualizers();
        BombClient.registerVisualizers();
        SiegeRenders.registerVisualizers();
        TerrainRenders.registerVisualizers();
    }

    public static void registerRenderPipelines(Consumer<RenderPipeline> reg) {
        SkyPipelines.forEach(reg);
        reg.accept(CelestialLightmapTint.PIPELINE);
        OrreryRenderTypes.forEach(reg);
        RocketryClient.registerRenderPipelines(reg);
        IndustryClient.registerRenderPipelines(reg);
        SiegeRenders.registerRenderPipelines(reg);
    }

    public interface PictureInPictureRegistrar {
        <T extends PictureInPictureRenderState> void accept(
                Class<T> state, Supplier<PictureInPictureRenderer<T>> factory);
    }

    public static void registerPictureInPictureRenderers(PictureInPictureRegistrar reg) {
        RocketryClient.registerPictureInPictureRenderers(reg);
    }

    public static void registerMenuScreens(ClientRegistry.MenuScreenRegistrar reg) {
        reg.accept(ModMenusSpace.MACHINE_MILK_REFORMER.get(), MilkReformerScreen::new);
        reg.accept(
                StationMenusSpace.ORBITAL_STATION_COMPUTER.get(),
                OrbitalStationComputerScreen::new);
        reg.accept(StationMenusSpace.TRANSPORTER_ROCKET.get(), TransporterRocketScreen::new);
        reg.accept(StationMenusSpace.TRANSPORTER_LINKER.get(), TransporterLinkerScreen::new);
        reg.accept(
                StationMenusSpace.ORBITAL_STATION_LAUNCHER.get(),
                OrbitalStationLauncherScreen::new);
        reg.accept(WarMenusSpace.WAR_CONTROLLER.get(), WarControllerScreen::new);
        RocketryClient.registerMenuScreens(reg);
        IndustryClient.registerMenuScreens(reg);
        BombClient.registerMenuScreens(reg);
    }

    public static void registerBlockEntityRenderers(
            ClientRegistry.BlockEntityRendererRegistrar reg) {
        BlockEntityShading.claim(HbmSpace.MOD_ID);
        StationRenders.registerBlockEntityRenderers(reg);
        reg.accept(
                StationBlockEntitiesSpace.XENON_THRUSTER.get(),
                context -> new XenonThrusterRenderer());
        reg.accept(StationBlockEntitiesSpace.HTRF4_NEO.get(), context -> new HTRNeoRenderer());
        reg.accept(
                TerrainBlockEntitiesSpace.LIGHTNING_VOLCANO.get(), context -> new Vol2Renderer());
        AtmosphereRenders.registerBlockEntityRenderers(reg);
        SkyRenders.registerBlockEntityRenderers(reg);
        RocketryRenders.registerBlockEntityRenderers(reg);
        IndustryClient.registerBlockEntityRenderers(reg);
        BombClient.registerBlockEntityRenderers(reg);
    }

    public static void registerEntityRenderers(ClientRegistry.EntityRendererRegistrar reg) {
        reg.accept(AtmosphereEntitiesSpace.DEPRESS.get(), NoopRenderer::new);
        RocketryClient.registerEntityRenderers(reg);
        MobRenders.registerEntityRenderers(reg);
        BombClient.registerEntityRenderers(reg);
        SiegeRenders.registerEntityRenderers(reg);
    }

    public interface ParticleProviderRegistrar {
        <T extends ParticleOptions> void accept(ParticleType<T> type, ParticleProvider<T> provider);
    }

    public static void registerParticleProviders(ParticleProviderRegistrar reg) {
        reg.accept(SpaceParticles.DUST_STORM.get(), new DustParticle.Provider());
        reg.accept(SpaceParticles.FLARE.get(), new GlowParticle.Provider());
        reg.accept(SpaceParticles.DUOEWE.get(), new DuoDecParticle.Provider(true));
        reg.accept(SpaceParticles.DUODEC.get(), new DuoDecParticle.Provider(false));
    }

    public static void registerParticleGroups(
            BiConsumer<ParticleRenderType, Function<ParticleEngine, ParticleGroup<?>>> reg) {
        reg.accept(DustParticle.DUST_GROUP, DustParticle::group);
        reg.accept(GlowParticle.GROUP, GlowParticle::group);
        reg.accept(DuoDecParticle.TUNE_GROUP, DuoDecParticle::tuneGroup);
        reg.accept(DuoDecParticle.JESUS_GROUP, DuoDecParticle::jesusGroup);
    }

    public static void registerBlockTintSources(ClientRegistry.BlockTintRegistrar reg) {
        reg.accept(List.of(FallingTintSource.INSTANCE), TerrainBlocksSpace.LAYTHE_SILT.get());
    }
}
