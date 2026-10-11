// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-FileCopyrightText: 2024-2026 George Paton
// SPDX-FileCopyrightText: 2024 JamesH_2
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.dim;

import com.hbmspace.HbmSpace;
import com.hbmspace.dim.legacy.LegacyDecorWorld;
import com.hbmspace.dim.legacy.LegacyMath;
import com.hbmspace.dim.legacy.LegacyWorld;
import com.hbmspace.dim.legacy.NoiseGeneratorOctaves;
import com.hbmspace.dim.legacy.NoiseGeneratorPerlin;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.RandomSupport;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.structure.StructureSet;

public abstract class ChunkProviderCelestial extends ChunkGenerator implements LegacyWorld {

    private static final float[] PARABOLIC_FIELD = new float[25];

    static {
        for (int j = -2; j <= 2; ++j) {
            for (int k = -2; k <= 2; ++k) {
                PARABOLIC_FIELD[j + 2 + (k + 2) * 5] =
                        10.0F / (float) Math.sqrt((float) (j * j + k * k) + 0.2F);
            }
        }
    }

    protected Block stoneBlock = Blocks.STONE;
    protected Block seaBlock = Blocks.AIR;
    protected int seaLevel = 63;

    protected double[] firstOrderFreq = {684.412D, 684.412D, 684.412D};
    protected double[] secondOrderFreq = {684.412D, 684.412D, 684.412D};
    protected double[] thirdOrderFreq = {
        8.555150000000001D, 4.277575000000001D, 8.555150000000001D
    };
    protected double[] heightOrderFreq = {200.0D, 200.0D, 0.5D};

    protected boolean amplified = false;
    protected boolean reclamp = true;

    private volatile Terrain terrain;

    private record Terrain(
            long seed,
            NoiseGeneratorOctaves firstOrder,
            NoiseGeneratorOctaves secondOrder,
            NoiseGeneratorOctaves thirdOrder,
            NoiseGeneratorPerlin perlin,
            NoiseGeneratorOctaves heightOrder) {}

    protected ChunkProviderCelestial(BiomeSource biomeSource) {
        super(biomeSource);
    }

    @Override
    public ChunkGeneratorStructureState createState(
            HolderLookup<StructureSet> structureSets, RandomState randomState, long levelSeed) {
        prepare(levelSeed);
        return ChunkGeneratorStructureState.createForNormal(
                randomState, levelSeed, biomeSource, new AddonStructureSets(structureSets));
    }

    private record AddonStructureSets(HolderLookup<StructureSet> all)
            implements HolderLookup<StructureSet> {

        @Override
        public Stream<Holder.Reference<StructureSet>> listElements() {
            return all.listElements()
                    .filter(set -> set.key().identifier().getNamespace().equals(HbmSpace.MOD_ID));
        }

        @Override
        public Stream<HolderSet.Named<StructureSet>> listTags() {
            return all.listTags();
        }

        @Override
        public Optional<Holder.Reference<StructureSet>> get(ResourceKey<StructureSet> id) {
            return id.identifier().getNamespace().equals(HbmSpace.MOD_ID)
                    ? all.get(id)
                    : Optional.empty();
        }

        @Override
        public Optional<HolderSet.Named<StructureSet>> get(TagKey<StructureSet> id) {
            return all.get(id);
        }
    }

    public void prepare(long seed) {
        if (biomeSource instanceof WorldChunkManagerCelestial layered) layered.prepare(seed);
        if (terrain != null) {
            if (terrain.seed() != seed)
                throw new IllegalStateException("a celestial generator serves one seed");
            return;
        }
        Random rand = new Random(seed);
        NoiseGeneratorOctaves firstOrder = new NoiseGeneratorOctaves(rand, 16);
        NoiseGeneratorOctaves secondOrder = new NoiseGeneratorOctaves(rand, 16);
        NoiseGeneratorOctaves thirdOrder = new NoiseGeneratorOctaves(rand, 8);
        NoiseGeneratorPerlin perlin = new NoiseGeneratorPerlin(rand, 4);
        NoiseGeneratorOctaves heightOrder = new NoiseGeneratorOctaves(rand, 16);
        onSeed(seed);
        terrain = new Terrain(seed, firstOrder, secondOrder, thirdOrder, perlin, heightOrder);
    }

    protected void onSeed(long seed) {}

    private Terrain terrain() {
        Terrain t = terrain;
        if (t == null)
            throw new IllegalStateException(
                    "celestial generator used before its level handed it a seed");
        return t;
    }

    @Override
    public long getSeed() {
        return terrain().seed();
    }

    private BiomeGenBaseCelestial singleBiome() {
        Holder<Biome> biome = biomeSource.possibleBiomes().iterator().next();
        return BiomeGenBaseCelestial.get(biome.unwrapKey().orElseThrow());
    }

    @Override
    public BiomeGenBaseCelestial getBiomeGenForCoords(int x, int z) {
        if (biomeSource instanceof WorldChunkManagerCelestial layered) {
            return BiomeGenBaseCelestial.getBiome(layered.getBiomeGenAt(x, z));
        }
        return singleBiome();
    }

    @Override
    public BiomeGenBaseCelestial[] getBiomesForGeneration(int x, int z, int width, int length) {
        if (biomeSource instanceof WorldChunkManagerCelestial layered) {
            return biomes(layered.getBiomesForGeneration(x, z, width, length));
        }
        return single(width * length);
    }

    @Override
    public BiomeGenBaseCelestial[] loadBlockGeneratorData(int x, int z, int width, int length) {
        if (biomeSource instanceof WorldChunkManagerCelestial layered) {
            return biomes(layered.loadBlockGeneratorData(x, z, width, length));
        }
        return single(width * length);
    }

    private BiomeGenBaseCelestial[] single(int size) {
        BiomeGenBaseCelestial[] biomes = new BiomeGenBaseCelestial[size];
        Arrays.fill(biomes, singleBiome());
        return biomes;
    }

    private static BiomeGenBaseCelestial[] biomes(int[] ids) {
        BiomeGenBaseCelestial[] biomes = new BiomeGenBaseCelestial[ids.length];
        for (int i = 0; i < ids.length; i++) biomes[i] = BiomeGenBaseCelestial.getBiome(ids[i]);
        return biomes;
    }

    public BlockState[] getChunkPrimer(int x, int z) {
        BlockState[] blocks = new BlockState[65536];
        generateBlocks(x, z, blocks);
        BiomeGenBaseCelestial[] biomes = loadBlockGeneratorData(x * 16, z * 16, 16, 16);
        shapeBeforeSurface(x, z, blocks, biomes);
        Random rand = new Random((long) x * 341873128712L + (long) z * 132897987541L ^ getSeed());
        replaceBlocksForBiome(x, z, blocks, biomes, rand);
        return blocks;
    }

    protected void shapeBeforeSurface(
            int x, int z, BlockState[] blocks, BiomeGenBaseCelestial[] biomes) {}

    protected void generateBlocks(int x, int z, BlockState[] blocks) {
        BiomeGenBaseCelestial[] biomesForGeneration =
                getBiomesForGeneration(x * 4 - 2, z * 4 - 2, 10, 10);
        double[] terrainBuffer = generateNoiseField(x * 4, 0, z * 4, biomesForGeneration);
        BlockState stone = stoneBlock.defaultBlockState();
        BlockState sea = seaBlock.defaultBlockState();

        for (int k = 0; k < 4; ++k) {
            int l = k * 5;
            int i1 = (k + 1) * 5;

            for (int j1 = 0; j1 < 4; ++j1) {
                int k1 = (l + j1) * 33;
                int l1 = (l + j1 + 1) * 33;
                int i2 = (i1 + j1) * 33;
                int j2 = (i1 + j1 + 1) * 33;

                for (int k2 = 0; k2 < 32; ++k2) {
                    double d0 = 0.125D;
                    double d1 = terrainBuffer[k1 + k2];
                    double d2 = terrainBuffer[l1 + k2];
                    double d3 = terrainBuffer[i2 + k2];
                    double d4 = terrainBuffer[j2 + k2];
                    double d5 = (terrainBuffer[k1 + k2 + 1] - d1) * d0;
                    double d6 = (terrainBuffer[l1 + k2 + 1] - d2) * d0;
                    double d7 = (terrainBuffer[i2 + k2 + 1] - d3) * d0;
                    double d8 = (terrainBuffer[j2 + k2 + 1] - d4) * d0;

                    for (int l2 = 0; l2 < 8; ++l2) {
                        double d9 = 0.25D;
                        double d10 = d1;
                        double d11 = d2;
                        double d12 = (d3 - d1) * d9;
                        double d13 = (d4 - d2) * d9;

                        for (int i3 = 0; i3 < 4; ++i3) {
                            int j3 = i3 + k * 4 << 12 | j1 * 4 << 8 | k2 * 8 + l2;
                            short short1 = 256;
                            j3 -= short1;
                            double d14 = 0.25D;
                            double d16 = (d11 - d10) * d14;
                            double d15 = d10 - d16;

                            for (int k3 = 0; k3 < 4; ++k3) {
                                if ((d15 += d16) > 0.0D) {
                                    blocks[j3 += short1] = stone;
                                } else if (k2 * 8 + l2 < seaLevel) {
                                    blocks[j3 += short1] = sea;
                                } else {
                                    blocks[j3 += short1] = null;
                                }
                            }

                            d10 += d12;
                            d11 += d13;
                        }

                        d1 += d5;
                        d2 += d6;
                        d3 += d7;
                        d4 += d8;
                    }
                }
            }
        }
    }

    protected double[] generateNoiseField(
            int x, int y, int z, BiomeGenBaseCelestial[] biomesForGeneration) {
        Terrain t = terrain();
        double[] firstOrderBuffer =
                t.firstOrder()
                        .generateNoiseOctaves(
                                null,
                                x,
                                y,
                                z,
                                5,
                                33,
                                5,
                                firstOrderFreq[0],
                                firstOrderFreq[1],
                                firstOrderFreq[2]);
        double[] secondOrderBuffer =
                t.secondOrder()
                        .generateNoiseOctaves(
                                null,
                                x,
                                y,
                                z,
                                5,
                                33,
                                5,
                                secondOrderFreq[0],
                                secondOrderFreq[1],
                                secondOrderFreq[2]);
        double[] thirdOrderBuffer =
                t.thirdOrder()
                        .generateNoiseOctaves(
                                null,
                                x,
                                y,
                                z,
                                5,
                                33,
                                5,
                                thirdOrderFreq[0],
                                thirdOrderFreq[1],
                                thirdOrderFreq[2]);
        double[] heightOrderBuffer =
                t.heightOrder()
                        .generateNoiseOctaves(
                                null,
                                x,
                                z,
                                5,
                                5,
                                heightOrderFreq[0],
                                heightOrderFreq[1],
                                heightOrderFreq[2]);
        double[] terrainBuffer = new double[825];

        int l = 0;
        int i1 = 0;

        for (int j1 = 0; j1 < 5; ++j1) {
            for (int k1 = 0; k1 < 5; ++k1) {
                float f = 0.0F;
                float f1 = 0.0F;
                float f2 = 0.0F;
                byte b0 = 2;
                BiomeGenBaseCelestial biomegenbase = biomesForGeneration[j1 + 2 + (k1 + 2) * 10];

                for (int l1 = -b0; l1 <= b0; ++l1) {
                    for (int i2 = -b0; i2 <= b0; ++i2) {
                        BiomeGenBaseCelestial biomegenbase1 =
                                biomesForGeneration[j1 + l1 + 2 + (k1 + i2 + 2) * 10];
                        float f3 = biomegenbase1.rootHeight;
                        float f4 = biomegenbase1.heightVariation;

                        if (amplified && f3 > 0.0F) {
                            f3 = 1.0F + f3 * 2.0F;
                            f4 = 1.0F + f4 * 4.0F;
                        }

                        float f5 = PARABOLIC_FIELD[l1 + 2 + (i2 + 2) * 5] / (f3 + 2.0F);

                        if (biomegenbase1.rootHeight > biomegenbase.rootHeight) {
                            f5 /= 2.0F;
                        }

                        f += f4 * f5;
                        f1 += f3 * f5;
                        f2 += f5;
                    }
                }

                f /= f2;
                f1 /= f2;
                f = f * 0.9F + 0.1F;
                f1 = (f1 * 4.0F - 1.0F) / 8.0F;
                double d12 = heightOrderBuffer[i1] / 8000.0D;

                if (d12 < 0.0D) {
                    d12 = -d12 * 0.3D;
                }

                d12 = d12 * 3.0D - 2.0D;

                if (d12 < 0.0D) {
                    d12 /= 2.0D;

                    if (d12 < -1.0D) {
                        d12 = -1.0D;
                    }

                    d12 /= 1.4D;
                    d12 /= 2.0D;
                } else {
                    if (d12 > 1.0D) {
                        d12 = 1.0D;
                    }

                    d12 /= 8.0D;
                }

                ++i1;
                double d13 = (double) f1;
                double d14 = (double) f;
                d13 += d12 * 0.2D;
                d13 = d13 * 8.5D / 8.0D;
                double d5 = 8.5D + d13 * 4.0D;

                for (int j2 = 0; j2 < 33; ++j2) {
                    double d6 = ((double) j2 - d5) * 12.0D * 128.0D / 256.0D / d14;

                    if (d6 < 0.0D) {
                        d6 *= 4.0D;
                    }

                    double d7 = firstOrderBuffer[l] / 512.0D;
                    double d8 = secondOrderBuffer[l] / 512.0D;
                    double d9 = (thirdOrderBuffer[l] / 10.0D + 1.0D) / 2.0D;
                    double d10 = reclamp ? LegacyMath.denormalizeClamp(d7, d8, d9) - d6 : d8 - d6;
                    if (j2 > 29) {
                        double d11 = (double) ((float) (j2 - 29) / 3.0F);
                        d10 = d10 * (1.0D - d11) + -10.0D * d11;
                    }

                    terrainBuffer[l] = d10;
                    ++l;
                }
            }
        }

        return terrainBuffer;
    }

    protected void replaceBlocksForBiome(
            int x, int z, BlockState[] blocks, BiomeGenBaseCelestial[] biomes, Random rand) {
        double d0 = 0.03125D;
        double[] stoneNoise =
                terrain()
                        .perlin()
                        .getRegion(
                                null,
                                (double) (x * 16),
                                (double) (z * 16),
                                16,
                                16,
                                d0 * 2.0D,
                                d0 * 2.0D,
                                1.0D);

        for (int k = 0; k < 16; ++k) {
            for (int l = 0; l < 16; ++l) {
                BiomeGenBaseCelestial biomegenbase = biomes[l + k * 16];
                biomegenbase.genTerrainBlocks(
                        this, rand, blocks, x * 16 + k, z * 16 + l, stoneNoise[l + k * 16]);
            }
        }
    }

    @Override
    public CompletableFuture<ChunkAccess> fillFromNoise(
            Blender blender,
            RandomState randomState,
            StructureManager structureManager,
            ChunkAccess chunk) {
        ChunkPos pos = chunk.getPos();
        BlockState[] blocks = getChunkPrimer(pos.x(), pos.z());

        LevelChunkSection[] sections = chunk.getSections();
        for (LevelChunkSection section : sections) section.acquire();
        try {
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    for (int y = 0; y < 256; y++) {
                        BlockState state = blocks[(x * 16 + z) * 256 + y];
                        if (state == null || state.isAir()) continue;
                        chunk.getSection(chunk.getSectionIndex(y))
                                .setBlockState(x, y & 15, z, state, false);
                    }
                }
            }
        } finally {
            for (LevelChunkSection section : sections) section.release();
        }

        Heightmap.primeHeightmaps(
                chunk,
                EnumSet.of(Heightmap.Types.OCEAN_FLOOR_WG, Heightmap.Types.WORLD_SURFACE_WG));
        return CompletableFuture.completedFuture(chunk);
    }

    @Override
    public void buildSurface(
            WorldGenRegion level,
            StructureManager structureManager,
            RandomState randomState,
            ChunkAccess protoChunk) {}

    @Override
    public void applyBiomeDecoration(
            WorldGenLevel level, ChunkAccess chunk, StructureManager structureManager) {
        super.applyBiomeDecoration(level, chunk, structureManager);

        ChunkPos pos = chunk.getPos();
        int k = pos.x() * 16;
        int l = pos.z() * 16;
        long seed = getSeed();
        Random rand = new Random(seed);
        long i1 = rand.nextLong() / 2L * 2L + 1L;
        long j1 = rand.nextLong() / 2L * 2L + 1L;
        rand.setSeed((long) pos.x() * i1 + (long) pos.z() * j1 ^ seed);

        ResourceKey<Level> dimension = level.getLevel().dimension();
        WorldProviderCelestial provider = WorldProviderCelestial.of(level.getLevel());
        LegacyDecorWorld world = new LegacyDecorWorld(level, this);
        getBiomeGenForCoords(k + 16, l + 16)
                .decorate(world, rand, k, l, provider != null && provider.hasLife());
        WorldGeneratorCelestial.generateWorld(dimension, pos.x(), pos.z(), world);
    }

    @Override
    public void applyCarvers(
            WorldGenRegion region,
            long seed,
            RandomState randomState,
            BiomeManager biomeManager,
            StructureManager structureManager,
            ChunkAccess chunk) {}

    @Override
    public void spawnOriginalMobs(WorldGenRegion worldGenRegion) {
        ChunkPos center = worldGenRegion.getCenter();
        Holder<Biome> biome =
                worldGenRegion.getBiome(center.getWorldPosition().atY(worldGenRegion.getMaxY()));
        WorldgenRandom random =
                new WorldgenRandom(new LegacyRandomSource(RandomSupport.generateUniqueSeed()));
        random.setDecorationSeed(
                worldGenRegion.getSeed(), center.getMinBlockX(), center.getMinBlockZ());
        NaturalSpawner.spawnMobsForChunkGeneration(worldGenRegion, biome, center, random);
    }

    @Override
    public int getGenDepth() {
        return 256;
    }

    @Override
    public int getSeaLevel() {
        return seaLevel;
    }

    @Override
    public int getMinY() {
        return 0;
    }

    @Override
    public int getBaseHeight(
            int x,
            int z,
            Heightmap.Types type,
            LevelHeightAccessor heightAccessor,
            RandomState randomState) {
        int height =
                heights.get()
                        .computeIfAbsent(new HeightKey(x >> 4, z >> 4, type), this::primerHeights)[
                        (x & 15) * 16 + (z & 15)];
        return height < 0 ? heightAccessor.getMinY() : height;
    }

    private record HeightKey(int chunkX, int chunkZ, Heightmap.Types type) {}

    private final ThreadLocal<Map<HeightKey, int[]>> heights =
            ThreadLocal.withInitial(
                    () ->
                            new LinkedHashMap<>(16, 0.75F, true) {
                                @Override
                                protected boolean removeEldestEntry(
                                        Map.Entry<HeightKey, int[]> eldest) {
                                    return size() > 16;
                                }
                            });

    private int[] primerHeights(HeightKey key) {
        BlockState[] blocks = getChunkPrimer(key.chunkX(), key.chunkZ());
        int[] heights = new int[256];
        Arrays.fill(heights, -1);
        for (int column = 0; column < 256; column++) {
            for (int y = 255; y >= 0; y--) {
                BlockState state = blocks[column * 256 + y];
                if (state != null && key.type().isOpaque().test(state)) {
                    heights[column] = y + 1;
                    break;
                }
            }
        }
        return heights;
    }

    @Override
    public NoiseColumn getBaseColumn(
            int x, int z, LevelHeightAccessor heightAccessor, RandomState randomState) {
        BlockState[] blocks = getChunkPrimer(x >> 4, z >> 4);
        int column = ((x & 15) * 16 + (z & 15)) * 256;
        BlockState[] states = new BlockState[256];
        for (int y = 0; y < 256; y++) {
            BlockState state = blocks[column + y];
            states[y] = state == null ? Blocks.AIR.defaultBlockState() : state;
        }
        return new NoiseColumn(0, states);
    }

    @Override
    public void addDebugScreenInfo(
            List<String> result, RandomState randomState, BlockPos feetPos) {}
}
