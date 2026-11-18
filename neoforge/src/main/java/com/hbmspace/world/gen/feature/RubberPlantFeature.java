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

import com.hbm.world.gen.WorldgenHeight;
import com.hbmspace.blocks.FloraBlocksSpace.RubberPlant;
import com.hbmspace.blocks.FloraBlocksSpace.RubberTall;
import com.hbmspace.blocks.FloraBlocksSpace;
import com.hbmspace.blocks.TerrainBlocksSpace;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.Feature;

public record RubberPlantFeature(boolean forest) implements Feature {

    public static final MapCodec<RubberPlantFeature> CODEC =
            RecordCodecBuilder.mapCodec(
                    i ->
                            i.group(
                                            Codec.BOOL
                                                    .fieldOf("forest")
                                                    .forGetter(RubberPlantFeature::forest))
                                    .apply(i, RubberPlantFeature::new));

    @Override
    public MapCodec<RubberPlantFeature> codec() {
        return CODEC;
    }

    @Override
    public boolean place(
            WorldGenLevel level, ChunkGenerator generator, RandomSource random, BlockPos origin) {
        RandomSource rand = random;
        boolean forest = this.forest;
        Block grass = FloraBlocksSpace.RUBBER_GRASS.get();
        Block sand = TerrainBlocksSpace.VINYL_SAND.get();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        boolean placed = false;

        for (int l = 0; l < 64; ++l) {
            pos.set(
                    origin.getX() + rand.nextInt(8) - rand.nextInt(8),
                    origin.getY() + rand.nextInt(4) - rand.nextInt(4),
                    origin.getZ() + rand.nextInt(8) - rand.nextInt(8));
            BlockState below = level.getBlockState(pos.below());

            if (below.is(grass)) {
                level.setBlock(
                        pos, plant(RubberPlant.values()[rand.nextInt(3)]), Block.UPDATE_CLIENTS);
                placed = true;
            } else if (below.is(sand)) {
                if (rand.nextInt(64) == 0) {
                    ChunkAccess chunk = WorldgenHeight.heldChunk(level, pos.getX(), pos.getZ());
                    if (chunk != null) {
                        level.setBlock(
                                pos.atY(WorldgenHeight.ground(chunk, pos.getX(), pos.getZ())),
                                tall(RubberTall.CACT),
                                Block.UPDATE_CLIENTS);
                    }
                }
            }

            if (forest && below.is(grass)) {
                RubberTall bush = rand.nextInt(2) == 0 ? RubberTall.BUSH : RubberTall.FLOWER;
                RubberPlant other = RubberPlant.values()[rand.nextInt(3)];

                if (rand.nextInt(4) == 0) {
                    level.setBlock(pos, tall(bush), Block.UPDATE_CLIENTS);
                } else {
                    level.setBlock(pos, plant(other), Block.UPDATE_CLIENTS);
                }
                if (rand.nextInt(10) == 0) {
                    level.setBlock(pos, tall(RubberTall.CACT), Block.UPDATE_CLIENTS);
                }
                placed = true;
            }
        }

        return placed;
    }

    private static BlockState plant(RubberPlant type) {
        return FloraBlocksSpace.RUBBER_PLANT[type.ordinal()].get().defaultBlockState();
    }

    private static BlockState tall(RubberTall type) {
        return FloraBlocksSpace.RUBBER_TALL[type.ordinal()].get().defaultBlockState();
    }
}
