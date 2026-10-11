// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-FileCopyrightText: 2024-2026 George Paton
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.dim.minmus;

import com.hbmspace.blocks.ModBlocksSpace;
import com.hbmspace.blocks.ore.CelestialOre;
import com.hbmspace.data.SpaceData;
import com.hbmspace.dim.ChunkProviderCelestial;
import com.hbmspace.dim.SolarSystem.Body;
import com.hbmspace.dim.mapgen.MapGenCrater;
import com.hbmspace.dim.mapgen.MapGenVanillaCaves;
import com.hbmspace.world.gen.terrain.MapGenBubble;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;

public class ChunkProviderMinmus extends ChunkProviderCelestial {

    public static final MapCodec<ChunkProviderMinmus> CODEC =
            RecordCodecBuilder.mapCodec(
                    i ->
                            i.group(
                                            BiomeSource.CODEC
                                                    .fieldOf("biome_source")
                                                    .forGetter(ChunkGenerator::getBiomeSource))
                                    .apply(i, i.stable(ChunkProviderMinmus::new)));

    private final Block minmusStone;
    private final Block minmusSmooth;
    private final Block minmusRegolith;

    public ChunkProviderMinmus(BiomeSource biomeSource) {
        super(biomeSource);
        this.minmusStone = ModBlocksSpace.MINMUS_STONE.get();
        this.minmusSmooth = ModBlocksSpace.MINMUS_SMOOTH.get();
        this.minmusRegolith = ModBlocksSpace.MINMUS_REGOLITH.get();

        stoneBlock = minmusStone;
        seaBlock = minmusSmooth;
        seaLevel = 63;
    }

    @Override
    protected MapCodec<? extends ChunkGenerator> codec() {
        return CODEC;
    }

    @Override
    public BlockState[] getChunkPrimer(int x, int z) {
        BlockState[] buffer = super.getChunkPrimer(x, z);

        MapGenVanillaCaves caveGenerator =
                new MapGenVanillaCaves(minmusStone).withLava(minmusSmooth);
        MapGenCrater smallCrater = new MapGenCrater(5);
        MapGenCrater largeCrater = new MapGenCrater(96);
        smallCrater.setSize(6, 24);
        largeCrater.setSize(64, 128);
        smallCrater.regolith = largeCrater.regolith = minmusRegolith;
        smallCrater.rock = largeCrater.rock = minmusStone;
        MapGenBubble brine = new MapGenBubble(SpaceData.MINMUS_BRINE_SPAWN.get());
        brine.block = CelestialOre.BRINE.state(Body.MINMUS);
        brine.replace = minmusStone;
        brine.setSize(8, 16);

        caveGenerator.generate(this, x, z, buffer);
        smallCrater.generate(this, x, z, buffer);
        largeCrater.generate(this, x, z, buffer);
        brine.generate(this, x, z, buffer);

        return buffer;
    }
}
