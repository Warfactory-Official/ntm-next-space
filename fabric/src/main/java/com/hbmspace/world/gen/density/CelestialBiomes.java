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
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.PositionalRandomFactory;
import org.jspecify.annotations.Nullable;

public record CelestialBiomes(Identifier layers, boolean terrain, @Nullable Regions regions)
        implements SeededDensityFunction {

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
                                    .apply(
                                            i,
                                            (layers, terrain) ->
                                                    new CelestialBiomes(layers, terrain, null)));

    private static final KeyDispatchDataCodec<CelestialBiomes> CODEC =
            KeyDispatchDataCodec.of(MAP_CODEC);

    @Override
    public DensityFunction seeded(PositionalRandomFactory random) {
        CelestialLayers.Stack stack =
                CelestialLayers.build(layers, random.fromHashOf(layers).nextLong());
        return new CelestialBiomes(
                layers, terrain, new Regions(terrain ? stack.terrain() : stack.world()));
    }

    @Override
    public double compute(FunctionContext context) {
        if (regions == null)
            throw new IllegalStateException(layers + " biomes computed before a level wired them");
        return regions.at(context.blockX() >> 2, context.blockZ() >> 2);
    }

    @Override
    public void fillArray(double[] output, ContextProvider contextProvider) {
        contextProvider.fillAllDirectly(output, this);
    }

    @Override
    public DensityFunction mapChildren(Visitor visitor) {
        return this;
    }

    @Override
    public double minValue() {
        return 0.0D;
    }

    @Override
    public double maxValue() {
        return 255.0D;
    }

    @Override
    public KeyDispatchDataCodec<? extends DensityFunction> codec() {
        return CODEC;
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
