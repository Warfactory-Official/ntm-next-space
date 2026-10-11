// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.client.render.flywheel;

import dev.engine_room.flywheel.api.material.CardinalLightingMode;
import dev.engine_room.flywheel.api.material.Material;
import dev.engine_room.flywheel.api.material.Transparency;
import dev.engine_room.flywheel.api.material.WriteMask;
import dev.engine_room.flywheel.lib.material.SimpleMaterial;
import net.minecraft.resources.Identifier;

final class StationMaterials {

    private StationMaterials() {}

    static Material blended(Identifier texture) {
        return plume(texture, Transparency.ORDER_INDEPENDENT);
    }

    static Material additive(Identifier texture) {
        return plume(texture, Transparency.ORDER_INDEPENDENT_ADDITIVE);
    }

    private static Material plume(Identifier texture, Transparency transparency) {
        return SimpleMaterial.builder()
                .texture(texture)
                .mipmap(false)
                .useLight(false)
                .useOverlay(false)
                .cardinalLightingMode(CardinalLightingMode.OFF)
                .transparency(transparency)
                .writeMask(WriteMask.COLOR)
                .backfaceCulling(false)
                .build();
    }
}
