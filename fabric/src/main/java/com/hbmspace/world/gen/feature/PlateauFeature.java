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
import com.hbm.world.feature.FractalSimplexNoise;
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
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;

public record PlateauFeature(
        Identifier domain,
        double scale,
        int maxAddition,
        int step,
        int topsoil,
        BlockState stone,
        BlockState surface,
        BlockState fill,
        HolderSet<Biome> biomes)
        implements Feature {

    public static final MapCodec<PlateauFeature> CODEC =
            RecordCodecBuilder.mapCodec(
                    i ->
                            i.group(
                                            Identifier.CODEC
                                                    .fieldOf("domain")
                                                    .forGetter(PlateauFeature::domain),
                                            Codec.DOUBLE
                                                    .fieldOf("scale")
                                                    .forGetter(PlateauFeature::scale),
                                            Codec.intRange(0, 64)
                                                    .fieldOf("max_addition")
                                                    .forGetter(PlateauFeature::maxAddition),
                                            Codec.intRange(1, 64)
                                                    .fieldOf("step")
                                                    .forGetter(PlateauFeature::step),
                                            Codec.intRange(0, 64)
                                                    .fieldOf("topsoil")
                                                    .forGetter(PlateauFeature::topsoil),
                                            BlockState.CODEC
                                                    .fieldOf("stone")
                                                    .forGetter(PlateauFeature::stone),
                                            BlockState.CODEC
                                                    .fieldOf("surface")
                                                    .forGetter(PlateauFeature::surface),
                                            BlockState.CODEC
                                                    .fieldOf("fill")
                                                    .forGetter(PlateauFeature::fill),
                                            RegistryCodecs.<Biome>holderSet(
                                                            (ResourceKey<? extends Registry<Biome>>)
                                                                    Registries.BIOME)
                                                    .fieldOf("biomes")
                                                    .forGetter(PlateauFeature::biomes))
                                    .apply(i, PlateauFeature::new));

    @Override
    public MapCodec<PlateauFeature> codec() {
        return CODEC;
    }

    public static int top(
            PlateauFeature config, FractalSimplexNoise field, int x, int z, int base) {
        int addition =
                (int)
                        ((field.get(x * config.scale(), z * config.scale()) + 1.0D)
                                / 2.0D
                                * config.maxAddition());
        return base + addition / config.step() * config.step();
    }

    @Override
    public boolean place(
            WorldGenLevel level, ChunkGenerator generator, RandomSource random, BlockPos origin) {
        PlateauFeature config = this;
        FractalSimplexNoise field = NtmWorldgenFields.get(level).fractal(config.domain(), 4);
        int[] bases = new int[256];
        int[] tops = new int[256];

        for (int lx = 0; lx < 16; lx++) {
            for (int lz = 0; lz < 16; lz++) {
                int x = origin.getX() + lx;
                int z = origin.getZ() + lz;
                int base = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z) - 1;
                bases[lx * 16 + lz] = base;
                tops[lx * 16 + lz] = top(config, field, x, z, base);
            }
        }

        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        boolean placed = false;
        for (int lx = 0; lx < 16; lx++) {
            for (int lz = 0; lz < 16; lz++) {
                int x = origin.getX() + lx;
                int z = origin.getZ() + lz;
                int top = tops[lx * 16 + lz];
                if (top == bases[lx * 16 + lz]
                        || !config.biomes().contains(level.getBiome(pos.set(x, top, z)))) continue;
                boolean flat =
                        (lx == 0 || tops[(lx - 1) * 16 + lz] == top)
                                && (lx == 15 || tops[(lx + 1) * 16 + lz] == top)
                                && (lz == 0 || tops[lx * 16 + lz - 1] == top)
                                && (lz == 15 || tops[lx * 16 + lz + 1] == top);

                for (int y = bases[lx * 16 + lz] + 1; y < top && y <= level.getMaxY(); y++) {
                    BlockState state =
                            y < top - config.topsoil()
                                    ? config.stone()
                                    : y == top - 1 && flat ? config.fill() : config.surface();
                    level.setBlock(pos.set(x, y, z), state, Block.UPDATE_CLIENTS);
                }
                placed = true;
            }
        }
        return placed;
    }
}
