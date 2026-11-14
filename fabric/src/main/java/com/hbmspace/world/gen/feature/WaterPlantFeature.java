/*
 * SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
 * SPDX-FileCopyrightText: 2025 George Paton
 * SPDX-License-Identifier: LGPL-3.0-only
 *
 * Copyright (C) 2026 movblock. All rights reserved.
 * Copyright (C) 2025 George Paton. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License version 3 only,
 * as published by the Free Software Foundation.
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this code. If not, see <https://www.gnu.org/licenses/>.
 *
 * Under GPLv3 §7(c) and §7(e), these additional terms apply to LGPL-covered
 * material authored by movblock:
 *
 * - the license grants no rights to the name "NTM NEXT: Space";
 * - a modified version must not be named NTM NEXT: Space or presented as
 *   the official release, and must be marked as different from it.
 *
 * Accurately identifying a work as derived from NTM NEXT: Space does not
 * violate these additional terms, provided it is clearly distinguished from
 * the official release and does not imply endorsement. Ports are encouraged
 * to acknowledge NTM NEXT: Space as their upstream project and provide a
 * link to it.
 */

package com.hbmspace.world.gen.feature;

import com.hbmspace.blocks.FloraBlocksSpace;
import com.hbmspace.blocks.TerrainBlocksSpace;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;

public class WaterPlantFeature extends Feature<SeaLevelConfig> {

    public WaterPlantFeature() {
        super(SeaLevelConfig.CODEC);
    }

    @Override
    public boolean place(FeaturePlaceContext<SeaLevelConfig> context) {
        WorldGenLevel level = context.level();
        RandomSource rand = context.random();
        BlockPos origin = context.origin();
        int seaLevel = context.config().seaLevel();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        boolean placed = false;

        for (int l = 0; l < 128; ++l) {
            pos.set(
                    origin.getX() + rand.nextInt(8) - rand.nextInt(8),
                    origin.getY() + rand.nextInt(4) - rand.nextInt(4),
                    origin.getZ() + rand.nextInt(8) - rand.nextInt(8));
            int py = pos.getY();

            if (py < seaLevel - 1
                    && isStillWater(level.getBlockState(pos))
                    && level.getBlockState(pos.below()).is(TerrainBlocksSpace.LAYTHE_SILT.get())) {
                switch (rand.nextInt(7)) {
                    case 0, 1, 2 ->
                            level.setBlock(
                                    pos,
                                    FloraBlocksSpace.LAYTHE_SEAGRASS.get().defaultBlockState(),
                                    Block.UPDATE_CLIENTS);
                    case 3 ->
                            level.setBlock(
                                    pos,
                                    FloraBlocksSpace.LAYTHE_GLOWGRASS.get().defaultBlockState(),
                                    Block.UPDATE_CLIENTS);
                    case 4, 5 -> {
                        if (py < seaLevel - 2) {
                            BlockState tall =
                                    FloraBlocksSpace.PLANT_TALL_LAYTHE.get().defaultBlockState();
                            level.setBlock(
                                    pos,
                                    tall.setValue(DoublePlantBlock.HALF, DoubleBlockHalf.LOWER),
                                    Block.UPDATE_CLIENTS);
                            level.setBlock(
                                    pos.above(),
                                    tall.setValue(DoublePlantBlock.HALF, DoubleBlockHalf.UPPER),
                                    Block.UPDATE_CLIENTS);
                        }
                    }
                    default -> {
                        if (py < seaLevel - 4) {
                            int height = 2 + rand.nextInt(Math.min(8, seaLevel - py - 2));
                            BlockState kelp =
                                    FloraBlocksSpace.LAYTHE_KELP.get().defaultBlockState();
                            for (int h = 0; h < height; ++h)
                                level.setBlock(pos.above(h), kelp, Block.UPDATE_CLIENTS);
                        }
                    }
                }

                placed = true;
            }
        }

        return placed;
    }

    private static boolean isStillWater(BlockState state) {
        return state.is(Blocks.WATER) && state.getFluidState().isSource();
    }
}
