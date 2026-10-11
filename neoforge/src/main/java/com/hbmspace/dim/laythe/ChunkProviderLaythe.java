// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-FileCopyrightText: 2024-2026 George Paton
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.dim.laythe;

import com.hbmspace.blocks.TerrainBlocksSpace;
import com.hbmspace.blocks.ore.CelestialOre;
import com.hbmspace.data.SpaceData;
import com.hbmspace.dim.ChunkProviderCelestial;
import com.hbmspace.dim.SolarSystem.Body;
import com.hbmspace.dim.laythe.biome.BiomeGenBaseLaythe;
import com.hbmspace.dim.mapgen.MapGenGreg;
import com.hbmspace.dim.mapgen.MapGenRoots;
import com.hbmspace.dim.mapgen.MapGenTiltedSpires;
import com.hbmspace.dim.mapgen.MapgenRavineButBased;
import com.hbmspace.dim.noise.DoublePerlinNoiseSampler;
import com.hbmspace.world.gen.terrain.MapGenBubble;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;

public class ChunkProviderLaythe extends ChunkProviderCelestial {

    public static final MapCodec<ChunkProviderLaythe> CODEC =
            RecordCodecBuilder.mapCodec(
                    i ->
                            i.group(
                                            BiomeSource.CODEC
                                                    .fieldOf("biome_source")
                                                    .forGetter(ChunkGenerator::getBiomeSource))
                                    .apply(i, i.stable(ChunkProviderLaythe::new)));

    private DoublePerlinNoiseSampler spireNoise;
    private MapGenGreg.Noise caveNoise;

    public ChunkProviderLaythe(BiomeSource biomeSource) {
        super(biomeSource);
        seaBlock = Blocks.WATER;
    }

    @Override
    protected void onSeed(long seed) {
        spireNoise = MapGenTiltedSpires.noise(seed);
        caveNoise = new MapGenGreg.Noise(seed);
    }

    @Override
    protected MapCodec<? extends ChunkGenerator> codec() {
        return CODEC;
    }

    @Override
    public BlockState[] getChunkPrimer(int x, int z) {
        BlockState[] buffer = super.getChunkPrimer(x, z);

        MapgenRavineButBased rgen = new MapgenRavineButBased();
        rgen.yMin = 32;
        rgen.frequency = 20;
        rgen.strataFreq = 3;
        rgen.strataScale = 8.0F;
        rgen.width = 16.0D;
        rgen.taper = 8.0D;
        rgen.allowUnderwater = true;
        rgen.height = 5.0D;

        MapGenTiltedSpires spires = new MapGenTiltedSpires(spireNoise, 2, 14, 0.75F);
        MapGenTiltedSpires snowires = new MapGenTiltedSpires(spireNoise, 2, 14, 0.75F);
        snowires.rock = Blocks.PACKED_ICE;
        snowires.regolith = Blocks.SNOW_BLOCK;
        spires.rock = Blocks.STONE;
        spires.regolith = TerrainBlocksSpace.LAYTHE_SILT.get();
        spires.curve = snowires.curve = true;
        spires.maxPoint = snowires.maxPoint = 6.0F;
        spires.maxTilt = snowires.maxTilt = 3.5F;

        MapGenBubble oil = new MapGenBubble(SpaceData.LAYTHE_OIL_SPAWN.get());
        oil.block = CelestialOre.OIL.state(Body.LAYTHE);
        oil.replace = Blocks.STONE;
        oil.setSize(8, 16);

        if (loadBlockGeneratorData(x * 16, z * 16, 16, 16)[0] == BiomeGenBaseLaythe.laythePolar) {
            snowires.generate(this, x, z, buffer);
        } else {
            spires.generate(this, x, z, buffer);
        }
        new MapGenRoots().generate(this, x, z, buffer);
        new MapGenGreg(caveNoise).generate(this, x, z, buffer);
        rgen.generate(this, x, z, buffer);
        oil.generate(this, x, z, buffer);

        return buffer;
    }
}
