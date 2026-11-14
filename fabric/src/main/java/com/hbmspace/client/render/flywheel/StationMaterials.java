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
