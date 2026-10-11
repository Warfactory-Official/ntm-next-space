// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.client;

import com.hbm.client.ClientRegistry;
import com.hbmspace.client.gui.ScreenLaunchPadRocket;
import com.hbmspace.client.gui.ScreenMachineDriveProcessor;
import com.hbmspace.client.gui.ScreenMachineRocketAssembly;
import com.hbmspace.client.gui.ScreenMachineStardar;
import com.hbmspace.client.gui.ScreenSatelliteSettings;
import com.hbmspace.client.gui.StardarMapRenderState;
import com.hbmspace.client.gui.StardarMapRenderer;
import com.hbmspace.client.render.RenderCombatDropPod;
import com.hbmspace.client.render.RenderRocketCustom;
import com.hbmspace.entity.RocketryEntitiesSpace;
import com.hbmspace.inventory.container.RocketryMenusSpace;
import com.hbmspace.saveddata.satellites.SatelliteItems;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.NoopRenderer;

public final class RocketryClient {

    private RocketryClient() {}

    public static void init() {
        SatelliteItems.openSettings =
                () -> {
                    Minecraft minecraft = Minecraft.getInstance();
                    minecraft.gui.setScreen(new ScreenSatelliteSettings(minecraft.player));
                };
    }

    public static void registerMenuScreens(ClientRegistry.MenuScreenRegistrar reg) {
        reg.accept(
                RocketryMenusSpace.MACHINE_ROCKET_ASSEMBLY.get(), ScreenMachineRocketAssembly::new);
        reg.accept(RocketryMenusSpace.LAUNCH_PAD_ROCKET.get(), ScreenLaunchPadRocket::new);
        reg.accept(
                RocketryMenusSpace.MACHINE_DRIVE_PROCESSOR.get(), ScreenMachineDriveProcessor::new);
        reg.accept(RocketryMenusSpace.MACHINE_STARDAR.get(), ScreenMachineStardar::new);
    }

    public static void registerRenderPipelines(Consumer<RenderPipeline> reg) {
        StardarMapRenderer.forEachPipeline(reg);
    }

    public static void registerPictureInPictureRenderers(
            SpaceClient.PictureInPictureRegistrar reg) {
        reg.accept(StardarMapRenderState.class, StardarMapRenderer::new);
    }

    public static void registerEntityRenderers(ClientRegistry.EntityRendererRegistrar reg) {
        reg.accept(RocketryEntitiesSpace.RIDEABLE_ROCKET.get(), RenderRocketCustom::new);
        reg.accept(RocketryEntitiesSpace.RIDEABLE_ROCKET_DUMMY.get(), NoopRenderer::new);
        reg.accept(RocketryEntitiesSpace.COMBAT_DROP_POD.get(), RenderCombatDropPod::new);
    }
}
