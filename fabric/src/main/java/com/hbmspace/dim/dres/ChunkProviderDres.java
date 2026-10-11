// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.dim.dres;

import com.hbmspace.blocks.ModBlocksSpace;
import com.hbmspace.dim.ChunkProviderCelestial;
import com.hbmspace.dim.mapgen.MapGenCrater;
import com.hbmspace.dim.mapgen.MapGenVanillaCaves;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;

public class ChunkProviderDres extends ChunkProviderCelestial {

    public static final MapCodec<ChunkProviderDres> CODEC =
            RecordCodecBuilder.mapCodec(
                    i ->
                            i.group(
                                            BiomeSource.CODEC
                                                    .fieldOf("biome_source")
                                                    .forGetter(ChunkGenerator::getBiomeSource))
                                    .apply(i, i.stable(ChunkProviderDres::new)));

    private final Block dresRock;

    public ChunkProviderDres(BiomeSource biomeSource) {
        super(biomeSource);
        this.dresRock = ModBlocksSpace.DRES_ROCK.get();

        stoneBlock = dresRock;
    }

    @Override
    protected MapCodec<? extends ChunkGenerator> codec() {
        return CODEC;
    }

    @Override
    public BlockState[] getChunkPrimer(int x, int z) {
        BlockState[] buffer = super.getChunkPrimer(x, z);

        MapGenVanillaCaves caveGenerator = new MapGenVanillaCaves(dresRock);
        MapGenCrater smallCrater = new MapGenCrater(6);
        MapGenCrater largeCrater = new MapGenCrater(64);
        smallCrater.setSize(8, 32);
        largeCrater.setSize(96, 128);
        smallCrater.regolith = largeCrater.regolith = dresRock;
        smallCrater.rock = largeCrater.rock = dresRock;

        caveGenerator.generate(this, x, z, buffer);
        smallCrater.generate(this, x, z, buffer);
        largeCrater.generate(this, x, z, buffer);

        return buffer;
    }
}
