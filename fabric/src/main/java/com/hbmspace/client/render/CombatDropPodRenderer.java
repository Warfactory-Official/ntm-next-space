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

package com.hbmspace.client.render;

import com.hbm.client.model.Meshes;
import com.hbm.render.loader.HFRWavefrontObject;
import com.hbmspace.HbmSpace;
import com.hbmspace.entity.missile.CombatDropPodEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.resources.Identifier;

public class CombatDropPodRenderer
        extends EntityRenderer<CombatDropPodEntity, CombatDropPodRenderer.State> {

    static final Identifier MESH =
            Identifier.fromNamespaceAndPath(HbmSpace.MOD_ID, "models/combat_drop_pod.obj");
    private static final Identifier[] SKINS = {skin("white"), skin("red"), skin("yellow")};

    private final HFRWavefrontObject mesh = Meshes.load(MESH);

    public CombatDropPodRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0F;
    }

    @Override
    protected boolean affectedByCulling(CombatDropPodEntity entity) {
        return false;
    }

    static Identifier skin(String color) {
        return Identifier.fromNamespaceAndPath(
                HbmSpace.MOD_ID, "textures/models/bombs/combat_drop_pod." + color + ".png");
    }

    static int skinIndex(int color) {
        return color == 1 || color == 2 ? color : 0;
    }

    static Identifier skinTexture(int skinIndex) {
        return SKINS[skinIndex];
    }

    static int skinCount() {
        return SKINS.length;
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(CombatDropPodEntity entity, State state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.color = entity.getColor();
    }

    @Override
    public void submit(
            State state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
        int light = state.lightCoords;
        collector.submitCustomGeometry(
                pose,
                RenderTypes.entityCutoutCull(SKINS[skinIndex(state.color)]),
                (p, buffer) -> mesh.render(p, buffer, light, -1));
        super.submit(state, pose, collector, camera);
    }

    public static final class State extends EntityRenderState {
        int color;
    }
}
