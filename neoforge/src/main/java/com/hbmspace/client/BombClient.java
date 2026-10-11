// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.client;

import com.hbm.client.ClientRegistry;
import com.hbmspace.blocks.BombBlocksSpace;
import com.hbmspace.client.gui.ScreenNukeAntimatter;
import com.hbmspace.client.render.RenderNukeAntimatter;
import com.hbmspace.client.render.flywheel.NukeAntimatterVisual;
import com.hbmspace.entity.BombEntitiesSpace;
import com.hbmspace.inventory.container.BombMenusSpace;
import dev.engine_room.flywheel.lib.visualization.SimpleBlockEntityVisualizer;
import net.minecraft.client.renderer.entity.NoopRenderer;

final class BombClient {

    private BombClient() {}

    static void registerMenuScreens(ClientRegistry.MenuScreenRegistrar reg) {
        reg.accept(BombMenusSpace.NUKE_ANTIMATTER.get(), ScreenNukeAntimatter::new);
    }

    static void registerBlockEntityRenderers(ClientRegistry.BlockEntityRendererRegistrar reg) {
        reg.accept(BombBlocksSpace.NUKE_ANTIMATTER_BE.get(), context -> new RenderNukeAntimatter());
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
