// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.client.render;

import com.hbm.render.loader.HFRWavefrontObject;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import org.joml.Matrix4f;

public record PosedParts<T extends BlockEntity>(List<Part<T>> parts, Extent lit, Extent drawn) {
    public interface Pose<T> {
        boolean at(T blockEntity, float partialTick, Matrix4f out);
    }

    public interface Extent {
        AABB at(BlockPos pos);
    }

    public record Part<T>(
            HFRWavefrontObject mesh, String group, Identifier texture, boolean cull, Pose<T> pose) {

        public int groupId() {
            return mesh.partId(group);
        }
    }
}
