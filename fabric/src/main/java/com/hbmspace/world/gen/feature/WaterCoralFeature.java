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

import com.hbm.blocks.ModBlocks;
import com.hbmspace.blocks.FloraBlocksSpace;
import com.hbmspace.blocks.KelpBlock;
import com.hbmspace.blocks.TerrainBlocksSpace;
import com.hbmspace.blocks.WaterPlantBlock;
import com.hbmspace.blocks.fluid.GenericFluidBlock;
import com.hbmspace.blocks.generic.TallPlantWaterBlock;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.Feature;

public record WaterCoralFeature(int seaLevel) implements Feature {
    public static final MapCodec<WaterCoralFeature> CODEC =
            Codec.INT
                    .fieldOf("sea_level")
                    .xmap(WaterCoralFeature::new, WaterCoralFeature::seaLevel);

    @Override
    public MapCodec<WaterCoralFeature> codec() {
        return CODEC;
    }

    @Override
    public boolean place(
            WorldGenLevel level, ChunkGenerator generator, RandomSource random, BlockPos origin) {
        RandomSource rand = random;
        int kinds = FloraBlocksSpace.Coral.values().length;
        boolean placed = false;

        for (int l = 0; l < 16; ++l) {
            int px = origin.getX() + rand.nextInt(8) - rand.nextInt(8);
            int py = origin.getY() + rand.nextInt(4) - rand.nextInt(4);
            int pz = origin.getZ() + rand.nextInt(8) - rand.nextInt(8);
            BlockPos at = new BlockPos(px, py, pz);

            if (py < seaLevel - 1
                    && isWaterMaterial(level.getBlockState(at))
                    && level.getBlockState(at.below()).is(TerrainBlocksSpace.LAYTHE_SILT.get())) {
                int kind = (rand.nextInt(kinds) + 8) % kinds;
                BlockState block =
                        FloraBlocksSpace.LAYTHE_CORAL_BLOCK[kind].get().defaultBlockState();
                BlockState coral = FloraBlocksSpace.LAYTHE_CORAL[kind].get().defaultBlockState();

                level.setBlock(at.below(), block, Block.UPDATE_CLIENTS);

                int oy = 0;
                int ox = 0;
                int oz = 0;
                while (oy < 8) {
                    if (py + oy > seaLevel - 3) break;
                    if (rand.nextBoolean()) break;

                    if (rand.nextBoolean()) {
                        if (rand.nextBoolean()) {
                            ox += rand.nextBoolean() ? 1 : -1;
                        } else {
                            oz += rand.nextBoolean() ? 1 : -1;
                        }
                    }

                    BlockPos stem = at.offset(ox, oy, oz);
                    level.setBlock(stem, block, Block.UPDATE_CLIENTS);
                    if (rand.nextBoolean())
                        level.setBlock(stem.east(), coral, Block.UPDATE_CLIENTS);
                    if (rand.nextBoolean())
                        level.setBlock(stem.west(), coral, Block.UPDATE_CLIENTS);
                    if (rand.nextBoolean())
                        level.setBlock(stem.south(), coral, Block.UPDATE_CLIENTS);
                    if (rand.nextBoolean())
                        level.setBlock(stem.north(), coral, Block.UPDATE_CLIENTS);

                    oy++;
                }

                level.setBlock(at.offset(ox, oy, oz), coral, Block.UPDATE_CLIENTS);
                placed = true;
            }
        }

        return placed;
    }

    private static boolean isWaterMaterial(BlockState state) {
        Block block = state.getBlock();
        return state.is(Blocks.WATER)
                || block instanceof WaterPlantBlock
                || block instanceof KelpBlock
                || block instanceof TallPlantWaterBlock
                || block instanceof GenericFluidBlock
                || block == ModBlocks.SULFURIC_ACID_BLOCK.get()
                || block == ModBlocks.SCHRABIDIC_BLOCK.get();
    }
}
