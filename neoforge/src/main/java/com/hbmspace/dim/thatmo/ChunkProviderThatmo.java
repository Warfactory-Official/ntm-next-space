// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.dim.thatmo;

import com.hbm.blocks.ModBlocks;
import com.hbm.lib.Library;
import com.hbmspace.dim.ChunkProviderCelestial;
import com.hbmspace.dim.mapgen.MapGenCrater;
import com.hbmspace.dim.mapgen.MapGenGreg;
import com.hbmspace.dim.mapgen.MapgenRavineButBased;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;

public class ChunkProviderThatmo extends ChunkProviderCelestial {

    public static final MapCodec<ChunkProviderThatmo> CODEC =
            RecordCodecBuilder.mapCodec(
                    i ->
                            i.group(
                                            BiomeSource.CODEC
                                                    .fieldOf("biome_source")
                                                    .forGetter(ChunkGenerator::getBiomeSource))
                                    .apply(i, i.stable(ChunkProviderThatmo::new)));

    private final Block slaked;
    private MapGenGreg.Noise caveNoise;

    public ChunkProviderThatmo(BiomeSource biomeSource) {
        super(biomeSource);
        this.slaked = ModBlocks.SELLAFIELD_SLAKED.get();
        Block basalt = BuiltInRegistries.BLOCK.getOptional(Library.id("basalt")).orElseThrow();

        stoneBlock = basalt;
        seaBlock = basalt;
        seaLevel = 64;
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

        MapGenGreg caveGenV3 = new MapGenGreg(caveNoise);
        caveGenV3.stoneBlock = slaked;
        MapgenRavineButBased rgen = new MapgenRavineButBased();
        rgen.stoneBlock = slaked;

        MapGenCrater smallCrater = new MapGenCrater(6);
        smallCrater.setSize(8, 32);
        smallCrater.regolith = slaked;
        smallCrater.rock = slaked;

        caveGenV3.generate(this, x, z, buffer);
        rgen.generate(this, x, z, buffer);
        smallCrater.generate(this, x, z, buffer);

        return buffer;
    }
}
