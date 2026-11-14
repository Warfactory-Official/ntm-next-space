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

package com.hbmspace.dim;

import com.hbmspace.HbmSpace;
import com.hbmspace.dim.ike.BiomeGenIke;
import com.hbmspace.dim.moon.BiomeGenMoon;
import com.hbmspace.dim.thatmo.BiomeGenThatmo;
import com.hbmspace.world.gen.layer.BiomeLayers;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import org.jspecify.annotations.Nullable;

public abstract class BiomeGenBaseCelestial {
    private static final BiomeGenBaseCelestial[] BY_ID = new BiomeGenBaseCelestial[256];

    private static final Map<ResourceKey<Biome>, Supplier<? extends BiomeGenBaseCelestial>> BY_KEY =
            new LinkedHashMap<>();

    public record Height(float rootHeight, float variation) {}

    public record SpawnListEntry(
            Supplier<? extends EntityType<?>> type,
            int weight,
            int minGroupCount,
            int maxGroupCount) {}

    public static final Height HEIGHT_DEFAULT = new Height(0.1F, 0.2F);

    public static final BiomeGenBaseCelestial RIVER =
            new BiomeGenBaseCelestial(
                    Identifier.withDefaultNamespace("river"), BiomeLayers.RIVER) {}.setBiomeName(
                            "River")
                    .setHeight(new Height(-0.5F, 0.0F));

    static {
        declare("mun", () -> BiomeGenMoon.biome);
        declare("ike", () -> BiomeGenIke.biome);
        declare("thatmo", () -> BiomeGenThatmo.biome);
    }

    private static void declare(String path, Supplier<? extends BiomeGenBaseCelestial> biome) {
        BY_KEY.put(
                ResourceKey.create(
                        Registries.BIOME, Identifier.fromNamespaceAndPath(HbmSpace.MOD_ID, path)),
                biome);
    }

    public final ResourceKey<Biome> key;
    public final int biomeID;
    public String biomeName;

    public Block topBlock = Blocks.GRASS_BLOCK;
    public Block fillerBlock = Blocks.DIRT;
    public float rootHeight = HEIGHT_DEFAULT.rootHeight();
    public float heightVariation = HEIGHT_DEFAULT.variation();
    public float temperature = 0.5F;
    public float rainfall = 0.5F;
    public int waterColorMultiplier = 0xFFFFFF;
    protected boolean enableRain = true;

    protected final List<SpawnListEntry> creatures = new ArrayList<>();
    protected final List<SpawnListEntry> monsters = new ArrayList<>();
    protected final List<SpawnListEntry> waterCreatures = new ArrayList<>();
    protected final List<SpawnListEntry> caveCreatures = new ArrayList<>();

    protected BiomeGenBaseCelestial(Identifier id, int biomeID) {
        this.key = ResourceKey.create(Registries.BIOME, id);
        this.biomeID = biomeID;
        synchronized (BY_ID) {
            if (BY_ID[biomeID] != null) {
                throw new IllegalStateException(
                        id
                                + " takes biome id "
                                + biomeID
                                + " from "
                                + BY_ID[biomeID].key.identifier());
            }
            BY_ID[biomeID] = this;
        }
    }

    public static BiomeGenBaseCelestial get(ResourceKey<Biome> key) {
        Supplier<? extends BiomeGenBaseCelestial> biome = BY_KEY.get(key);
        if (biome == null)
            throw new IllegalStateException(key.identifier() + " is no celestial biome");
        return biome.get();
    }

    public static void buildDeclared() {
        BY_KEY.values().forEach(Supplier::get);
    }

    public static BiomeGenBaseCelestial getBiome(int id) {
        BiomeGenBaseCelestial biome = getBiomeOrNull(id);
        if (biome == null) throw new IllegalStateException("no celestial biome has id " + id);
        return biome;
    }

    public static @Nullable BiomeGenBaseCelestial getBiomeOrNull(int id) {
        return id >= 0 && id < BY_ID.length ? BY_ID[id] : null;
    }

    protected BiomeGenBaseCelestial setBiomeName(String name) {
        this.biomeName = name;
        return this;
    }

    protected BiomeGenBaseCelestial setDisableRain() {
        this.enableRain = false;
        return this;
    }

    protected BiomeGenBaseCelestial setTemperatureRainfall(float temperature, float rainfall) {
        this.temperature = temperature;
        this.rainfall = rainfall;
        return this;
    }

    protected BiomeGenBaseCelestial setHeight(Height height) {
        this.rootHeight = height.rootHeight();
        this.heightVariation = height.variation();
        return this;
    }

    public boolean canRain() {
        return enableRain;
    }

    public List<SpawnListEntry> creatures() {
        return creatures;
    }

    public List<SpawnListEntry> monsters() {
        return monsters;
    }

    public List<SpawnListEntry> waterCreatures() {
        return waterCreatures;
    }

    public List<SpawnListEntry> caveCreatures() {
        return caveCreatures;
    }

    public float getSpawningChance() {
        return 0.1F;
    }
}
