// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;

public interface RocketHost<T extends BlockEntity, S> {

    S snapshot(T blockEntity);

    void draw(S snapshot, RocketPronter pronter, PoseStack pose, PieceSink sink);

    AABB bounds(BlockPos pos, S snapshot);

    @FunctionalInterface
    interface PieceSink {
        void accept(RocketPronter.Piece piece, boolean cull);
    }
}
