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
import com.hbm.client.render.flywheel.LightBounds;
import com.hbm.client.render.flywheel.MeshPart;
import com.hbm.main.ResourceManager;
import com.hbm.render.loader.HFRWavefrontObject;
import com.hbm.util.Facing;
import com.hbm.util.GameTime;
import com.hbmspace.blockentity.machine.HTRNeoBlockEntity;
import com.hbmspace.client.render.StationModels;
import dev.engine_room.flywheel.api.instance.Instance;
import dev.engine_room.flywheel.api.material.Material;
import dev.engine_room.flywheel.api.visual.ShaderLightVisual;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.instance.InstanceTypes;
import dev.engine_room.flywheel.lib.instance.TransformedInstance;
import dev.engine_room.flywheel.lib.instance.UvTransformedInstance;
import dev.engine_room.flywheel.lib.visual.AbstractBoundedBlockEntityVisual;
import dev.engine_room.flywheel.lib.visual.CulledDynamicVisual;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.AABB;
import org.joml.Matrix4f;
import org.jspecify.annotations.Nullable;

public final class HTRNeoVisual extends AbstractBoundedBlockEntityVisual<HTRNeoBlockEntity>
        implements ShaderLightVisual, CulledDynamicVisual {

    private static final HFRWavefrontObject MODEL = StationModels.htrf4_neo;
    private static final MeshPart ROTOR =
            part("Rotor", MeshPart.litCutout(StationModels.htrf4_neo_tex));
    private static final MeshPart ROTOR_2 =
            part("Rotor2", MeshPart.litCutout(StationModels.htrf4_neo_tex));
    private static final MeshPart EXHAUST =
            part("Exhaust", StationMaterials.blended(StationModels.htrf4_exhaust_tex));
    private static final MeshPart SPARKLE =
            part("Exhaust", StationMaterials.additive(ResourceManager.fusion_plasma_sparkle_tex));

    private final TransformedInstance rotor;
    private final TransformedInstance rotor2;
    private final TransformedInstance exhaust;
    private final UvTransformedInstance sparkle;
    private final RandomSource random = RandomSource.create();
    private final Matrix4f base = new Matrix4f();
    private final Matrix4f rotorPose = new Matrix4f();
    private final Matrix4f plumePose = new Matrix4f();
    private final Matrix4f instancePose = new Matrix4f();
    private final AABB lit;
    private final AABB visible;
    private final double[] lightBounds = new double[6];
    private @Nullable AABB lastLightBounds;
    private float lastRotor = Float.NaN;
    private boolean plumeShown;

    public HTRNeoVisual(
            VisualizationContext context, HTRNeoBlockEntity blockEntity, float partialTick) {
        super(context, blockEntity, partialTick);
        BlockPos p = pos;
        lit = new AABB(p);
        visible =
                new AABB(
                        p.getX() - 22,
                        p.getY() - 2,
                        p.getZ() - 22,
                        p.getX() + 23,
                        p.getY() + 3,
                        p.getZ() + 23);
        base.translation(0.5F, -2.0F, 0.5F)
                .rotateY(
                        Facing.yaw(blockState.getValue(MultiblockCoreBlock.FACING), 90)
                                * Mth.DEG_TO_RAD);
        rotor =
                instancerProvider()
                        .instancer(InstanceTypes.TRANSFORMED, ROTOR.model())
                        .createInstance();
        rotor2 =
                instancerProvider()
                        .instancer(InstanceTypes.TRANSFORMED, ROTOR_2.model())
                        .createInstance();
        exhaust =
                instancerProvider()
                        .instancer(InstanceTypes.TRANSFORMED, EXHAUST.model())
                        .createInstance();
        sparkle =
                instancerProvider()
                        .instancer(InstanceTypes.UV_TRANSFORMED, SPARKLE.model())
                        .createInstance();
        exhaust.setVisible(false);
        sparkle.setVisible(false);
        writeFrame(partialTick);
    }

    private static MeshPart part(String group, Material material) {
        return MeshPart.obj(MODEL.groups[MODEL.partId(group)], MODEL.smoothing(), material);
    }

    @Override
    public void frame(Context context) {
        writeFrame(context.partialTick());
    }

    private void writeFrame(float partialTick) {
        float angle =
                blockEntity.prevRotor + (blockEntity.rotor - blockEntity.prevRotor) * partialTick;
        if (angle != lastRotor) {
            lastRotor = angle;
            rotorPose
                    .set(base)
                    .translate(0F, 2.5F, 0F)
                    .rotateZ(angle * Mth.DEG_TO_RAD)
                    .translate(0F, -2.5F, 0F);
            write(rotor, rotorPose);
            write(rotor2, rotorPose);
            LightBounds.resetBounds(lightBounds, lit);
            LightBounds.includeLightBounds(lightBounds, ROTOR.model(), rotorPose, pos);
            LightBounds.includeLightBounds(lightBounds, ROTOR_2.model(), rotorPose, pos);
            lastLightBounds = LightBounds.sections(lightSections, lightBounds, lastLightBounds);
        }

        float trailStretch = random.nextFloat();
        trailStretch = (1.2F - (trailStretch * trailStretch * 0.1F)) * blockEntity.thrustAmount;
        if (trailStretch <= 0) {
            if (plumeShown) {
                exhaust.setVisible(false);
                sparkle.setVisible(false);
            }
            plumeShown = false;
            return;
        }
        if (!plumeShown) {
            exhaust.setVisible(true);
            sparkle.setVisible(true);
        }
        plumeShown = true;

        plumePose
                .set(base)
                .translate(0F, 0F, 12F)
                .scale(1F, 1F, trailStretch)
                .translate(0F, 0F, -12F);
        instancePose
                .translation(visualPos.getX(), visualPos.getY(), visualPos.getZ())
                .mul(plumePose);
        float r = blockEntity.plasmaR;
        float g = blockEntity.plasmaG;
        float b = blockEntity.plasmaB;
        float thrust = blockEntity.thrustAmount;
        exhaust.setTransform(instancePose).colorArgb(color(r, g, b, thrust)).light(0).setChanged();

        float spin = (float) (GameTime.now() / 250D % 1D);
        sparkle.setTransform(instancePose);
        sparkle.colorArgb(color(r * 2, g * 2, b * 2, thrust * 0.75F)).light(0);
        sparkle.uvRegion(0F, spin, 0F, 1F).setChanged();
    }

    private void write(TransformedInstance instance, Matrix4f pose) {
        instancePose.translation(visualPos.getX(), visualPos.getY(), visualPos.getZ()).mul(pose);
        instance.setTransform(instancePose).light(0).setChanged();
    }

    private static int color(float r, float g, float b, float a) {
        return ARGB.colorFromFloat(
                Mth.clamp(a, 0F, 1F),
                Mth.clamp(r, 0F, 1F),
                Mth.clamp(g, 0F, 1F),
                Mth.clamp(b, 0F, 1F));
    }

    @Override
    protected AABB getRenderBoundingBox() {
        return lit;
    }

    @Override
    protected AABB visibleBounds() {
        return visible;
    }

    @Override
    public void collectCrumblingInstances(Consumer<Instance> consumer) {
        consumer.accept(rotor);
        consumer.accept(rotor2);
    }

    @Override
    protected void _delete() {
        rotor.delete();
        rotor2.delete();
        exhaust.delete();
        sparkle.delete();
    }
}
