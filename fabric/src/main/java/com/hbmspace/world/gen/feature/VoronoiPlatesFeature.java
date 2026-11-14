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

import com.hbm.world.NtmWorldgenFields;
import com.hbm.world.WorldgenHash;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.registries.codec.RegistryCodecs;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.Feature;

public record VoronoiPlatesFeature(
        int cellSize,
        double crackSize,
        int startY,
        int thickness,
        double shapeExponent,
        BlockState rock,
        BlockState surface,
        BlockState fluid,
        HolderSet<Biome> biomes)
        implements Feature {

    private static final long CELLS =
            WorldgenHash.identifier(Identifier.fromNamespaceAndPath("hbmspace", "voronoi_plates"));

    public static final MapCodec<VoronoiPlatesFeature> CODEC =
            RecordCodecBuilder.mapCodec(
                    i ->
                            i.group(
                                            Codec.intRange(1, 256)
                                                    .fieldOf("cell_size")
                                                    .forGetter(VoronoiPlatesFeature::cellSize),
                                            Codec.DOUBLE
                                                    .fieldOf("crack_size")
                                                    .forGetter(VoronoiPlatesFeature::crackSize),
                                            Codec.INT
                                                    .fieldOf("start_y")
                                                    .forGetter(VoronoiPlatesFeature::startY),
                                            Codec.intRange(0, 256)
                                                    .fieldOf("thickness")
                                                    .forGetter(VoronoiPlatesFeature::thickness),
                                            Codec.DOUBLE
                                                    .fieldOf("shape_exponent")
                                                    .forGetter(VoronoiPlatesFeature::shapeExponent),
                                            BlockState.CODEC
                                                    .fieldOf("rock")
                                                    .forGetter(VoronoiPlatesFeature::rock),
                                            BlockState.CODEC
                                                    .fieldOf("surface")
                                                    .forGetter(VoronoiPlatesFeature::surface),
                                            BlockState.CODEC
                                                    .fieldOf("fluid")
                                                    .forGetter(VoronoiPlatesFeature::fluid),
                                            RegistryCodecs.<Biome>holderSet(
                                                            (ResourceKey<? extends Registry<Biome>>)
                                                                    Registries.BIOME)
                                                    .fieldOf("biomes")
                                                    .forGetter(VoronoiPlatesFeature::biomes))
                                    .apply(i, VoronoiPlatesFeature::new));

    @Override
    public MapCodec<VoronoiPlatesFeature> codec() {
        return CODEC;
    }

    @Override
    public boolean place(
            WorldGenLevel level, ChunkGenerator generator, RandomSource random, BlockPos origin) {
        VoronoiPlatesFeature config = this;
        long seed = NtmWorldgenFields.get(level).domainSeed(CELLS);
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        boolean placed = false;

        for (int x = origin.getX(); x < origin.getX() + 16; x++) {
            for (int z = origin.getZ(); z < origin.getZ() + 16; z++) {
                if (!config.biomes().contains(level.getBiome(pos.set(x, config.startY(), z))))
                    continue;

                int top = top(config, seed, x, z);
                if (top <= config.startY()) continue;

                for (int y = config.startY(); y >= level.getMinY(); y--) {
                    BlockState state = level.getBlockState(pos.set(x, y, z));
                    if (!state.isAir() && !state.is(config.fluid().getBlock())) break;
                    level.setBlock(pos, config.rock(), Block.UPDATE_CLIENTS);
                }
                for (int y = config.startY() + 1; y < top && y <= level.getMaxY(); y++) {
                    BlockState state = level.getBlockState(pos.set(x, y, z));
                    if (state.isAir() || state.is(config.fluid().getBlock())) {
                        level.setBlock(pos, config.rock(), Block.UPDATE_CLIENTS);
                    }
                }
                level.setBlock(pos.set(x, top - 1, z), config.surface(), Block.UPDATE_CLIENTS);
                placed = true;
            }
        }
        return placed;
    }

    public static long domainSeed(long worldSeed, Identifier dimension) {
        return WorldgenHash.domainSeed(
                WorldgenHash.worldDimensionSeed(worldSeed, dimension), CELLS);
    }

    public static int top(VoronoiPlatesFeature config, long seed, int x, int z) {
        double nearest = Double.MAX_VALUE;
        double second = Double.MAX_VALUE;
        int cellX = x / config.cellSize();
        int cellZ = z / config.cellSize();
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                double distance = distance(seed, config.cellSize(), cellX + dx, cellZ + dz, x, z);
                if (distance < nearest) {
                    second = nearest;
                    nearest = distance;
                } else if (distance < second) {
                    second = distance;
                }
            }
        }

        double edge = second - nearest;
        if (edge <= config.crackSize()) return config.startY();
        double alpha =
                Math.clamp(
                        (edge - config.crackSize()) / (config.cellSize() - config.crackSize()),
                        0.0D,
                        1.0D);
        return config.startY()
                + (int) (config.thickness() * Math.pow(alpha, config.shapeExponent()));
    }

    private static double distance(long seed, int cellSize, int cellX, int cellZ, int x, int z) {
        double offsetX =
                (WorldgenHash.unitDouble(WorldgenHash.position(seed, cellX, 0, cellZ, 0)) - 0.5D)
                        * cellSize;
        double offsetZ =
                (WorldgenHash.unitDouble(WorldgenHash.position(seed, cellX, 0, cellZ, 1)) - 0.5D)
                        * cellSize;
        double dx = x - (cellX * (double) cellSize + cellSize / 2.0D + offsetX);
        double dz = z - (cellZ * (double) cellSize + cellSize / 2.0D + offsetZ);
        return Math.sqrt(dx * dx + dz * dz);
    }
}
