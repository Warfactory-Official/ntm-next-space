// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-FileCopyrightText: 2025 George Paton
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.dim;

import com.hbmspace.HbmSpace;
import com.hbmspace.dim.ike.BiomeGenIke;
import com.hbmspace.dim.legacy.LegacyDecorWorld;
import com.hbmspace.dim.legacy.LegacyWorld;
import com.hbmspace.dim.legacy.NoiseGeneratorPerlin;
import com.hbmspace.dim.legacy.layer.GenLayerRiver;
import com.hbmspace.dim.moon.BiomeGenMoon;
import com.hbmspace.dim.thatmo.BiomeGenThatmo;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Supplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

public abstract class BiomeGenBaseCelestial {
    private static final BiomeGenBaseCelestial[] BY_ID = new BiomeGenBaseCelestial[256];

    private static final Map<ResourceKey<Biome>, Supplier<? extends BiomeGenBaseCelestial>> BY_KEY =
            new LinkedHashMap<>();

    private static final NoiseGeneratorPerlin TEMPERATURE_NOISE =
            new NoiseGeneratorPerlin(new Random(1234L), 1);

    public record Height(float rootHeight, float variation) {}

    public record SpawnListEntry(
            Supplier<? extends EntityType<?>> type,
            int weight,
            int minGroupCount,
            int maxGroupCount) {}

    public static final Height HEIGHT_DEFAULT = new Height(0.1F, 0.2F);

    public static final BiomeGenBaseCelestial RIVER =
            new BiomeGenBaseCelestial(
                    Identifier.withDefaultNamespace("river"), GenLayerRiver.RIVER) {}.setBiomeName(
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
    protected boolean enableSnow;
    protected boolean enableRain = true;

    public @Nullable BiomeDecoratorCelestial theBiomeDecorator;

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

    protected BiomeGenBaseCelestial setEnableSnow() {
        this.enableSnow = true;
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

    public boolean isSnowy() {
        return enableSnow;
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

    public void decorate(
            LegacyDecorWorld world, Random rand, int chunkX, int chunkZ, boolean hasLife) {
        CelestialStrata.generate(world, rand, chunkX, chunkZ);
        if (theBiomeDecorator != null)
            theBiomeDecorator.decorate(world, rand, chunkX, chunkZ, hasLife);
    }

    public float getSpawningChance() {
        return 0.1F;
    }

    public final float getFloatTemperature(int x, int y, int z) {
        if (y > 64) {
            float f =
                    (float) TEMPERATURE_NOISE.getValue((double) x / 8.0D, (double) z / 8.0D) * 4.0F;
            return this.temperature - (f + (float) y - 64.0F) * 0.05F / 30.0F;
        }

        return this.temperature;
    }

    protected static double columnRandom(LegacyWorld world, int x, int y, int z) {
        return new Random(Mth.getSeed(x, y, z) ^ world.getSeed()).nextDouble();
    }

    public void genTerrainBlocks(
            LegacyWorld world, Random rand, BlockState[] blocks, int x, int z, double noise) {
        BlockState block = this.topBlock.defaultBlockState();
        BlockState block1 = this.fillerBlock.defaultBlockState();
        int k = -1;
        int l = (int) (noise / 3.0D + 3.0D + rand.nextDouble() * 0.25D);
        int i1 = x & 15;
        int j1 = z & 15;
        int k1 = blocks.length / 256;

        for (int l1 = 255; l1 >= 0; --l1) {
            int i2 = (j1 * 16 + i1) * k1 + l1;

            if (l1 <= rand.nextInt(5)) {
                blocks[i2] = Blocks.BEDROCK.defaultBlockState();
            } else {
                BlockState block2 = blocks[i2];

                if (block2 != null && !block2.isAir()) {
                    if (block2.getBlock() == Blocks.STONE) {
                        if (k == -1) {
                            if (l <= 0) {
                                block = null;
                                block1 = Blocks.STONE.defaultBlockState();
                            } else if (l1 >= 59 && l1 <= 64) {
                                block = this.topBlock.defaultBlockState();
                                block1 = this.fillerBlock.defaultBlockState();
                            }

                            if (l1 < 63 && (block == null || block.isAir())) {
                                if (this.getFloatTemperature(x, l1, z) < 0.15F) {
                                    block = Blocks.ICE.defaultBlockState();
                                } else {
                                    block = Blocks.WATER.defaultBlockState();
                                }
                            }

                            k = l;

                            if (l1 >= 62) {
                                blocks[i2] = block;
                            } else if (l1 < 56 - l) {
                                block = null;
                                block1 = Blocks.STONE.defaultBlockState();
                                blocks[i2] = Blocks.GRAVEL.defaultBlockState();
                            } else {
                                blocks[i2] = block1;
                            }
                        } else if (k > 0) {
                            --k;
                            blocks[i2] = block1;

                            if (k == 0 && block1.getBlock() == Blocks.SAND) {
                                k = rand.nextInt(4) + Math.max(0, l1 - 63);
                                block1 = Blocks.SANDSTONE.defaultBlockState();
                            }
                        }
                    }
                } else {
                    k = -1;
                }
            }
        }
    }
}
