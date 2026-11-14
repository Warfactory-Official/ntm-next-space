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

package com.hbmspace.client.render;

import com.hbm.blocks.multiblock.MultiblockCoreBlock;
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
        base.apply(MultiblockCoreBlock.coreFacing(state), out);
        spin.apply(out, degrees);
        return out;
    }
}
