// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-FileCopyrightText: 2025 George Paton
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.dim.eve;

import com.hbmspace.blocks.TerrainBlocksSpace;
import com.hbmspace.blocks.ore.CelestialOre;
import com.hbmspace.data.SpaceData;
import com.hbmspace.dim.BiomeGenBaseCelestial;
import com.hbmspace.dim.ChunkProviderCelestial;
import com.hbmspace.dim.SolarSystem.Body;
import com.hbmspace.dim.eve.biome.BiomeGenBaseEve;
import com.hbmspace.dim.legacy.NoiseGeneratorPerlin;
import com.hbmspace.dim.noise.MapGenVNoise;
import com.hbmspace.world.gen.terrain.MapGenBubble;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Random;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;

public class ChunkProviderEve extends ChunkProviderCelestial {

    public static final MapCodec<ChunkProviderEve> CODEC =
            RecordCodecBuilder.mapCodec(
                    i ->
                            i.group(
                                            BiomeSource.CODEC
                                                    .fieldOf("biome_source")
                                                    .forGetter(ChunkGenerator::getBiomeSource))
                                    .apply(i, i.stable(ChunkProviderEve::new)));

    private final Block eveRock;
    private final Block eveSilt;
    private final Block mercury;
    private NoiseGeneratorPerlin crackNoise;

    public ChunkProviderEve(BiomeSource biomeSource) {
        super(biomeSource);
        this.eveRock = TerrainBlocksSpace.EVE_ROCK.get();
        this.eveSilt = TerrainBlocksSpace.EVE_SILT.get();
        this.mercury = TerrainBlocksSpace.MERCURY_BLOCK.get();

        reclamp = false;
        stoneBlock = eveRock;
        seaBlock = mercury;
    }

    @Override
    protected void onSeed(long seed) {
        crackNoise = new NoiseGeneratorPerlin(new Random(seed), 4);
    }

    @Override
    protected MapCodec<? extends ChunkGenerator> codec() {
        return CODEC;
    }

    @Override
    public BlockState[] getChunkPrimer(int x, int z) {
        BlockState[] buffer = super.getChunkPrimer(x, z);
        BiomeGenBaseCelestial[] biomes = loadBlockGeneratorData(x * 16, z * 16, 16, 16);

        boolean hasOcean = false;
        boolean hasSeismic = false;

        for (BiomeGenBaseCelestial biome : biomes) {
            if (biome == BiomeGenBaseEve.eveOcean) hasOcean = true;
            if (biome == BiomeGenBaseEve.eveSeismicPlains) hasSeismic = true;
            if (hasOcean && hasSeismic) break;
        }

        if (hasSeismic) generateCracks(x, z, buffer, biomes);
        if (hasOcean) {
            MapGenVNoise noise = new MapGenVNoise();
            noise.fluidBlock = mercury;
            noise.rockBlock = eveRock;
            noise.surfBlock = eveSilt;
            noise.cellSize = 72;
            noise.crackSize = 2.0;
            noise.plateThickness = 35;
            noise.shapeExponent = 2.0;
            noise.plateStartY = 57;
            noise.applyToBiome = BiomeGenBaseEve.eveOcean;
            noise.generate(this, x, z, buffer);
        }

        MapGenBubble gas = new MapGenBubble(SpaceData.EVE_GAS_SPAWN.get());
        gas.block = CelestialOre.GAS.state(Body.EVE);
        gas.replace = eveRock;
        gas.setSize(8, 16);
        gas.generate(this, x, z, buffer);

        return buffer;
    }

    private void generateCracks(
            int chunkX, int chunkZ, BlockState[] buffer, BiomeGenBaseCelestial[] biomes) {
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                if (biomes[x + z * 16] != BiomeGenBaseEve.eveSeismicPlains) continue;

                double crackValue =
                        crackNoise.getValue((chunkX * 16 + x) * 0.3, (chunkZ * 16 + z) * 0.3);

                if (crackValue > 0.9) {
                    int bedrockY = -1;
                    for (int y = 0; y < 256; y++) {
                        int index = (x * 16 + z) * 256 + y;
                        if (buffer[index] != null && buffer[index].is(Blocks.BEDROCK)) {
                            if (bedrockY == -1) bedrockY = y;
                        } else {
                            buffer[index] = null;
                        }
                    }
                    if (bedrockY != -1) {
                        for (int y = bedrockY + 1; y < Math.min(bedrockY + 10, 256); y++) {
                            buffer[(x * 16 + z) * 256 + y] = Blocks.LAVA.defaultBlockState();
                        }
                    }
                }
            }
        }
    }
}
