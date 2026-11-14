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

import com.hbm.client.render.flywheel.LightBounds;
import com.hbm.client.render.flywheel.MeshPart;
import com.hbmspace.client.render.SpinningPart;
import dev.engine_room.flywheel.api.instance.Instance;
import dev.engine_room.flywheel.api.material.CardinalLightingMode;
import dev.engine_room.flywheel.api.material.Material;
import dev.engine_room.flywheel.api.visual.ShaderLightVisual;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.instance.InstanceTypes;
import dev.engine_room.flywheel.lib.instance.TransformedInstance;
import dev.engine_room.flywheel.lib.material.CutoutShaders;
import dev.engine_room.flywheel.lib.material.LightShaders;
import dev.engine_room.flywheel.lib.material.Materials;
import dev.engine_room.flywheel.lib.material.SimpleMaterial;
import dev.engine_room.flywheel.lib.visual.AbstractBoundedBlockEntityVisual;
import dev.engine_room.flywheel.lib.visual.CulledDynamicVisual;
import java.util.function.Consumer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import org.joml.Matrix4f;
import org.jspecify.annotations.Nullable;

public final class SpinningPartVisual<T extends BlockEntity>
        extends AbstractBoundedBlockEntityVisual<T>
        implements ShaderLightVisual, CulledDynamicVisual {

    private final SpinningPart<T> part;
    private final MeshPart mesh;
    private final TransformedInstance instance;
    private final Matrix4f partPose = new Matrix4f();
    private final Matrix4f instancePose = new Matrix4f();
    private final AABB extent;
    private final double[] lightBoundsAccumulator = new double[6];
    private @Nullable AABB lastLightBounds;
    private float lastAngle = Float.NaN;

    public SpinningPartVisual(
            VisualizationContext context,
            T blockEntity,
            float partialTick,
            SpinningPart<T> part,
            MeshPart mesh) {
        super(context, blockEntity, partialTick);
        this.part = part;
        this.mesh = mesh;
        this.extent = part.extent().at(pos);
        instance =
                instancerProvider()
                        .instancer(InstanceTypes.TRANSFORMED, mesh.model())
                        .createInstance();
        writeFrame(partialTick);
    }

    public static <T extends BlockEntity> MeshPart meshOf(SpinningPart<T> part) {
        Material material =
                SimpleMaterial.builderOf(part.cull() ? Materials.CUTOUT : Materials.CUTOUT_NO_CULL)
                        .texture(part.texture())
                        .mipmap(false)
                        .cutout(CutoutShaders.ONE_TENTH)
                        .light(LightShaders.SMOOTH)
                        .ambientOcclusion(false)
                        .cardinalLightingMode(CardinalLightingMode.CHUNK)
                        .build();
        return MeshPart.obj(part.mesh().groups[part.groupId()], part.mesh().smoothing(), material);
    }

    @Override
    public void frame(Context context) {
        writeFrame(context.partialTick());
    }

    private void writeFrame(float partialTick) {
        float angle = part.angle().at(blockEntity, partialTick);
        if (angle == lastAngle) return;
        lastAngle = angle;
        part.pose(blockState, angle, partPose.identity());
        instancePose
                .translation(visualPos.getX(), visualPos.getY(), visualPos.getZ())
                .mul(partPose);
        instance.setTransform(instancePose).light(0).setChanged();
        LightBounds.resetBounds(lightBoundsAccumulator, extent);
        LightBounds.includeLightBounds(lightBoundsAccumulator, mesh.model(), partPose, pos);
        lastLightBounds =
                LightBounds.sections(lightSections, lightBoundsAccumulator, lastLightBounds);
    }

    @Override
    protected AABB getRenderBoundingBox() {
        return extent;
    }

    @Override
    public void collectCrumblingInstances(Consumer<Instance> consumer) {
        consumer.accept(instance);
    }

    @Override
    protected void _delete() {
        instance.delete();
    }
}
