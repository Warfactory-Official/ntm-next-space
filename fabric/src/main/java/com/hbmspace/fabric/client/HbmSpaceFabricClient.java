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

package com.hbmspace.fabric.client;

import com.hbm.client.ClientRegistry;
import com.hbmspace.client.CelestialOreTooltip;
import com.hbmspace.client.SpaceClient;
import com.hbmspace.client.SpaceEffectHud;
import com.hbmspace.client.SpaceOxygenHud;
import com.hbmspace.client.model.CoralModel;
import com.hbmspace.client.model.KelpModel;
import java.util.function.Supplier;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.particle.v1.ParticleGroupRegistry;
import net.fabricmc.fabric.api.client.particle.v1.ParticleProviderRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.BlockColorRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.PictureInPictureRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.renderer.state.gui.pip.PictureInPictureRenderState;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.world.entity.player.Player;

public final class HbmSpaceFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        SpaceClient.init();
        SpaceClient.registerMenuScreens(MenuScreens::register);
        SpaceClient.registerBlockEntityRenderers(BlockEntityRendererRegistry::register);
        SpaceClient.registerEntityRenderers(EntityRendererRegistry::register);
        ResourceLoader.get(PackType.CLIENT_RESOURCES)
                .registerReloadListener(
                        SpaceClient.GUN_CLIENT_SETUP_ID,
                        (ResourceManagerReloadListener)
                                resourceManager -> SpaceClient.setupGunClient());
        ResourceLoader.get(PackType.CLIENT_RESOURCES)
                .addListenerOrdering(
                        ClientRegistry.GUN_CLIENT_SETUP_ID, SpaceClient.GUN_CLIENT_SETUP_ID);
        SpaceClient.registerFlywheelVisualizers();
        SpaceClient.registerBlockTintSources(BlockColorRegistry::register);
        SpaceClient.registerPictureInPictureRenderers(
                new SpaceClient.PictureInPictureRegistrar() {
                    @Override
                    public <T extends PictureInPictureRenderState> void accept(
                            Class<T> state, Supplier<PictureInPictureRenderer<T>> factory) {
                        PictureInPictureRendererRegistry.register(context -> factory.get());
                    }
                });
        SpaceClient.registerParticleProviders(
                new SpaceClient.ParticleProviderRegistrar() {
                    @Override
                    public <T extends ParticleOptions> void accept(
                            ParticleType<T> type, ParticleProvider<T> provider) {
                        ParticleProviderRegistry.getInstance().register(type, provider);
                    }
                });
        SpaceClient.registerParticleGroups(ParticleGroupRegistry::register);
        ClientTickEvents.END_CLIENT_TICK.register(client -> SpaceClient.clientTick());
        ClientPlayConnectionEvents.DISCONNECT.register(
                (listener, client) -> SpaceClient.disconnected());
        HudElementRegistry.replaceElement(
                VanillaHudElements.AIR_BAR,
                original ->
                        (graphics, deltaTracker) -> {
                            Player player = Minecraft.getInstance().player;
                            if (player != null && SpaceOxygenHud.replacesAir(player)) {
                                SpaceOxygenHud.render(
                                        graphics,
                                        player,
                                        graphics.guiWidth() / 2 + 91,
                                        SpaceOxygenHud.vanillaAirTop(player, graphics.guiHeight()));
                            } else {
                                original.extractRenderState(graphics, deltaTracker);
                            }
                        });
        HudElementRegistry.attachElementAfter(
                VanillaHudElements.HEALTH_BAR, SpaceEffectHud.TAINT_BAR, SpaceEffectHud::taintBar);
        HudElementRegistry.attachElementBefore(
                VanillaHudElements.CHAT, SpaceEffectHud.FLASHBANG, SpaceEffectHud::flashbang);
        KelpModel.FACTORY = FabricKelpModel::new;
        CoralModel.FACTORY = FabricCoralModel::new;
        ItemTooltipCallback.EVENT.register(
                (stack, context, flag, lines) -> CelestialOreTooltip.append(stack, lines));
    }
}
