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

package com.hbmspace.world.gen.density;

import com.hbmspace.world.gen.layer.BiomeLayer;
import com.hbmspace.world.gen.layer.CelestialLayers;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Interval;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.densityfunction.*;

public record CelestialBiomes(Identifier layers, boolean terrain) implements DensityFunction {
    public static final MapCodec<CelestialBiomes> MAP_CODEC =
            RecordCodecBuilder.mapCodec(
                    i ->
                            i.group(
                                            Identifier.CODEC
                                                    .fieldOf("layers")
                                                    .forGetter(CelestialBiomes::layers),
                                            Codec.BOOL
                                                    .optionalFieldOf("terrain", false)
                                                    .forGetter(CelestialBiomes::terrain))
                                    .apply(i, CelestialBiomes::new));

    @Override
    public DensitySampler compileSampler(CompileContext context) {
        CelestialLayers.Stack stack =
                CelestialLayers.build(layers, context.createRandom(layers).nextLong());
        Regions regions = new Regions(terrain ? stack.terrain() : stack.world());
        return new DensitySampler() {
            @Override
            public float sampleValue(SamplerContext context, int x, int y, int z) {
                return regions.at(x >> 2, z >> 2);
            }

            @Override
            public void sampleVolume(
                    SamplerContext context, DensityBuffer output, DensityVolume volume) {
                DensitySampler.sampleVolumeNaive(context, output, volume, this);
            }
        };
    }

    @Override
    public DensityFunction rewriteChildren(DfRewriteRule rule) {
        return this;
    }

    @Override
    public Interval range() {
        return Interval.of(0.0F, 255.0F);
    }

    @Override
    public int domainAxes() {
        return AXIS_X | AXIS_Z;
    }

    @Override
    public MapCodec<CelestialBiomes> codec() {
        return MAP_CODEC;
    }

    public static final class Regions {

        private static final int SIZE = 16;
        private static final int KEPT = 8;

        private final BiomeLayer layer;
        private final ThreadLocal<Kept> kept = ThreadLocal.withInitial(Kept::new);

        Regions(BiomeLayer layer) {
            this.layer = layer;
        }

        public BiomeLayer layer() {
            return layer;
        }

        int at(int quartX, int quartZ) {
            int regionX = Math.floorDiv(quartX, SIZE);
            int regionZ = Math.floorDiv(quartZ, SIZE);
            int[] ids = kept.get().region(layer, ChunkPos.pack(regionX, regionZ), regionX, regionZ);
            return ids[Math.floorMod(quartX, SIZE) + Math.floorMod(quartZ, SIZE) * SIZE];
        }

        private static final class Kept {

            private final long[] keys = new long[KEPT];
            private final int[][] regions = new int[KEPT][];
            private int next;

            int[] region(BiomeLayer layer, long key, int regionX, int regionZ) {
                for (int i = 0; i < KEPT; i++) {
                    if (regions[i] != null && keys[i] == key) return regions[i];
                }
                int slot = next;
                next = (next + 1) % KEPT;
                keys[slot] = key;
                regions[slot] = layer.area(regionX * SIZE, regionZ * SIZE, SIZE, SIZE);
                return regions[slot];
            }
        }
    }
}
