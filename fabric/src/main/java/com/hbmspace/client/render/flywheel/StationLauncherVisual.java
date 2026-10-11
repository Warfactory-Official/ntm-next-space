// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.client.render.flywheel;

import com.hbm.blocks.multiblock.BlockMultiblockCore;
import com.hbm.client.render.flywheel.MeshPart;
import com.hbm.util.Facing;
import com.hbmspace.blockentity.machine.BlockEntityOrbitalStationLauncher;
import com.hbmspace.client.render.PosedParts;
import com.hbmspace.client.render.RocketPronter;
import com.hbmspace.client.render.StationRenders;
import com.hbmspace.handler.RocketStruct;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.instance.InstanceTypes;
import dev.engine_room.flywheel.lib.instance.TransformedInstance;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.phys.AABB;
import org.joml.Matrix4f;

public final class StationLauncherVisual
        extends PosedPartsVisual<BlockEntityOrbitalStationLauncher> {

    private final RocketPronter pronter;
    private final float yaw;
    private RocketStruct drawn;
    private AABB visible;
    private boolean stale;

    public StationLauncherVisual(
            VisualizationContext context,
            BlockEntityOrbitalStationLauncher blockEntity,
            float partialTick,
            PosedParts<BlockEntityOrbitalStationLauncher> parts,
            MeshPart[] meshes,
            RocketPronter pronter) {
        super(context, blockEntity, partialTick, parts, meshes);
        this.pronter = pronter;
        this.yaw = Facing.yaw(blockEntity.getBlockState().getValue(BlockMultiblockCore.FACING), 0);
        track();
        rebuild();
    }

    private void track() {
        drawn = blockEntity.rocket.copy();
        visible = super.visibleBounds().minmax(StationRenders.launcherRocketBox(pos, drawn));
    }

    @Override
    protected void trackExtent() {
        if (blockEntity.rocket.equals(drawn)) return;
        track();
        stale = true;
        refreshVisibleBounds();
    }

    @Override
    protected void frame(Context context) {
        if (stale) rebuild();
        super.frame(context);
    }

    private void rebuild() {
        stale = false;
        List<Extra> extras = new ArrayList<>();
        PoseStack pose = new PoseStack();
        StationRenders.launcherRocketOrigin(pose, yaw, drawn);
        pronter.visit(
                drawn,
                null,
                false,
                0,
                0,
                0F,
                pose,
                piece -> {
                    Matrix4f local = new Matrix4f(pose.last().pose());
                    for (MeshPart part : RocketHostVisual.parts(piece, true)) {
                        TransformedInstance instance =
                                instancerProvider()
                                        .instancer(InstanceTypes.TRANSFORMED, part.model())
                                        .createInstance();
                        instance.setTransform(
                                        new Matrix4f()
                                                .translation(
                                                        visualPos.getX(),
                                                        visualPos.getY(),
                                                        visualPos.getZ())
                                                .mul(local))
                                .light(0)
                                .setChanged();
                        extras.add(new Extra(instance, part, local));
                    }
                });
        replaceExtras(extras);
    }

    @Override
    protected AABB visibleBounds() {
        return visible;
    }
}
