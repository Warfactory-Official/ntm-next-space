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

package com.hbmspace.neoforge.client;

import com.hbm.client.ClientRegistry;
import com.hbmspace.HbmSpace;
import com.hbmspace.client.CelestialOreTooltip;
import com.hbmspace.client.SpaceClient;
import com.hbmspace.client.SpaceEffectHud;
import com.hbmspace.client.SpaceOxygenHud;
import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.AddClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.RegisterParticleGroupsEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.neoforge.client.event.RegisterPictureInPictureRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterRenderPipelinesEvent;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

@Mod(value = HbmSpace.MOD_ID, dist = Dist.CLIENT)
public final class HbmSpaceNeoForgeClient {
    public HbmSpaceNeoForgeClient(IEventBus modBus) {
        modBus.addListener((FMLClientSetupEvent event) -> SpaceClient.init());
        modBus.addListener(
                (AddClientReloadListenersEvent event) -> {
                    event.addListener(
                            SpaceClient.GUN_CLIENT_SETUP_ID,
                            (ResourceManagerReloadListener)
                                    resourceManager -> SpaceClient.setupGunClient());
                    event.addDependency(
                            ClientRegistry.GUN_CLIENT_SETUP_ID, SpaceClient.GUN_CLIENT_SETUP_ID);
                });
        modBus.addListener(
                (FMLClientSetupEvent event) ->
                        event.enqueueWork(SpaceClient::registerFlywheelVisualizers));
        modBus.addListener(
                (RegisterMenuScreensEvent event) ->
                        SpaceClient.registerMenuScreens(event::register));
        modBus.addListener(
                (EntityRenderersEvent.RegisterRenderers event) ->
                        SpaceClient.registerBlockEntityRenderers(
                                event::registerBlockEntityRenderer));
        modBus.addListener(
                (EntityRenderersEvent.RegisterRenderers event) ->
                        SpaceClient.registerEntityRenderers(event::registerEntityRenderer));
        modBus.addListener(
                (RegisterColorHandlersEvent.BlockTintSources event) ->
                        SpaceClient.registerBlockTintSources(event::register));
        modBus.addListener(
                (RegisterRenderPipelinesEvent event) ->
                        SpaceClient.registerRenderPipelines(event::registerPipeline));
        modBus.addListener(
                (RegisterPictureInPictureRenderersEvent event) ->
                        SpaceClient.registerPictureInPictureRenderers(event::register));
        modBus.addListener(
                (RegisterParticleProvidersEvent event) ->
                        SpaceClient.registerParticleProviders(event::registerSpecial));
        modBus.addListener(
                (RegisterParticleGroupsEvent event) ->
                        SpaceClient.registerParticleGroups(event::register));
        modBus.addListener(
                (RegisterGuiLayersEvent event) -> {
                    event.registerAbove(
                            VanillaGuiLayers.PLAYER_HEALTH,
                            SpaceEffectHud.TAINT_BAR,
                            (graphics, delta) -> {
                                Minecraft mc = Minecraft.getInstance();
                                if (!mc.gui.hud.isHidden() && mc.gameMode.canHurtPlayer())
                                    SpaceEffectHud.taintBar(graphics, delta);
                            });
                    event.registerBelow(
                            VanillaGuiLayers.CHAT,
                            SpaceEffectHud.FLASHBANG,
                            (graphics, delta) -> {
                                if (!Minecraft.getInstance().gui.hud.isHidden())
                                    SpaceEffectHud.flashbang(graphics, delta);
                            });
                });
        NeoForge.EVENT_BUS.addListener((ClientTickEvent.Post event) -> SpaceClient.clientTick());
        NeoForge.EVENT_BUS.addListener(
                (ClientPlayerNetworkEvent.LoggingOut event) -> SpaceClient.disconnected());
        NeoForge.EVENT_BUS.addListener(
                (ItemTooltipEvent event) ->
                        CelestialOreTooltip.append(event.getItemStack(), event.getToolTip()));
        NeoForge.EVENT_BUS.addListener(
                (RenderGuiLayerEvent.Pre event) -> {
                    Minecraft mc = Minecraft.getInstance();
                    if (!event.getName().equals(VanillaGuiLayers.AIR_LEVEL)
                            || mc.player == null
                            || !SpaceOxygenHud.replacesAir(mc.player)) {
                        return;
                    }
                    event.setCanceled(true);
                    var graphics = event.getGuiGraphics();
                    var hud = mc.gui.hud;
                    hud.rightHeight +=
                            SpaceOxygenHud.render(
                                    graphics,
                                    mc.player,
                                    graphics.guiWidth() / 2 + 91,
                                    graphics.guiHeight() - hud.rightHeight);
                });
    }
}
