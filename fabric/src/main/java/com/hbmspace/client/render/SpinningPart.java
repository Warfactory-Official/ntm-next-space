// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.client.render;

import com.hbm.blocks.multiblock.BlockMultiblockCore;
import com.hbm.render.loader.HFRWavefrontObject;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.joml.Matrix4f;

public record SpinningPart<T extends BlockEntity>(
        HFRWavefrontObject mesh,
        Identifier texture,
        String group,
        boolean cull,
        Base base,
        Spin spin,
        Angle<T> angle,
        Extent extent) {

    public interface Base {
        void apply(Direction facing, Matrix4f pose);
    }

    public interface Spin {
        void apply(Matrix4f pose, float degrees);
    }

    public interface Angle<T> {
        float at(T blockEntity, float partialTick);
    }

    public interface Extent {
        AABB at(BlockPos pos);
    }

    public int groupId() {
        return mesh.partId(group);
    }

    public Matrix4f pose(BlockState state, float degrees, Matrix4f out) {
        base.apply(BlockMultiblockCore.coreFacing(state), out);
        spin.apply(out, degrees);
        return out;
    }
}
