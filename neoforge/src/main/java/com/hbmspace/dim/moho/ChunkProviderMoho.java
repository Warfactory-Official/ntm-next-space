// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-FileCopyrightText: 2024-2026 George Paton
// SPDX-FileCopyrightText: 2025 JamesH_2
// SPDX-FileCopyrightText: 2023 Pu-238
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.dim.moho;

import com.hbm.blocks.ModBlocks;
import com.hbm.lib.Library;
import com.hbmspace.blocks.ModBlocksSpace;
import com.hbmspace.dim.BiomeGenBaseCelestial;
import com.hbmspace.dim.ChunkProviderCelestial;
import com.hbmspace.dim.mapgen.ExperimentalCaveGenerator;
import com.hbmspace.dim.mapgen.MapGenCrater;
import com.hbmspace.dim.mapgen.MapGenPlateau;
import com.hbmspace.dim.mapgen.MapGenVolcano;
import com.hbmspace.dim.mapgen.MapgenRavineButBased;
import com.hbmspace.dim.moho.biome.BiomeGenBaseMoho;
import com.hbmspace.dim.noise.MapGenVNoise;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;

public class ChunkProviderMoho extends ChunkProviderCelestial {

    public static final MapCodec<ChunkProviderMoho> CODEC =
            RecordCodecBuilder.mapCodec(
                    i ->
                            i.group(
                                            BiomeSource.CODEC
                                                    .fieldOf("biome_source")
                                                    .forGetter(ChunkGenerator::getBiomeSource))
                                    .apply(i, i.stable(ChunkProviderMoho::new)));

    private final Block mohoStone;
    private final Block mohoRegolith;
    private final Block basalt;
    private MapGenPlateau plateau;

    public ChunkProviderMoho(BiomeSource biomeSource) {
        super(biomeSource);
        this.mohoStone = ModBlocksSpace.MOHO_STONE.get();
        this.mohoRegolith = ModBlocksSpace.MOHO_REGOLITH.get();
        this.basalt = BuiltInRegistries.BLOCK.getOptional(Library.id("basalt")).orElseThrow();

        stoneBlock = mohoStone;
        seaBlock = Blocks.LAVA;
    }

    @Override
    protected void onSeed(long seed) {
        plateau = new MapGenPlateau(seed);
        plateau.surfrock = mohoRegolith;
        plateau.stoneBlock = mohoStone;
        plateau.fillblock = Blocks.LAVA;
        plateau.maxPlateauAddition = 6;
        plateau.stepHeight = 2;
        plateau.noiseScale = 0.03;
        plateau.applyToBiome = BiomeGenBaseMoho.mohoPlateau;
    }

    @Override
    protected MapCodec<? extends ChunkGenerator> codec() {
        return CODEC;
    }

    @Override
    public BlockState[] getChunkPrimer(int x, int z) {
        BlockState[] buffer = super.getChunkPrimer(x, z);

        boolean hasLavaSea = false;
        boolean hasPlateau = false;

        for (BiomeGenBaseCelestial biome : loadBlockGeneratorData(x * 16, z * 16, 16, 16)) {
            if (biome == BiomeGenBaseMoho.mohoLavaSea) hasLavaSea = true;
            if (biome == BiomeGenBaseMoho.mohoPlateau) hasPlateau = true;
            if (hasLavaSea && hasPlateau) break;
        }

        if (hasLavaSea) {
            MapGenVNoise noise = new MapGenVNoise();
            noise.surfBlock = mohoStone;
            noise.rockBlock = mohoStone;
            noise.fluidBlock = Blocks.LAVA;
            noise.crackSize = 0.5;
            noise.cellSize = 27;
            noise.plateStartY = 62;
            noise.plateThickness = 25;
            noise.applyToBiome = BiomeGenBaseMoho.mohoLavaSea;
            noise.generate(this, x, z, buffer);
        }

        if (hasPlateau) plateau.generate(this, x, z, buffer);

        ExperimentalCaveGenerator caveGenV2 = new ExperimentalCaveGenerator(1, 52, 5.0F);
        caveGenV2.stoneBlock = mohoStone;
        MapgenRavineButBased rgen = new MapgenRavineButBased();
        rgen.stoneBlock = mohoStone;
        MapGenCrater smallCrater = new MapGenCrater(6);
        MapGenCrater largeCrater = new MapGenCrater(64);
        smallCrater.setSize(8, 32);
        largeCrater.setSize(96, 128);
        smallCrater.regolith = largeCrater.regolith = mohoRegolith;
        smallCrater.rock = largeCrater.rock = mohoStone;
        MapGenVolcano volcano = new MapGenVolcano(72);
        volcano.setSize(64, 128);
        volcano.setMaterial(ModBlocks.VOLCANO_CORE.get(), basalt);

        caveGenV2.generate(this, x, z, buffer);
        rgen.generate(this, x, z, buffer);
        smallCrater.generate(this, x, z, buffer);
        largeCrater.generate(this, x, z, buffer);
        volcano.generate(this, x, z, buffer);
        return buffer;
    }
}
