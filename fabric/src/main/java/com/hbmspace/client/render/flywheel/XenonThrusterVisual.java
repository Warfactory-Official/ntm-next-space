// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.client.render.flywheel;

import com.hbm.blocks.multiblock.BlockMultiblockCore;
import com.hbm.client.render.flywheel.HbmDynamicBlockEntityVisual;
import com.hbm.client.render.flywheel.MeshPart;
import com.hbm.util.Facing;
import com.hbmspace.blockentity.machine.BlockEntityXenonThruster;
import com.hbmspace.client.render.StationModels;
import dev.engine_room.flywheel.api.instance.Instance;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.instance.InstanceTypes;
import dev.engine_room.flywheel.lib.instance.TransformedInstance;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.AABB;
import org.joml.Matrix4f;

public final class XenonThrusterVisual
        extends HbmDynamicBlockEntityVisual<BlockEntityXenonThruster> {

    private static final MeshPart EXHAUST =
            MeshPart.obj(
                    StationModels.xenon_thruster
                            .groups[StationModels.xenon_thruster.partId("Exhaust")],
                    StationModels.xenon_thruster.smoothing(),
                    StationMaterials.blended(StationModels.xenon_exhaust_tex));

    private final TransformedInstance exhaust;
    private final RandomSource random = RandomSource.create();
    private final Matrix4f instancePose = new Matrix4f();
    private final AABB visible;
    private boolean shown;

    public XenonThrusterVisual(
            VisualizationContext context, BlockEntityXenonThruster blockEntity, float partialTick) {
        super(context, blockEntity, partialTick);
        BlockPos p = pos;
        visible =
                new AABB(
                        p.getX() - 4,
                        p.getY() - 1,
                        p.getZ() - 4,
                        p.getX() + 5,
                        p.getY() + 2,
                        p.getZ() + 5);
        exhaust =
                instancerProvider()
                        .instancer(InstanceTypes.TRANSFORMED, EXHAUST.model())
                        .createInstance();
        exhaust.setVisible(false);
        writeFrame();
    }

    @Override
    protected void frame(Context context) {
        writeFrame();
    }

    private void writeFrame() {
        float trailStretch = random.nextFloat();
        trailStretch = (1.2F - (trailStretch * trailStretch * 0.2F)) * blockEntity.thrustAmount;
        if (trailStretch <= 0) {
            if (shown) exhaust.setVisible(false);
            shown = false;
            return;
        }
        if (!shown) exhaust.setVisible(true);
        shown = true;

        instancePose
                .translation(visualPos.getX(), visualPos.getY(), visualPos.getZ())
                .translate(0.5F, -1.0F, 0.5F)
                .rotateY(
                        Facing.yaw(blockState.getValue(BlockMultiblockCore.FACING), 0)
                                * Mth.DEG_TO_RAD)
                .translate(0F, 0F, -1.5F)
                .scale(1F, 1F, trailStretch)
                .translate(0F, 0F, 1F);
        exhaust.setTransform(instancePose)
                .colorArgb(
                        ARGB.colorFromFloat(
                                Mth.clamp(blockEntity.thrustAmount, 0F, 1F), 1F, 1F, 1F))
                .light(0)
                .setChanged();
    }

    @Override
    protected AABB getRenderBoundingBox() {
        return new AABB(pos);
    }

    @Override
    protected AABB visibleBounds() {
        return visible;
    }

    @Override
    public void collectCrumblingInstances(Consumer<Instance> consumer) {}

    @Override
    protected void _delete() {
        exhaust.delete();
    }
}
