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

package com.hbmspace.client.sky;

import com.hbm.client.model.Meshes;
import com.hbm.render.loader.GroupObject;
import com.hbm.render.loader.HFRWavefrontObject;
import com.mojang.blaze3d.PrimitiveTopology;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.util.function.Consumer;
import net.minecraft.resources.Identifier;

public record SkyMesh(
        String name,
        VertexFormat format,
        PrimitiveTopology topology,
        Consumer<VertexConsumer> vertices) {
    public static final SkyMesh QUAD =
            new SkyMesh(
                    "Space sky quad",
                    DefaultVertexFormat.POSITION_TEX,
                    PrimitiveTopology.QUADS,
                    buffer -> {
                        buffer.addVertex(-1.0F, 0.0F, -1.0F).setUv(0.0F, 0.0F);
                        buffer.addVertex(1.0F, 0.0F, -1.0F).setUv(1.0F, 0.0F);
                        buffer.addVertex(1.0F, 0.0F, 1.0F).setUv(1.0F, 1.0F);
                        buffer.addVertex(-1.0F, 0.0F, 1.0F).setUv(0.0F, 1.0F);
                    });
    public static final SkyMesh QUAD_TRANSPOSED =
            new SkyMesh(
                    "Space sky sprite",
                    DefaultVertexFormat.POSITION_TEX,
                    PrimitiveTopology.QUADS,
                    buffer -> {
                        buffer.addVertex(-1.0F, 0.0F, -1.0F).setUv(0.0F, 0.0F);
                        buffer.addVertex(1.0F, 0.0F, -1.0F).setUv(0.0F, 1.0F);
                        buffer.addVertex(1.0F, 0.0F, 1.0F).setUv(1.0F, 1.0F);
                        buffer.addVertex(-1.0F, 0.0F, 1.0F).setUv(1.0F, 0.0F);
                    });
    public static final SkyMesh QUAD_FLAT =
            new SkyMesh(
                    "Space sky flat quad",
                    DefaultVertexFormat.POSITION,
                    PrimitiveTopology.QUADS,
                    buffer -> {
                        buffer.addVertex(-1.0F, 0.0F, -1.0F);
                        buffer.addVertex(1.0F, 0.0F, -1.0F);
                        buffer.addVertex(1.0F, 0.0F, 1.0F);
                        buffer.addVertex(-1.0F, 0.0F, 1.0F);
                    });

    public static SkyMesh obj(Identifier model) {
        return new SkyMesh(
                model.toString(),
                DefaultVertexFormat.POSITION_TEX,
                PrimitiveTopology.QUADS,
                buffer -> {
                    HFRWavefrontObject mesh = Meshes.load(model).insetUv(0.0005F);
                    for (GroupObject group : mesh.groups) {
                        float[] corners = group.quads(true);
                        for (int i = 0; i < corners.length; i += GroupObject.STRIDE) {
                            buffer.addVertex(corners[i], corners[i + 1], corners[i + 2])
                                    .setUv(corners[i + 3], corners[i + 4]);
                        }
                    }
                });
    }
}
