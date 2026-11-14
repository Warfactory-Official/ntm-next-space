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

package com.hbmspace.world.gen;

import com.google.common.base.Suppliers;
import com.hbm.world.feature.FractalSimplexNoise;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.RandomState;
import org.jspecify.annotations.Nullable;

public record Terrace(
        ResourceKey<Biome> biome,
        Identifier domain,
        double scale,
        int maxAddition,
        int step,
        int topsoil,
        BlockState stone,
        BlockState surface,
        BlockState fill) {

    private static final Map<Identifier, Supplier<Terrace>> TERRACES = new HashMap<>();

    public static void register(Identifier settings, Supplier<Terrace> terrace) {
        if (TERRACES.putIfAbsent(settings, Suppliers.memoize(terrace::get)) != null) {
            throw new IllegalStateException(settings + " already has a terrace");
        }
    }

    public static @Nullable Terrace of(Holder<NoiseGeneratorSettings> settings) {
        Supplier<Terrace> terrace =
                settings.unwrapKey().map(key -> TERRACES.get(key.identifier())).orElse(null);
        return terrace == null ? null : terrace.get();
    }

    public final class Cover {

        private final ChunkAccess chunk;
        private final int[] bases;
        private final int[] tops;
        private final boolean[] raised;

        private Cover(ChunkAccess chunk, int[] bases, int[] tops, boolean[] raised) {
            this.chunk = chunk;
            this.bases = bases;
            this.tops = tops;
            this.raised = raised;
        }

        public void lay() {
            ChunkPos at = chunk.getPos();
            BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
            for (int lx = 0; lx < 16; lx++) {
                for (int lz = 0; lz < 16; lz++) {
                    int i = lx * 16 + lz;
                    if (!raised[i]) continue;
                    int top = tops[i];
                    boolean flat =
                            (lx == 0 || tops[i - 16] == top)
                                    && (lx == 15 || tops[i + 16] == top)
                                    && (lz == 0 || tops[i - 1] == top)
                                    && (lz == 15 || tops[i + 1] == top);
                    for (int y = Math.max(top - topsoil, bases[i] + 1);
                            y < top && y <= chunk.getMaxY();
                            y++) {
                        BlockState state = y == top - 1 && flat ? fill : surface;
                        chunk.setBlockState(pos.set(at.getBlockX(lx), y, at.getBlockZ(lz)), state);
                    }
                }
            }
        }
    }

    private FractalSimplexNoise field(RandomState random) {
        return new FractalSimplexNoise(
                random.getOrCreateRandomFactory(domain).at(0, 0, 0).nextLong(), 4);
    }

    private int top(FractalSimplexNoise field, int x, int z, int base) {
        int addition = (int) ((field.get(x * scale, z * scale) + 1.0D) / 2.0D * maxAddition);
        return base + addition / step * step;
    }

    public int firstFreeHeight(
            int x, int z, int firstFree, int maxY, BiomeManager biomes, RandomState random) {
        int base = firstFree - 1;
        if (!biomes.getBiome(new BlockPos(x, base, z)).is(biome)) return firstFree;
        return Math.max(firstFree, Math.min(top(field(random), x, z, base), maxY + 1));
    }

    public Cover raise(ChunkAccess chunk, BiomeManager biomes, RandomState random) {
        FractalSimplexNoise field = field(random);
        ChunkPos at = chunk.getPos();
        int[] bases = new int[256];
        int[] tops = new int[256];
        boolean[] raised = new boolean[256];
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        for (int lx = 0; lx < 16; lx++) {
            for (int lz = 0; lz < 16; lz++) {
                int i = lx * 16 + lz;
                bases[i] =
                        Math.max(
                                chunk.getHeight(Heightmap.Types.WORLD_SURFACE_WG, lx, lz),
                                chunk.getMinY());
                tops[i] = top(field, at.getBlockX(lx), at.getBlockZ(lz), bases[i]);
            }
        }

        for (int lx = 0; lx < 16; lx++) {
            for (int lz = 0; lz < 16; lz++) {
                int i = lx * 16 + lz;
                int x = at.getBlockX(lx);
                int z = at.getBlockZ(lz);
                if (tops[i] <= bases[i] || !biomes.getBiome(pos.set(x, bases[i], z)).is(biome))
                    continue;
                raised[i] = true;
                for (int y = bases[i] + 1; y < tops[i] - topsoil && y <= chunk.getMaxY(); y++) {
                    chunk.setBlockState(pos.set(x, y, z), stone);
                }
            }
        }
        return new Cover(chunk, bases, tops, raised);
    }
}
