// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.dim.moon;

import com.hbm.lib.Library;
import com.hbmspace.blocks.ModBlocksSpace;
import com.hbmspace.blocks.ore.CelestialOre;
import com.hbmspace.data.SpaceData;
import com.hbmspace.dim.ChunkProviderCelestial;
import com.hbmspace.dim.SolarSystem.Body;
import com.hbmspace.dim.mapgen.MapGenCrater;
import com.hbmspace.dim.mapgen.MapGenGreg;
import com.hbmspace.dim.mapgen.MapgenRavineButBased;
import com.hbmspace.world.gen.terrain.MapGenBubble;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;

public class ChunkProviderMoon extends ChunkProviderCelestial {

    public static final MapCodec<ChunkProviderMoon> CODEC =
            RecordCodecBuilder.mapCodec(
                    i ->
                            i.group(
                                            BiomeSource.CODEC
                                                    .fieldOf("biome_source")
                                                    .forGetter(ChunkGenerator::getBiomeSource))
                                    .apply(i, i.stable(ChunkProviderMoon::new)));

    private final Block moonRock;
    private final Block basalt;
    private MapGenGreg.Noise caveNoise;

    public ChunkProviderMoon(BiomeSource biomeSource) {
        super(biomeSource);
        this.moonRock = ModBlocksSpace.MOON_ROCK.get();
        this.basalt = BuiltInRegistries.BLOCK.getOptional(Library.id("basalt")).orElseThrow();

        stoneBlock = moonRock;
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
        caveGenV3.stoneBlock = moonRock;
        MapgenRavineButBased rgen = new MapgenRavineButBased();
        rgen.stoneBlock = moonRock;

        MapGenCrater smallCrater = new MapGenCrater(6);
        MapGenCrater largeCrater = new MapGenCrater(64);
        smallCrater.setSize(8, 32);
        largeCrater.setSize(96, 128);
        smallCrater.regolith = largeCrater.regolith = basalt;
        smallCrater.rock = largeCrater.rock = moonRock;
        MapGenBubble brine = new MapGenBubble(SpaceData.MUN_BRINE_SPAWN.get());
        brine.block = CelestialOre.BRINE.state(Body.MUN);
        brine.replace = moonRock;
        brine.setSize(8, 16);

        caveGenV3.generate(this, x, z, buffer);
        rgen.generate(this, x, z, buffer);
        smallCrater.generate(this, x, z, buffer);
        largeCrater.generate(this, x, z, buffer);
        brine.generate(this, x, z, buffer);

        return buffer;
    }
}
