// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.dim.tekto;

import com.hbm.lib.Library;
import com.hbmspace.blocks.TerrainBlocksSpace;
import com.hbmspace.blocks.ore.CelestialOre;
import com.hbmspace.data.SpaceData;
import com.hbmspace.dim.ChunkProviderCelestial;
import com.hbmspace.dim.SolarSystem.Body;
import com.hbmspace.dim.mapgen.MapGenGreg;
import com.hbmspace.dim.mapgen.MapGenVolcano;
import com.hbmspace.dim.tekto.biome.BiomeGenBaseTekto;
import com.hbmspace.world.gen.terrain.MapGenBedrockOil;
import com.hbmspace.world.gen.terrain.MapGenBubble;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;

public class ChunkProviderTekto extends ChunkProviderCelestial {

    public static final MapCodec<ChunkProviderTekto> CODEC =
            RecordCodecBuilder.mapCodec(
                    i ->
                            i.group(
                                            BiomeSource.CODEC
                                                    .fieldOf("biome_source")
                                                    .forGetter(ChunkGenerator::getBiomeSource))
                                    .apply(i, i.stable(ChunkProviderTekto::new)));

    private final Block basalt;
    private MapGenGreg.Noise caveNoise;

    public ChunkProviderTekto(BiomeSource biomeSource) {
        super(biomeSource);
        this.basalt = BuiltInRegistries.BLOCK.getOptional(Library.id("basalt")).orElseThrow();

        reclamp = false;
        stoneBlock = basalt;
        seaBlock = TerrainBlocksSpace.CCL_BLOCK.get();
    }

    @Override
    protected void onSeed(long seed) {
        caveNoise = new MapGenGreg.Noise(seed);
    }

    @Override
    protected MapCodec<? extends ChunkGenerator> codec() {
        return CODEC;
    }

    @Override
    public BlockState[] getChunkPrimer(int x, int z) {
        BlockState[] buffer = super.getChunkPrimer(x, z);

        if (loadBlockGeneratorData(x * 16, z * 16, 16, 16)[0] == BiomeGenBaseTekto.vinylsands) {
            MapGenVolcano volcano = new MapGenVolcano(12);
            volcano.setSize(8, 16);
            volcano.setMaterial(
                    TerrainBlocksSpace.GEYSIR_CHLORIC.get(), TerrainBlocksSpace.VINYL_SAND.get());
            volcano.generate(this, x, z, buffer);
        }

        MapGenGreg caveGenV3 = new MapGenGreg(caveNoise);
        caveGenV3.stoneBlock = basalt;
        caveGenV3.generate(this, x, z, buffer);

        MapGenBubble tektonic = new MapGenBubble(SpaceData.TEKTO_OIL_SPAWN.get());
        tektonic.block = CelestialOre.TEKTO.state(Body.TEKTO);
        tektonic.replace = basalt;
        tektonic.setSize(8, 16);
        tektonic.generate(this, x, z, buffer);

        MapGenBedrockOil bedtonic = new MapGenBedrockOil(SpaceData.TEKTO_BEDROCK_OIL_SPAWN.get());
        bedtonic.block = CelestialOre.BEDROCK_OIL.state(Body.TEKTO);
        bedtonic.replace = basalt;
        bedtonic.generate(this, x, z, buffer);

        return buffer;
    }
}
