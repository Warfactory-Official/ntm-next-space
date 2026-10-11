// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.client.render.flywheel;

import com.hbm.client.render.flywheel.BeamVisual;
import com.hbm.client.render.flywheel.MeshPart;
import com.hbm.render.util.BeamPronter.EnumWaveType;
import com.hbmspace.blockentity.machine.BlockEntityDysonReceiver;
import com.hbmspace.client.render.PosedParts;
import com.hbmspace.client.render.RenderDysonReceiver;
import com.hbmspace.client.render.SkyRenders;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import org.joml.Matrix4f;

public final class DysonReceiverVisual extends PosedPartsVisual<BlockEntityDysonReceiver> {
    private static final int COLOR = 0x804400;

    private final BeamVisual spiral;
    private final BeamVisual random;
    private final Matrix4f base = new Matrix4f();

    public DysonReceiverVisual(
            VisualizationContext context,
            BlockEntityDysonReceiver blockEntity,
            float partialTick,
            PosedParts<BlockEntityDysonReceiver> parts,
            MeshPart[] meshes) {
        super(context, blockEntity, partialTick, parts, meshes);
        SkyRenders.base(blockEntity, base).translate(0F, RenderDysonReceiver.AXIS, 0F);
        spiral = new BeamVisual(context, level, pos, false, RenderDysonReceiver.LAYERS, 1F);
        random = new BeamVisual(context, level, pos, false, RenderDysonReceiver.LAYERS, 1F);
        beams();
    }

    @Override
    protected void frame(Context context) {
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
                RenderDysonReceiver.SPIRAL_SIZE,
                RenderDysonReceiver.THICKNESS,
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
                RenderDysonReceiver.RANDOM_SIZE,
                RenderDysonReceiver.THICKNESS,
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
