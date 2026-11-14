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

package com.hbmspace.world.gen.feature;

import com.hbm.blocks.ModBlocks;
import com.hbmspace.blocks.ModBlocksSpace;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.Feature;

public record MohoVentFeature() implements Feature {
    public static final MapCodec<MohoVentFeature> CODEC = MapCodec.unit(MohoVentFeature::new);

    @Override
    public MapCodec<MohoVentFeature> codec() {
        return CODEC;
    }

    @Override
    public boolean place(
            WorldGenLevel level, ChunkGenerator generator, RandomSource random, BlockPos origin) {
        BlockState geysir = ModBlocks.GEYSIR_NETHER.get().defaultBlockState();
        BlockState netherrack = Blocks.NETHERRACK.defaultBlockState();
        boolean placed = false;

        for (int y = origin.getY() - 5; y <= origin.getY(); y++) {
            BlockPos pos = origin.atY(y);
            BlockState ground = level.getBlockState(pos);
            if (!level.isEmptyBlock(pos.above())
                    || !ground.is(ModBlocksSpace.MOHO_STONE.get())
                            && !ground.is(ModBlocksSpace.MOHO_REGOLITH.get())) {
                continue;
            }

            level.setBlock(pos, geysir, Block.UPDATE_ALL);
            for (Direction side : Direction.Plane.HORIZONTAL) {
                level.setBlock(pos.relative(side), netherrack, Block.UPDATE_ALL);
                level.setBlock(pos.relative(side).below(), netherrack, Block.UPDATE_ALL);
            }
            placed = true;
        }

        return placed;
    }
}
