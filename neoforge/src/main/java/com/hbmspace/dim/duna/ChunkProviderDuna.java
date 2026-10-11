// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.dim.duna;

import com.hbm.lib.Library;
import com.hbmspace.blocks.ModBlocksSpace;
import com.hbmspace.blocks.TerrainBlocksSpace;
import com.hbmspace.blocks.ore.CelestialOre;
import com.hbmspace.data.SpaceData;
import com.hbmspace.dim.BiomeGenBaseCelestial;
import com.hbmspace.dim.ChunkProviderCelestial;
import com.hbmspace.dim.SolarSystem.Body;
import com.hbmspace.dim.duna.biome.BiomeGenBaseDuna;
import com.hbmspace.dim.mapgen.ExperimentalCaveGenerator;
import com.hbmspace.dim.mapgen.MapGenPlateau;
import com.hbmspace.world.gen.terrain.MapGenBubble;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;

public class ChunkProviderDuna extends ChunkProviderCelestial {

    public static final MapCodec<ChunkProviderDuna> CODEC =
            RecordCodecBuilder.mapCodec(
                    i ->
                            i.group(
                                            BiomeSource.CODEC
                                                    .fieldOf("biome_source")
                                                    .forGetter(ChunkGenerator::getBiomeSource))
                                    .apply(i, i.stable(ChunkProviderDuna::new)));

    private final Block dunaRock;
    private final Block dunaCobble;
    private final Block dunaSands;
    private final Block basalt;
    private MapGenPlateau plateau;

    public ChunkProviderDuna(BiomeSource biomeSource) {
        super(biomeSource);
        this.dunaRock = TerrainBlocksSpace.DUNA_ROCK.get();
        this.dunaCobble = ModBlocksSpace.DUNA_COBBLE.get();
        this.dunaSands = ModBlocksSpace.DUNA_SANDS.get();
        this.basalt = BuiltInRegistries.BLOCK.getOptional(Library.id("basalt")).orElseThrow();

        stoneBlock = dunaRock;
    }

    @Override
    protected void onSeed(long seed) {
        plateau = new MapGenPlateau(seed);
        plateau.surfrock = dunaSands;
        plateau.stoneBlock = dunaRock;
        plateau.fillblock = dunaSands;
        plateau.applyToBiome = BiomeGenBaseDuna.dunaHills;
    }

    @Override
    protected MapCodec<? extends ChunkGenerator> codec() {
        return CODEC;
    }

    @Override
    protected void shapeBeforeSurface(
            int x, int z, BlockState[] blocks, BiomeGenBaseCelestial[] biomes) {
        for (BiomeGenBaseCelestial biome : biomes) {
            if (biome == BiomeGenBaseDuna.dunaHills) {
                plateau.generate(this, x, z, blocks);
                return;
            }
        }
    }

    @Override
    public BlockState[] getChunkPrimer(int x, int z) {
        BlockState[] buffer = super.getChunkPrimer(x, z);

        boolean hasLowlands = false;
        boolean hasNotLowlands = false;

        for (BiomeGenBaseCelestial biome : loadBlockGeneratorData(x * 16, z * 16, 16, 16)) {
            if (biome == BiomeGenBaseDuna.dunaLowlands) hasLowlands = true;
            else hasNotLowlands = true;
            if (hasLowlands && hasNotLowlands) break;
        }

        if (hasLowlands) {
            ExperimentalCaveGenerator caveGenV2 = new ExperimentalCaveGenerator(2, 40, 3.0F);
            caveGenV2.lavaBlock = basalt;
            caveGenV2.stoneBlock = dunaCobble;
            caveGenV2.onlyBiome = BiomeGenBaseDuna.dunaLowlands;
            caveGenV2.generate(this, x, z, buffer);
        }
        if (hasNotLowlands) {
            ExperimentalCaveGenerator caveGenSmall = new ExperimentalCaveGenerator(2, 12, 0.12F);
            caveGenSmall.lavaBlock = dunaSands;
            caveGenSmall.stoneBlock = dunaRock;
            caveGenSmall.smallCaveSize = 0.1F;
            caveGenSmall.ignoreBiome = BiomeGenBaseDuna.dunaLowlands;
            caveGenSmall.generate(this, x, z, buffer);
        }

        MapGenBubble oil = new MapGenBubble(SpaceData.DUNA_OIL_SPAWN.get());
        oil.block = CelestialOre.OIL.state(Body.DUNA);
        oil.replace = dunaRock;
        oil.setSize(8, 16);
        oil.generate(this, x, z, buffer);

        return buffer;
    }
}
