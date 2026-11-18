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

package com.hbmspace.world.gen;

import com.hbmspace.dim.BiomeGenBaseCelestial;
import com.mojang.serialization.MapCodec;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.QuartPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeResolver;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;

public final class CelestialBiomeSource extends BiomeSource {

    public static final MapCodec<CelestialBiomeSource> CODEC =
            Biome.LIST_CODEC
                    .fieldOf("biomes")
                    .xmap(CelestialBiomeSource::new, source -> source.biomes);

    private final HolderSet<Biome> biomes;
    private final Map<ResourceKey<Biome>, Holder<Biome>> byKey = new HashMap<>();

    public CelestialBiomeSource(HolderSet<Biome> biomes) {
        this.biomes = biomes;
        for (Holder<Biome> biome : biomes) byKey.put(biome.unwrapKey().orElseThrow(), biome);
    }

    @Override
    protected MapCodec<? extends BiomeSource> codec() {
        return CODEC;
    }

    @Override
    protected Stream<Holder<Biome>> collectPossibleBiomes() {
        return biomes.stream();
    }

    @Override
    public BiomeResolver createResolver(Climate.Sampler sampler) {
        return (quartX, quartY, quartZ) ->
                biome(
                        (int)
                                sampler.temperature()
                                        .sampleValue(
                                                QuartPos.toBlock(quartX),
                                                QuartPos.toBlock(quartY),
                                                QuartPos.toBlock(quartZ)));
    }

    private Holder<Biome> biome(int id) {
        BiomeGenBaseCelestial biome = BiomeGenBaseCelestial.getBiome(id);
        Holder<Biome> holder = byKey.get(biome.key);
        if (holder == null) {
            throw new IllegalStateException(
                    biome.key.identifier() + " is absent from its level's biome list");
        }
        return holder;
    }
}
