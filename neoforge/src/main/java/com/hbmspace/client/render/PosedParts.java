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
