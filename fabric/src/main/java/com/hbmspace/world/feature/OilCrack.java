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

package com.hbmspace.world.feature;

import com.hbm.blocks.ModBlocks;
import com.hbm.entity.item.FallingBlockNTEntity;
import com.hbm.util.RegistryUtil;
import com.hbm.world.feature.FractalSimplexNoise;
import com.hbmspace.blocks.FloraBlocksSpace;
import com.hbmspace.blocks.TerrainBlocksSpace;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;

public final class OilCrack {

    private static final FractalSimplexNoise CRACKS = new FractalSimplexNoise(73470L, 4);

    private OilCrack() {}

    public static void generateCrack(
            ServerLevel level, RandomSource rand, int x, int z, int width, int count) {
        Block basalt = BuiltInRegistries.BLOCK.getOptional(RegistryUtil.id("basalt")).orElseThrow();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        BlockPos.MutableBlockPos below = new BlockPos.MutableBlockPos();

        for (int i = 0; i < count * 10; i++) {
            int rX = x + (int) (rand.nextGaussian() * width);
            int rZ = z + (int) (rand.nextGaussian() * width);
            if (CRACKS.get(rX * 0.25, rZ * 0.25) < 0.9) continue;

            int rY = level.getHeight(Heightmap.Types.MOTION_BLOCKING, rX, rZ);
            for (int y = rY; y > rY - 4; y--) {
                BlockState under = level.getBlockState(below.set(rX, y - 1, rZ));
                BlockState ground = level.getBlockState(pos.set(rX, y, rZ));

                if (under.isRedstoneConductor(level, below)) {
                    for (int type = 0; type < FloraBlocksSpace.RUBBER_PLANT.length; type++) {
                        if (ground.is(FloraBlocksSpace.RUBBER_PLANT[type].get())) {
                            level.setBlock(
                                    pos, deadPlant(type).defaultBlockState(), Block.UPDATE_ALL);
                        }
                    }
                }

                if (ground.is(FloraBlocksSpace.RUBBER_GRASS.get())) {
                    level.setBlock(
                            pos,
                            TerrainBlocksSpace.RUBBER_SILT.get().defaultBlockState(),
                            Block.UPDATE_ALL);
                    break;
                }

                if (ground.is(TerrainBlocksSpace.RUBBER_SILT.get())
                        || ground.is(TerrainBlocksSpace.VINYL_SAND.get())
                        || ground.is(ModBlocks.SELLAFIELD_SLAKED.get())
                        || ground.is(basalt)) {
                    if (under.getDestroySpeed(level, below) == -1.0F) break;
                    level.setBlock(below, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);

                    if (!ground.is(TerrainBlocksSpace.VINYL_SAND.get()))
                        fall(level, x, z, pos.immutable(), ground);
                    break;
                }
            }
        }
    }

    private static Block deadPlant(int meta) {
        return switch (meta) {
            case 0 -> ModBlocks.PLANT_DEAD.get();
            case 1 -> ModBlocks.PLANT_DEAD_GRASS.get();
            default -> ModBlocks.PLANT_DEAD_FLOWER.get();
        };
    }

    private static void fall(ServerLevel level, int x, int z, BlockPos pos, BlockState ground) {
        int range = 64;
        if (level.hasChunksAt(x - range, z - range, x + range, z + range)) {
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
            FallingBlockNTEntity.fall(level, pos, ground).dropItem = false;
            return;
        }
        level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
        int y = pos.getY();
        while (y > level.getMinY() && FallingBlock.isFree(level.getBlockState(pos.atY(y - 1)))) --y;
        if (y > level.getMinY()) level.setBlock(pos.atY(y), ground, Block.UPDATE_ALL);
    }
}
