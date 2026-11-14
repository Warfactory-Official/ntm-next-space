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

import com.hbm.blocks.machine.HorizontalMachineBlock;
import com.hbm.client.render.NukeFstbmbRenderer;
import com.hbm.client.render.flywheel.BeamVisual;
import com.hbm.client.render.flywheel.MeshPart;
import com.hbm.main.ResourceManager;
import com.hbm.render.util.BeamPronter.EnumWaveType;
import com.hbm.util.GameTime;
import com.hbmspace.blockentity.bomb.NukeAntimatterBlockEntity;
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
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

public final class NukeAntimatterVisual
        extends AbstractBoundedBlockEntityVisual<NukeAntimatterBlockEntity>
        implements ShaderLightVisual, CulledDynamicVisual {

    private static final Vec3 BEAM = new Vec3(0D, 2.5D, 0D);
    private static final float SCALE = 0.1F;
    private static final int CORE_COLOR = 0xFF010101;
    private static final int SPIRAL_COLOR = 0x080810;
    private static final int RANDOM_COLOR = 0x101030;
    private static final Material SPHERE_MATERIAL =
            SimpleMaterial.builderOf(Materials.CUTOUT)
                    .texture(ResourceManager.white_tex)
                    .mipmap(false)
                    .cutout(CutoutShaders.ONE_TENTH)
                    .light(LightShaders.SMOOTH)
                    .useOverlay(false)
                    .ambientOcclusion(false)
                    .cardinalLightingMode(CardinalLightingMode.OFF)
                    .build();
    private static final MeshPart[] SPHERE_PARTS =
            MeshPart.objParts(ResourceManager.sphere_uv, SPHERE_MATERIAL);

    private final TransformedInstance[] spheres;
    private final BeamVisual spiral;
    private final BeamVisual[] randoms = new BeamVisual[2];
    private final Matrix4f basePose = new Matrix4f();
    private final Matrix4f spherePose = new Matrix4f();
    private final Matrix4f beamPose = new Matrix4f();
    private final Matrix4f instancePose = new Matrix4f();
    private boolean shown = true;

    public NukeAntimatterVisual(
            VisualizationContext context,
            NukeAntimatterBlockEntity blockEntity,
            float partialTick) {
        super(context, blockEntity, partialTick);
        float yaw = NukeFstbmbRenderer.yawFor(blockState.getValue(HorizontalMachineBlock.FACING));
        basePose.translation(0.5F, 0F, 0.5F).rotateY(yaw * Mth.DEG_TO_RAD);
        beamPose.set(basePose).translate(0F, 1F, 0F);

        spheres = new TransformedInstance[SPHERE_PARTS.length];
        for (int i = 0; i < SPHERE_PARTS.length; i++) {
            spheres[i] =
                    instancerProvider()
                            .instancer(InstanceTypes.TRANSFORMED, SPHERE_PARTS[i].model())
                            .createInstance();
        }
        spiral = new BeamVisual(context, level, pos, false, 6, 1F);
        for (int i = 0; i < randoms.length; i++)
            randoms[i] = new BeamVisual(context, level, pos, false, 2, 1F);
        writeFrame(partialTick);
    }

    @Override
    public void frame(Context context) {
        writeFrame(context.partialTick());
    }

    private void writeFrame(float partialTick) {
        if (!blockEntity.core) {
            if (shown) {
                for (TransformedInstance sphere : spheres) sphere.setVisible(false);
                spiral.hide();
                for (BeamVisual beam : randoms) beam.hide();
                shown = false;
            }
            return;
        }

        double ticks = GameTime.millis(blockEntity.getLevel()) / 50D + partialTick;
        float bob = (float) Math.sin(ticks * 0.1D % (Math.PI * 2D));
        spherePose.set(basePose).translate(0F, 2.4F + bob * 0.125F * SCALE, 0F).scale(SCALE);
        instancePose
                .translation(visualPos.getX(), visualPos.getY(), visualPos.getZ())
                .mul(spherePose);
        for (TransformedInstance sphere : spheres) {
            if (!shown) sphere.setVisible(true);
            sphere.setTransform(instancePose).colorArgb(CORE_COLOR).light(0).setChanged();
        }
        shown = true;

        int tick = (int) ticks;
        spiral.update(
                beamPose,
                BEAM,
                EnumWaveType.SPIRAL,
                0,
                1,
                0F,
                SCALE * 0.1F,
                SPIRAL_COLOR,
                SPIRAL_COLOR);
        randoms[0].update(
                beamPose,
                BEAM,
                EnumWaveType.RANDOM,
                tick / 2 % 1000,
                6,
                SCALE,
                0.0625F * SCALE,
                RANDOM_COLOR,
                RANDOM_COLOR);
        randoms[1].update(
                beamPose,
                BEAM,
                EnumWaveType.RANDOM,
                tick / 4 % 1000,
                6,
                SCALE,
                0.0625F * SCALE,
                RANDOM_COLOR,
                RANDOM_COLOR);
    }

    @Override
    protected AABB getRenderBoundingBox() {
        return new AABB(
                pos.getX() - 1,
                pos.getY(),
                pos.getZ() - 1,
                pos.getX() + 2,
                pos.getY() + 4,
                pos.getZ() + 2);
    }

    @Override
    public void collectCrumblingInstances(Consumer<Instance> consumer) {
        for (TransformedInstance sphere : spheres) consumer.accept(sphere);
    }

    @Override
    protected void _delete() {
        for (TransformedInstance sphere : spheres) sphere.delete();
        spiral.delete();
        for (BeamVisual beam : randoms) beam.delete();
    }
}
