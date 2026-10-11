// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.client.render;

import com.hbmspace.blocks.TerrainBlockEntitiesSpace;
import com.hbmspace.client.render.flywheel.VolcanoVisual;
import dev.engine_room.flywheel.lib.visualization.SimpleBlockEntityVisualizer;

public final class TerrainRenders {

    private TerrainRenders() {}

    public static void registerVisualizers() {
        SimpleBlockEntityVisualizer.builder(TerrainBlockEntitiesSpace.LIGHTNING_VOLCANO.get())
                .factory(VolcanoVisual::new)
                .apply();
    }
}
