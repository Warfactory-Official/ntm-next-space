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

package com.hbmspace.client.gui;

import com.hbmspace.client.sky.PlanetInfo;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import java.util.List;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.renderer.state.gui.pip.PictureInPictureRenderState;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

public record StardarMapRenderState(List<Draw> draws, int x0, int y0, int x1, int y1)
        implements PictureInPictureRenderState {

    public sealed interface Draw {}

    public record Textured(
            RenderPipeline pipeline,
            @Nullable Identifier texture,
            @Nullable Identifier texture1,
            @Nullable Identifier texture2,
            @Nullable PlanetInfo planet,
            float r,
            float g,
            float b,
            float a,
            float x0,
            float y0,
            float x1,
            float y1,
            float u0,
            float v0,
            float u1,
            float v1,
            float angle,
            float pivotX,
            float pivotY)
            implements Draw {}

    public record Colored(float[] vertices) implements Draw {}

    @Override
    public float scale() {
        return 1;
    }

    @Override
    public @Nullable ScreenRectangle scissorArea() {
        return null;
    }

    @Override
    public ScreenRectangle bounds() {
        return new ScreenRectangle(x0, y0, x1 - x0, y1 - y0);
    }
}
