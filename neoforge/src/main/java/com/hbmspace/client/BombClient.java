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
import com.hbmspace.blocks.BombBlocksSpace;
import com.hbmspace.client.gui.NukeAntimatterScreen;
import com.hbmspace.client.render.NukeAntimatterRenderer;
import com.hbmspace.client.render.flywheel.NukeAntimatterVisual;
import com.hbmspace.entity.BombEntitiesSpace;
import com.hbmspace.inventory.container.BombMenusSpace;
import dev.engine_room.flywheel.lib.visualization.SimpleBlockEntityVisualizer;
import net.minecraft.client.renderer.entity.NoopRenderer;

final class BombClient {

    private BombClient() {}

    static void registerMenuScreens(ClientRegistry.MenuScreenRegistrar reg) {
        reg.accept(BombMenusSpace.NUKE_ANTIMATTER.get(), NukeAntimatterScreen::new);
    }

    static void registerBlockEntityRenderers(ClientRegistry.BlockEntityRendererRegistrar reg) {
        reg.accept(
                BombBlocksSpace.NUKE_ANTIMATTER_BE.get(), context -> new NukeAntimatterRenderer());
    }

    static void registerEntityRenderers(ClientRegistry.EntityRendererRegistrar reg) {
        reg.accept(BombEntitiesSpace.ANTIMATTER_BLAST.get(), NoopRenderer::new);
    }

    static void registerVisualizers() {
        SimpleBlockEntityVisualizer.builder(BombBlocksSpace.NUKE_ANTIMATTER_BE.get())
                .factory(NukeAntimatterVisual::new)
                .apply();
    }
}
