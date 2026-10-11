// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.dim.ike;

import com.hbm.blocks.ModBlocks;
import com.hbmspace.blocks.ModBlocksSpace;
import com.hbmspace.blocks.TerrainBlocksSpace;
import com.hbmspace.blocks.ore.CelestialOre;
import com.hbmspace.data.SpaceData;
import com.hbmspace.dim.ChunkProviderCelestial;
import com.hbmspace.dim.SolarSystem.Body;
import com.hbmspace.dim.legacy.MapGenCaves;
import com.hbmspace.dim.mapgen.MapGenTiltedSpires;
import com.hbmspace.dim.noise.DoublePerlinNoiseSampler;
import com.hbmspace.world.gen.terrain.MapGenBubble;
import com.hbmspace.world.gen.terrain.MapGenCrater;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;

public class ChunkProviderIke extends ChunkProviderCelestial {

    public static final MapCodec<ChunkProviderIke> CODEC =
            RecordCodecBuilder.mapCodec(
                    i ->
                            i.group(
                                            BiomeSource.CODEC
                                                    .fieldOf("biome_source")
                                                    .forGetter(ChunkGenerator::getBiomeSource))
                                    .apply(i, i.stable(ChunkProviderIke::new)));

    private final Block ikeStone;
    private final Block ikeRegolith;
    private DoublePerlinNoiseSampler spireNoise;

    public ChunkProviderIke(BiomeSource biomeSource) {
        super(biomeSource);
        this.ikeStone = ModBlocksSpace.IKE_STONE.get();
        this.ikeRegolith = TerrainBlocksSpace.IKE_REGOLITH.get();

        stoneBlock = ikeStone;
    }

    @Override
    protected void onSeed(long seed) {
        spireNoise = MapGenTiltedSpires.noise(seed);
    }

    @Override
    protected MapCodec<? extends ChunkGenerator> codec() {
        return CODEC;
    }

    @Override
    public BlockState[] getChunkPrimer(int x, int z) {
        BlockState[] buffer = super.getChunkPrimer(x, z);

        MapGenTiltedSpires spires = new MapGenTiltedSpires(spireNoise, 6, 6, 0F);
        spires.rock = ikeStone;
        spires.regolith = ikeRegolith;
        spires.mid = 86;

        MapGenCrater sellafield = new MapGenCrater(500);
        sellafield.regolith = ModBlocks.SELLAFIELD.get();
        sellafield.rock = ModBlocks.SELLAFIELD_SLAKED.get();
        MapGenBubble brine = new MapGenBubble(SpaceData.IKE_BRINE_SPAWN.get());
        brine.block = CelestialOre.BRINE.state(Body.IKE);
        brine.replace = ikeStone;
        brine.setSize(8, 16);

        spires.generate(this, x, z, buffer);
        new MapGenCaves().generate(this, x, z, buffer);
        brine.generate(this, x, z, buffer);
        sellafield.generate(this, x, z, buffer);

        return buffer;
    }

    @Override
    public WeightedList<MobSpawnSettings.SpawnerData> getMobsAt(
            Holder<Biome> biome,
            StructureManager structureManager,
            MobCategory mobCategory,
            BlockPos pos) {
        return WeightedList.of();
    }
}
