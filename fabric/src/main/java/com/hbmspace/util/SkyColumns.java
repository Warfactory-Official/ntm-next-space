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

package com.hbmspace.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.lighting.ChunkSkyLightSources;
import net.minecraft.world.level.lighting.LightEngine;
import net.minecraft.world.phys.shapes.Shapes;

public final class SkyColumns {

    public static int lowestSourceY(ChunkAccess chunk, int x, int z) {
        ChunkSkyLightSources sources = chunk.getSkyLightSources();
        return sources != null ? sources.getLowestSourceY(x & 15, z & 15) : scan(chunk, x, z);
    }

    public static int scan(ChunkAccess chunk, int x, int z) {
        int lx = x & 15;
        int lz = z & 15;
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(lx, 0, lz);
        BlockState above = Blocks.AIR.defaultBlockState();
        for (int y = chunk.getHeight(Heightmap.Types.WORLD_SURFACE, lx, lz);
                y >= chunk.getMinY();
                y--) {
            BlockState state = chunk.getBlockState(pos.setY(y));
            if (state.getLightDampening() != 0
                    || Shapes.faceShapeOccludes(
                            LightEngine.getOcclusionShape(above, Direction.DOWN),
                            LightEngine.getOcclusionShape(state, Direction.UP))) {
                return y + 1;
            }
            above = state;
        }
        return Integer.MIN_VALUE;
    }

    private SkyColumns() {}
}
