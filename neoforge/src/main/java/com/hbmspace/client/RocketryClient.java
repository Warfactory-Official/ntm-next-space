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

import com.hbm.client.ClientRegistry;
import com.hbmspace.client.gui.DriveProcessorScreen;
import com.hbmspace.client.gui.LaunchPadRocketScreen;
import com.hbmspace.client.gui.RocketAssemblyScreen;
import com.hbmspace.client.gui.SatelliteSettingsScreen;
import com.hbmspace.client.gui.StardarMapRenderState;
import com.hbmspace.client.gui.StardarMapRenderer;
import com.hbmspace.client.gui.StardarScreen;
import com.hbmspace.client.render.CombatDropPodRenderer;
import com.hbmspace.client.render.RocketCustomRenderer;
import com.hbmspace.entity.RocketryEntitiesSpace;
import com.hbmspace.inventory.container.RocketryMenusSpace;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import java.util.function.Consumer;
import net.minecraft.client.renderer.entity.NoopRenderer;

public final class RocketryClient {

    private RocketryClient() {}

    public static void registerMenuScreens(ClientRegistry.MenuScreenRegistrar reg) {
        reg.accept(RocketryMenusSpace.MACHINE_ROCKET_ASSEMBLY.get(), RocketAssemblyScreen::new);
        reg.accept(RocketryMenusSpace.LAUNCH_PAD_ROCKET.get(), LaunchPadRocketScreen::new);
        reg.accept(RocketryMenusSpace.MACHINE_DRIVE_PROCESSOR.get(), DriveProcessorScreen::new);
        reg.accept(RocketryMenusSpace.MACHINE_STARDAR.get(), StardarScreen::new);
        reg.accept(RocketryMenusSpace.SATELLITE_SETTINGS.get(), SatelliteSettingsScreen::new);
    }

    public static void registerRenderPipelines(Consumer<RenderPipeline> reg) {
        StardarMapRenderer.forEachPipeline(reg);
    }

    public static void registerPictureInPictureRenderers(
            SpaceClient.PictureInPictureRegistrar reg) {
        reg.accept(StardarMapRenderState.class, StardarMapRenderer::new);
    }

    public static void registerEntityRenderers(ClientRegistry.EntityRendererRegistrar reg) {
        reg.accept(RocketryEntitiesSpace.RIDEABLE_ROCKET.get(), RocketCustomRenderer::new);
        reg.accept(RocketryEntitiesSpace.RIDEABLE_ROCKET_DUMMY.get(), NoopRenderer::new);
        reg.accept(RocketryEntitiesSpace.COMBAT_DROP_POD.get(), CombatDropPodRenderer::new);
    }
}
