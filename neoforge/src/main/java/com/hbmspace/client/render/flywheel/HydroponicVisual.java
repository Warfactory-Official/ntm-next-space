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

import com.hbm.blocks.multiblock.MultiblockCoreBlock;
import com.hbm.client.render.flywheel.EffectVisuals;
import com.hbm.client.render.flywheel.MeshPart;
import com.hbm.render.loader.HFRWavefrontObject;
import com.hbmspace.blockentity.machine.HydroponicBlockEntity;
import com.hbmspace.client.render.HydroponicRenderer;
import dev.engine_room.flywheel.api.instance.Instance;
import dev.engine_room.flywheel.api.material.CardinalLightingMode;
import dev.engine_room.flywheel.api.material.Material;
import dev.engine_room.flywheel.api.material.Transparency;
import dev.engine_room.flywheel.api.material.WriteMask;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.instance.InstanceTypes;
import dev.engine_room.flywheel.lib.instance.TransformedInstance;
import dev.engine_room.flywheel.lib.material.CutoutShaders;
import dev.engine_room.flywheel.lib.material.SimpleMaterial;
import dev.engine_room.flywheel.lib.visual.AbstractBoundedBlockEntityVisual;
import dev.engine_room.flywheel.lib.visual.CulledDynamicVisual;
import java.util.function.Consumer;
import net.minecraft.resources.Identifier;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import org.joml.Matrix4f;

public final class HydroponicVisual extends AbstractBoundedBlockEntityVisual<HydroponicBlockEntity>
        implements CulledDynamicVisual {

    private final TransformedInstance lights;
    private final AABB extent;
    private boolean lit;

    public HydroponicVisual(
            VisualizationContext context,
            HydroponicBlockEntity blockEntity,
            float partialTick,
            MeshPart lamps) {
        super(context, blockEntity, partialTick);
        extent = HydroponicRenderer.extent(pos);
        Matrix4f pose =
                new Matrix4f()
                        .translation(
                                visualPos.getX() + 0.5F, visualPos.getY(), visualPos.getZ() + 0.5F)
                        .rotateY(
                                HydroponicRenderer.yaw(MultiblockCoreBlock.coreFacing(blockState))
                                        * Mth.DEG_TO_RAD);
        lights =
                instancerProvider()
                        .instancer(InstanceTypes.TRANSFORMED, lamps.model())
                        .createInstance();
        lights.setTransform(pose).light(LightCoordsUtil.FULL_BRIGHT).setChanged();
        lit = HydroponicRenderer.lit(blockEntity);
        lights.setVisible(lit);
    }

    public static MeshPart meshOf(HFRWavefrontObject mesh, Identifier texture) {
        Material material =
                SimpleMaterial.builder()
                        .texture(texture)
                        .mipmap(false)
                        .cutout(CutoutShaders.ONE_TENTH)
                        .useLight(false)
                        .useOverlay(false)
                        .ambientOcclusion(false)
                        .cardinalLightingMode(CardinalLightingMode.CHUNK)
                        .transparency(Transparency.ORDER_INDEPENDENT_ADDITIVE)
                        .writeMask(WriteMask.COLOR)
                        .backfaceCulling(false)
                        .fog(EffectVisuals.FADE)
                        .build();
        return MeshPart.obj(mesh.groups[mesh.partId("Lights")], mesh.smoothing(), material);
    }

    @Override
    public void frame(Context context) {
        boolean now = HydroponicRenderer.lit(blockEntity);
        if (now == lit) return;
        lit = now;
        lights.setVisible(now);
    }

    @Override
    protected AABB getRenderBoundingBox() {
        return extent;
    }

    @Override
    public void collectCrumblingInstances(Consumer<Instance> consumer) {}

    @Override
    protected void _delete() {
        lights.delete();
    }
}
