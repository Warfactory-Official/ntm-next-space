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

import com.hbm.client.render.flywheel.BeamVisual;
import com.hbm.render.util.BeamPronter.EnumWaveType;
import com.hbmspace.blockentity.deco.LightningVolcanoBlockEntity;
import dev.engine_room.flywheel.api.instance.Instance;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.visual.AbstractBoundedBlockEntityVisual;
import dev.engine_room.flywheel.lib.visual.CulledDynamicVisual;
import java.util.function.Consumer;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

public final class VolcanoVisual
        extends AbstractBoundedBlockEntityVisual<LightningVolcanoBlockEntity>
        implements CulledDynamicVisual {

    private final BeamVisual core;
    private final BeamVisual arcSlow;
    private final BeamVisual arcFast;
    private final Matrix4f base =
            new Matrix4f()
                    .translation(0.5F, 0.0F, 0.5F)
                    .rotateY(90.0F * Mth.DEG_TO_RAD)
                    .translate(0.0F, 0.5F, -0.5F)
                    .rotateX(90.0F * Mth.DEG_TO_RAD)
                    .translate(0.0F, 0.5F, 0.5F);
    private final AABB extent;

    public VolcanoVisual(
            VisualizationContext context,
            LightningVolcanoBlockEntity blockEntity,
            float partialTick) {
        super(context, blockEntity, partialTick);
        core = new BeamVisual(context, level, pos, false, 6, 1F);
        arcSlow = new BeamVisual(context, level, pos, false, 2, 1F);
        arcFast = new BeamVisual(context, level, pos, false, 2, 1F);
        extent = new AABB(pos).expandTowards(0, 202, 0).inflate(4, 0, 4);
        writeFrame();
    }

    @Override
    public void frame(Context context) {
        writeFrame();
    }

    private void writeFrame() {
        if (blockEntity.chargetime != 0) {
            core.hide();
            arcSlow.hide();
            arcFast.hide();
            return;
        }
        float scale = blockEntity.flashd;
        float alpha = 1.0F - Math.min(1.0F, scale / 110);
        Vec3 skeleton = new Vec3(0, 0, -100 - scale);
        long time = level.getGameTime();
        core.update(
                base,
                skeleton,
                EnumWaveType.SPIRAL,
                0,
                8,
                0F,
                3 * alpha,
                0x101020,
                0x101020,
                alpha * 2F);
        arcSlow.update(
                base,
                skeleton,
                EnumWaveType.RANDOM,
                (int) (time / 2) % 1000,
                26,
                3F,
                0.0625F * 2,
                0x202060,
                0x202060,
                0.5F * alpha);
        arcFast.update(
                base,
                skeleton,
                EnumWaveType.RANDOM,
                (int) (time / 4) % 1000,
                26,
                3F,
                0.0625F * 2,
                0x202060,
                0x202060,
                0.5F * alpha);
    }

    @Override
    protected AABB getRenderBoundingBox() {
        return extent;
    }

    @Override
    public void collectCrumblingInstances(Consumer<Instance> consumer) {}

    @Override
    protected void _delete() {
        core.delete();
        arcSlow.delete();
        arcFast.delete();
    }
}
