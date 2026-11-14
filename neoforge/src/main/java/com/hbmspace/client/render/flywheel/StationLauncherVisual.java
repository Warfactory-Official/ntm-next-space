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
import com.hbm.client.render.flywheel.MeshPart;
import com.hbm.util.Facing;
import com.hbmspace.blockentity.machine.OrbitalStationLauncherBlockEntity;
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
        extends PosedPartsVisual<OrbitalStationLauncherBlockEntity> {

    private final RocketPronter pronter;
    private final float yaw;
    private RocketStruct drawn;
    private AABB visible;
    private boolean stale;

    public StationLauncherVisual(
            VisualizationContext context,
            OrbitalStationLauncherBlockEntity blockEntity,
            float partialTick,
            PosedParts<OrbitalStationLauncherBlockEntity> parts,
            MeshPart[] meshes,
            RocketPronter pronter) {
        super(context, blockEntity, partialTick, parts, meshes);
        this.pronter = pronter;
        this.yaw = Facing.yaw(blockEntity.getBlockState().getValue(MultiblockCoreBlock.FACING), 0);
        track();
        rebuild();
    }

    private void track() {
        drawn = blockEntity.rocket.copy();
        visible = super.visibleBounds().minmax(StationRenders.launcherRocketBox(pos, drawn));
    }

    @Override
    public void beforeFrame() {
        if (blockEntity.rocket.equals(drawn)) return;
        track();
        stale = true;
        refreshVisibleBounds();
    }

    @Override
    public void frame(Context context) {
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
