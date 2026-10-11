// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.client;

import com.google.common.base.Suppliers;
import com.hbm.client.ClientRegistry;
import com.hbm.client.model.Meshes;
import com.hbm.client.render.BomberStyles;
import com.hbm.integration.recipeviewer.RecipePages;
import com.hbmspace.HbmSpace;
import com.hbmspace.blocks.StationBlockEntitiesSpace;
import com.hbmspace.blocks.TerrainBlockEntitiesSpace;
import com.hbmspace.blocks.TerrainBlocksSpace;
import com.hbmspace.client.gui.ScreenMachineMilkReformer;
import com.hbmspace.client.gui.ScreenOrbitalStationComputer;
import com.hbmspace.client.gui.ScreenOrbitalStationLauncher;
import com.hbmspace.client.gui.ScreenTransporterRocket;
import com.hbmspace.client.gui.ScreenWarController;
import com.hbmspace.client.particle.ParticleDuoDec;
import com.hbmspace.client.particle.ParticleDust;
import com.hbmspace.client.particle.ParticleGlow;
import com.hbmspace.client.render.AtmosphereRenders;
import com.hbmspace.client.render.OrreryRenderTypes;
import com.hbmspace.client.render.RenderHTRNeo;
import com.hbmspace.client.render.RenderVol2;
import com.hbmspace.client.render.RenderXenonThruster;
import com.hbmspace.client.render.RocketryRenders;
import com.hbmspace.client.render.SkyRenders;
import com.hbmspace.client.render.StationRenders;
import com.hbmspace.client.render.TerrainRenders;
import com.hbmspace.client.render.TrinketFiguresSpace;
import com.hbmspace.client.render.mob.MobRenders;
import com.hbmspace.client.render.mob.SiegeRenders;
import com.hbmspace.client.sky.CelestialLightmapTint;
import com.hbmspace.client.sky.SkyPipelines;
import com.hbmspace.client.sky.SpaceSkies;
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

public final class SpaceClient {

    public static final Identifier GUN_CLIENT_SETUP_ID =
            Identifier.fromNamespaceAndPath(HbmSpace.MOD_ID, "gun_client_setup");

    private SpaceClient() {}

    public static void setupGunClient() {
        MobRenders.registerBulletDrawers();
        SiegeRenders.registerBulletDrawers();
    }

    public static void init() {
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
        RocketryClient.init();
        InvasionWarning.install();
        SiegeRenders.registerSocketCentrepieces();
    }

    public static void clientTick() {
        SpaceSkies.clientTick();
        StationHum.tick();
    }

    public static void disconnected() {
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
        reg.accept(ModMenusSpace.MACHINE_MILK_REFORMER.get(), ScreenMachineMilkReformer::new);
        reg.accept(
                StationMenusSpace.ORBITAL_STATION_COMPUTER.get(),
                ScreenOrbitalStationComputer::new);
        reg.accept(StationMenusSpace.TRANSPORTER_ROCKET.get(), ScreenTransporterRocket::new);
        reg.accept(
                StationMenusSpace.ORBITAL_STATION_LAUNCHER.get(),
                ScreenOrbitalStationLauncher::new);
        reg.accept(WarMenusSpace.WAR_CONTROLLER.get(), ScreenWarController::new);
        RocketryClient.registerMenuScreens(reg);
        IndustryClient.registerMenuScreens(reg);
        BombClient.registerMenuScreens(reg);
    }

    public static void registerBlockEntityRenderers(
            ClientRegistry.BlockEntityRendererRegistrar reg) {
        StationRenders.registerBlockEntityRenderers(reg);
        reg.accept(
                StationBlockEntitiesSpace.XENON_THRUSTER.get(),
                context -> new RenderXenonThruster());
        reg.accept(StationBlockEntitiesSpace.HTRF4_NEO.get(), context -> new RenderHTRNeo());
        reg.accept(TerrainBlockEntitiesSpace.LIGHTNING_VOLCANO.get(), context -> new RenderVol2());
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
        reg.accept(SpaceParticles.DUST_STORM.get(), new ParticleDust.Provider());
        reg.accept(SpaceParticles.FLARE.get(), new ParticleGlow.Provider());
        reg.accept(SpaceParticles.DUOEWE.get(), new ParticleDuoDec.Provider(true));
        reg.accept(SpaceParticles.DUODEC.get(), new ParticleDuoDec.Provider(false));
    }

    public static void registerParticleGroups(
            BiConsumer<ParticleRenderType, Function<ParticleEngine, ParticleGroup<?>>> reg) {
        reg.accept(ParticleDust.DUST_GROUP, ParticleDust::group);
        reg.accept(ParticleGlow.GROUP, ParticleGlow::group);
        reg.accept(ParticleDuoDec.TUNE_GROUP, ParticleDuoDec::tuneGroup);
        reg.accept(ParticleDuoDec.JESUS_GROUP, ParticleDuoDec::jesusGroup);
    }

    public static void registerBlockTintSources(ClientRegistry.BlockTintRegistrar reg) {
        reg.accept(List.of(FallingTintSource.INSTANCE), TerrainBlocksSpace.LAYTHE_SILT.get());
    }
}
