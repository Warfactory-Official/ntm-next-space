// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.dim;

import com.hbmspace.dim.legacy.layer.GenLayer;
import com.hbmspace.dim.legacy.layer.GenLayerVoronoiZoom;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.HashMap;
import java.util.Map;
import java.util.function.LongFunction;
import java.util.stream.Stream;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;

public class WorldChunkManagerCelestial extends BiomeSource {

    public static final MapCodec<WorldChunkManagerCelestial> CODEC =
            RecordCodecBuilder.mapCodec(
                    i ->
                            i.group(
                                            Identifier.CODEC
                                                    .fieldOf("layers")
                                                    .forGetter(s -> s.layers),
                                            Biome.LIST_CODEC
                                                    .fieldOf("biomes")
                                                    .forGetter(s -> s.biomes))
                                    .apply(i, i.stable(WorldChunkManagerCelestial::new)));

    private static final Map<Identifier, LongFunction<BiomeGenLayers>> STACKS = new HashMap<>();

    public static void register(Identifier layers, LongFunction<BiomeGenLayers> stack) {
        if (STACKS.putIfAbsent(layers, stack) != null)
            throw new IllegalStateException(layers + " is already registered");
    }

    public static final class BiomeGenLayers {

        public final GenLayer biomeLayer;
        public final GenLayer biomeDetailLayer;

        public BiomeGenLayers(GenLayer biomeLayer, GenLayer biomeDetailLayer, long seed) {
            this.biomeLayer = biomeLayer;
            this.biomeDetailLayer = biomeDetailLayer;

            biomeLayer.initWorldGenSeed(seed);
            biomeDetailLayer.initWorldGenSeed(seed);
        }
    }

    private final Identifier layers;
    private final HolderSet<Biome> biomes;
    private final Map<ResourceKey<Biome>, Holder<Biome>> byKey = new HashMap<>();
    private volatile long seed;
    private volatile boolean prepared;
    private final ThreadLocal<Stack> stacks = ThreadLocal.withInitial(this::newStack);

    public WorldChunkManagerCelestial(Identifier layers, HolderSet<Biome> biomes) {
        this.layers = layers;
        this.biomes = biomes;
        for (Holder<Biome> biome : biomes) {
            byKey.put(biome.unwrapKey().orElseThrow(), biome);
        }
    }

    public void prepare(long seed) {
        if (prepared) {
            if (this.seed != seed)
                throw new IllegalStateException("a celestial biome source serves one seed");
            return;
        }
        this.seed = seed;
        this.prepared = true;
    }

    private Stack newStack() {
        if (!prepared)
            throw new IllegalStateException(layers + " queried before its level handed it a seed");
        LongFunction<BiomeGenLayers> stack = STACKS.get(layers);
        if (stack == null)
            throw new IllegalStateException("no biome layers are registered as " + layers);
        BiomeGenLayers built = stack.apply(seed);
        if (!(built.biomeDetailLayer instanceof GenLayerVoronoiZoom voronoi)) {
            throw new IllegalStateException(layers + " details its biomes without a voronoi zoom");
        }
        return new Stack(built, voronoi.parent());
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
    public Holder<Biome> getNoiseBiome(
            int quartX, int quartY, int quartZ, Climate.Sampler sampler) {
        BiomeGenBaseCelestial biome =
                BiomeGenBaseCelestial.getBiome(stacks.get().worldAt(quartX, quartZ));
        Holder<Biome> holder = byKey.get(biome.key);
        if (holder == null)
            throw new IllegalStateException(
                    layers + " emits " + biome.key.identifier() + ", absent from its biome list");
        return holder;
    }

    public int[] getBiomesForGeneration(int x, int z, int width, int length) {
        return stacks.get().layers.biomeLayer.getInts(x, z, width, length);
    }

    public int[] loadBlockGeneratorData(int x, int z, int width, int length) {
        return stacks.get().layers.biomeDetailLayer.getInts(x, z, width, length);
    }

    public int getBiomeGenAt(int x, int z) {
        return stacks.get().detailAt(x, z);
    }

    private static final class Stack {

        final BiomeGenLayers layers;
        final GenLayer world;

        long worldCell = Long.MIN_VALUE;
        int[] worldInts;
        long detailChunk = Long.MIN_VALUE;
        int[] detailInts;

        Stack(BiomeGenLayers layers, GenLayer world) {
            this.layers = layers;
            this.world = world;
        }

        int worldAt(int quartX, int quartZ) {
            long cell = pack(quartX >> 2, quartZ >> 2);
            if (cell != worldCell) {
                worldInts = world.getInts(quartX & ~3, quartZ & ~3, 4, 4);
                worldCell = cell;
            }
            return worldInts[(quartX & 3) + (quartZ & 3) * 4];
        }

        int detailAt(int x, int z) {
            long chunk = pack(x >> 4, z >> 4);
            if (chunk != detailChunk) {
                detailInts = layers.biomeDetailLayer.getInts(x & ~15, z & ~15, 16, 16);
                detailChunk = chunk;
            }
            return detailInts[(x & 15) + (z & 15) * 16];
        }

        private static long pack(int x, int z) {
            return (long) x << 32 | (z & 0xFFFFFFFFL);
        }
    }
}
