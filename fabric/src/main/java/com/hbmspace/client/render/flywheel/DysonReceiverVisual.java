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
import com.hbm.client.render.flywheel.MeshPart;
import com.hbm.render.util.BeamPronter.EnumWaveType;
import com.hbmspace.blockentity.machine.DysonReceiverBlockEntity;
import com.hbmspace.client.render.DysonReceiverRenderer;
import com.hbmspace.client.render.PosedParts;
import com.hbmspace.client.render.SkyRenders;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import org.joml.Matrix4f;

public final class DysonReceiverVisual extends PosedPartsVisual<DysonReceiverBlockEntity> {
    private static final int COLOR = 0x804400;

    private final BeamVisual spiral;
    private final BeamVisual random;
    private final Matrix4f base = new Matrix4f();

    public DysonReceiverVisual(
            VisualizationContext context,
            DysonReceiverBlockEntity blockEntity,
            float partialTick,
            PosedParts<DysonReceiverBlockEntity> parts,
            MeshPart[] meshes) {
        super(context, blockEntity, partialTick, parts, meshes);
        SkyRenders.base(blockEntity, base).translate(0F, DysonReceiverRenderer.AXIS, 0F);
        spiral = new BeamVisual(context, level, pos, false, DysonReceiverRenderer.LAYERS, 1F);
        random = new BeamVisual(context, level, pos, false, DysonReceiverRenderer.LAYERS, 1F);
        beams();
    }

    @Override
    public void frame(Context context) {
        super.frame(context);
        beams();
    }

    private void beams() {
        if (!blockEntity.isReceiving || blockEntity.beamLength <= 0) {
            spiral.hide();
            random.hide();
            return;
        }
        int length = blockEntity.beamLength;
        spiral.update(
                base,
                0,
                0,
                length,
                EnumWaveType.SPIRAL,
                0,
                1,
                DysonReceiverRenderer.SPIRAL_SIZE,
                DysonReceiverRenderer.THICKNESS,
                COLOR,
                COLOR);
        random.update(
                base,
                0,
                0,
                length,
                EnumWaveType.RANDOM,
                (int) (level.getGameTime() % 1000),
                length / 2,
                DysonReceiverRenderer.RANDOM_SIZE,
                DysonReceiverRenderer.THICKNESS,
                COLOR,
                COLOR);
    }

    @Override
    protected void _delete() {
        super._delete();
        spiral.delete();
        random.delete();
    }
}
