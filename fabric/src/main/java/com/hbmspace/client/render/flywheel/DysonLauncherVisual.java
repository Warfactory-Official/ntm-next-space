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
import com.hbmspace.blockentity.machine.DysonLauncherBlockEntity;
import com.hbmspace.client.render.PosedParts;
import dev.engine_room.flywheel.api.instance.Instance;
import dev.engine_room.flywheel.api.visual.ShaderLightVisual;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.instance.InstanceTypes;
import dev.engine_room.flywheel.lib.instance.TransformedInstance;
import dev.engine_room.flywheel.lib.visual.AbstractDynamicExtentBlockEntityVisual;
import dev.engine_room.flywheel.lib.visual.CulledDynamicVisual;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.core.SectionPos;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import org.joml.Matrix4f;

public final class DysonLauncherVisual
        extends AbstractDynamicExtentBlockEntityVisual<DysonLauncherBlockEntity>
        implements ShaderLightVisual, CulledDynamicVisual {

    private final PosedParts<DysonLauncherBlockEntity> parts;
    private final MeshPart[] meshes;
    private final TransformedInstance[] instances;
    private final Matrix4f[] poses;
    private final Matrix4f[] written;
    private final boolean[] drawn;
    private final Matrix4f instancePose = new Matrix4f();
    private final Matrix4f extentPose = new Matrix4f();
    private final AABB lit;
    private final AABB machineBounds;
    private AABB visible;
    private int extentTick = Integer.MIN_VALUE;
    private boolean extentShot;
    private boolean extentRetained;
    private final double[] near = new double[6];
    private final int[] published = new int[12];
    private final int[] next = new int[12];
    private boolean publishedShot;
    private boolean initialized;
    private boolean publishedAny;

    public DysonLauncherVisual(
            VisualizationContext context,
            DysonLauncherBlockEntity blockEntity,
            float partialTick,
            PosedParts<DysonLauncherBlockEntity> parts,
            MeshPart[] meshes) {
        super(context, blockEntity, partialTick);
        this.parts = parts;
        this.meshes = meshes;
        this.lit = parts.lit().at(pos);
        this.machineBounds = parts.drawn().at(pos);
        this.visible = machineBounds;
        int count = meshes.length;
        instances = new TransformedInstance[count];
        poses = new Matrix4f[count];
        written = new Matrix4f[count];
        drawn = new boolean[count];
        for (int i = 0; i < count; i++) {
            instances[i] =
                    instancerProvider()
                            .instancer(InstanceTypes.TRANSFORMED, meshes[i].model())
                            .createInstance();
            poses[i] = new Matrix4f();
            written[i] = new Matrix4f();
        }
        writeFrame(partialTick);
    }

    @Override
    public void frame(Context context) {
        writeFrame(context.partialTick());
    }

    @Override
    public void beforeFrame() {
        int shot = instances.length - 1;
        int tick = blockEntity.payloadTicks;
        boolean flying = blockEntity.isSpinningDown;
        boolean retained = drawn[shot];
        if (tick == extentTick && flying == extentShot && retained == extentRetained) return;
        extentTick = tick;
        extentShot = flying;
        extentRetained = retained;
        AABB bounds = machineBounds;
        if (flying) {
            var part = parts.parts().get(shot);
            part.pose().at(blockEntity, 0F, extentPose.identity());
            bounds = bounds.minmax(LightBounds.lightBounds(meshes[shot].model(), extentPose, pos));
            part.pose().at(blockEntity, 1F, extentPose.identity());
            bounds = bounds.minmax(LightBounds.lightBounds(meshes[shot].model(), extentPose, pos));
        }
        if (retained)
            bounds =
                    bounds.minmax(
                            LightBounds.lightBounds(meshes[shot].model(), written[shot], pos));
        visible = bounds;
        refreshVisibleBounds();
    }

    private void writeFrame(float partialTick) {
        List<PosedParts.Part<DysonLauncherBlockEntity>> list = parts.parts();
        boolean moved = false;
        for (int i = 0; i < instances.length; i++) {
            boolean draws = list.get(i).pose().at(blockEntity, partialTick, poses[i].identity());
            boolean revealed = draws && (!initialized || !drawn[i]);
            if (draws != drawn[i] || !initialized) {
                instances[i].setVisible(draws);
                moved |= draws != drawn[i];
            }
            drawn[i] = draws;
            if (!draws) continue;
            if (!revealed && poses[i].equals(written[i])) continue;
            written[i].set(poses[i]);
            instancePose
                    .translation(visualPos.getX(), visualPos.getY(), visualPos.getZ())
                    .mul(poses[i]);
            instances[i].setTransform(instancePose).light(0).setChanged();
            moved = true;
        }
        initialized = true;
        if (moved) relight();
    }

    private void relight() {
        if (lightSections == null) return;
        int shot = instances.length - 1;
        LightBounds.resetBounds(near, lit);
        for (int i = 0; i < shot; i++) {
            if (drawn[i]) LightBounds.includeLightBounds(near, meshes[i].model(), poses[i], pos);
        }
        sectionBox(near[0], near[1], near[2], near[3], near[4], near[5], next, 0);
        if (drawn[shot]) {
            AABB box = LightBounds.lightBounds(meshes[shot].model(), poses[shot], pos);
            sectionBox(box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ, next, 6);
        }
        if (publishedAny
                && publishedShot == drawn[shot]
                && Arrays.equals(next, 0, drawn[shot] ? 12 : 6, published, 0, drawn[shot] ? 12 : 6))
            return;
        System.arraycopy(next, 0, published, 0, 12);
        publishedShot = drawn[shot];
        publishedAny = true;
        LongOpenHashSet sections = new LongOpenHashSet();
        addSections(sections, next, 0);
        if (drawn[shot]) addSections(sections, next, 6);
        lightSections.sections(sections);
    }

    private static void sectionBox(
            double x0, double y0, double z0, double x1, double y1, double z1, int[] out, int at) {
        out[at] = SectionPos.blockToSectionCoord(Mth.floor(x0));
        out[at + 1] = SectionPos.blockToSectionCoord(Mth.floor(y0));
        out[at + 2] = SectionPos.blockToSectionCoord(Mth.floor(z0));
        out[at + 3] = SectionPos.blockToSectionCoord(Mth.floor(x1));
        out[at + 4] = SectionPos.blockToSectionCoord(Mth.floor(y1));
        out[at + 5] = SectionPos.blockToSectionCoord(Mth.floor(z1));
    }

    private static void addSections(LongOpenHashSet sections, int[] box, int at) {
        for (int x = box[at]; x <= box[at + 3]; x++) {
            for (int y = box[at + 1]; y <= box[at + 4]; y++) {
                for (int z = box[at + 2]; z <= box[at + 5]; z++)
                    sections.add(SectionPos.asLong(x, y, z));
            }
        }
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
        for (int i = 0; i < instances.length; i++) {
            if (drawn[i]) consumer.accept(instances[i]);
        }
    }

    @Override
    protected void _delete() {
        for (TransformedInstance instance : instances) instance.delete();
    }
}
