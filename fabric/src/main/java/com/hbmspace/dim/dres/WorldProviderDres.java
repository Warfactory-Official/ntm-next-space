// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-FileCopyrightText: 2024-2025 George Paton
// SPDX-FileCopyrightText: 2023 Pu-238
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.dim.dres;

import com.hbm.inventory.fluid.FluidStackNTM;
import com.hbm.inventory.fluid.NTMFluids;
import com.hbmspace.blocks.ModBlocksSpace;
import com.hbmspace.dim.BiomeGenBaseCelestial;
import com.hbmspace.dim.GenLayerBorder;
import com.hbmspace.dim.GenLayerCelestialBiomes;
import com.hbmspace.dim.GenLayerDiversify;
import com.hbmspace.dim.SpaceDimensions;
import com.hbmspace.dim.WorldChunkManagerCelestial.BiomeGenLayers;
import com.hbmspace.dim.WorldProviderCelestial;
import com.hbmspace.dim.dres.biome.BiomeGenBaseDres;
import com.hbmspace.dim.legacy.layer.GenLayer;
import com.hbmspace.dim.legacy.layer.GenLayerFuzzyZoom;
import com.hbmspace.dim.legacy.layer.GenLayerSmooth;
import com.hbmspace.dim.legacy.layer.GenLayerVoronoiZoom;
import com.hbmspace.dim.legacy.layer.GenLayerZoom;
import net.minecraft.world.level.block.state.BlockState;

public class WorldProviderDres extends WorldProviderCelestial {

    public WorldProviderDres() {
        super(SpaceDimensions.DRES);
    }

    @Override
    public BlockState getStone() {
        return ModBlocksSpace.DRES_ROCK.get().defaultBlockState();
    }

    @Override
    public FluidStackNTM getBedrockAcid() {
        return new FluidStackNTM(NTMFluids.NITRIC_ACID, 500);
    }

    public static BiomeGenLayers createBiomeGenerators(long seed) {
        BiomeGenBaseCelestial canyon = BiomeGenBaseDres.dresCanyon;
        BiomeGenBaseCelestial plains = BiomeGenBaseDres.dresPlains;
        GenLayer biomes = new GenLayerCelestialBiomes(seed, canyon, plains);

        biomes = new GenLayerFuzzyZoom(2000L, biomes);
        biomes = new GenLayerZoom(2001L, biomes);
        biomes = new GenLayerDiversify(1000L, biomes, 2, canyon, plains);
        biomes = new GenLayerZoom(1000L, biomes);
        biomes = new GenLayerDiversify(1001L, biomes, 2, canyon, plains);
        biomes = new GenLayerZoom(1001L, biomes);
        biomes = new GenLayerBorder(3000L, biomes, canyon, plains, canyon);
        biomes = new GenLayerZoom(1003L, biomes);
        biomes = new GenLayerSmooth(700L, biomes);
        biomes = new GenLayerBorder(200L, biomes, plains, canyon, plains);

        biomes = new GenLayerZoom(1006L, biomes);

        GenLayer genLayerVoronoiZoom = new GenLayerVoronoiZoom(10L, biomes);

        return new BiomeGenLayers(biomes, genLayerVoronoiZoom, seed);
    }
}
