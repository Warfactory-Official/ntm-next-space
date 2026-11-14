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

package com.hbmspace.client.fog;

import com.seibel.distanthorizons.api.DhApi;
import com.seibel.distanthorizons.api.enums.rendering.EDhApiFogFalloff;
import com.seibel.distanthorizons.api.enums.rendering.EDhApiHeightFogMixMode;
import com.seibel.distanthorizons.api.methods.events.DhApiEventRegister;
import com.seibel.distanthorizons.api.methods.events.abstractEvents.DhApiBeforeFogRenderEvent;
import com.seibel.distanthorizons.api.methods.events.sharedParameterObjects.DhApiCancelableEventParam;
import java.awt.Color;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.fog.FogRenderer;

public final class DistantHorizonsFog extends DhApiBeforeFogRenderEvent {
    public static void install() {
        var result =
                DhApiEventRegister.on(DhApiBeforeFogRenderEvent.class, new DistantHorizonsFog());
        if (!result.success) throw new IllegalStateException(result.message);
    }

    @Override
    public void beforeRender(DhApiCancelableEventParam<EventParam> event) {
        if (!FogRenderer.fogEnabled) return;
        var mc = Minecraft.getInstance();
        var frame = mc.gameRenderer.gameRenderState().levelRenderState.cameraRenderState.fogData;
        float density = ((com.hbmspace.client.fog.TerrainFogPayload) (frame)).hbmspace$density();
        if (density <= 0F
                || (((com.hbmspace.client.fog.TerrainFogPayload) (frame)).hbmspace$flags()
                                & TerrainExpFog.DARKNESS)
                        != 0) return;

        var fog = event.value.getFogRenderParam();
        fog.setFogColor(new Color(frame.color.x, frame.color.y, frame.color.z));
        fog.setHeightFogMixingMode(EDhApiHeightFogMixMode.SPHERICAL);
        fog.setFarFogFalloff(EDhApiFogFalloff.EXPONENTIAL);
        fog.setFarFogStartPercent(0F);
        fog.setFarFogEndPercent(1F);
        fog.setFarFogMinThickness(0F);
        fog.setFarFogMaxThickness(1F);
        fog.setFarFogDensity(
                density * DhApi.Delayed.configs.graphics().chunkRenderDistance().getValue() * 16F);
    }
}
