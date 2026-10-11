// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-FileCopyrightText: 2024 George Paton
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.dim.tekto;

import com.hbm.lib.Library;
import com.hbmspace.dim.GenLayerCelestialBiomes;
import com.hbmspace.dim.SpaceDimensions;
import com.hbmspace.dim.WorldChunkManagerCelestial.BiomeGenLayers;
import com.hbmspace.dim.WorldProviderCelestial;
import com.hbmspace.dim.legacy.layer.GenLayer;
import com.hbmspace.dim.legacy.layer.GenLayerRiver;
import com.hbmspace.dim.legacy.layer.GenLayerSmooth;
import com.hbmspace.dim.legacy.layer.GenLayerVoronoiZoom;
import com.hbmspace.dim.legacy.layer.GenLayerZoom;
import com.hbmspace.dim.tekto.biome.BiomeGenBaseTekto;
import com.hbmspace.dim.tekto.genlayer.GenLayerTektoRiverMix;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.state.BlockState;

public class WorldProviderTekto extends WorldProviderCelestial {

    public WorldProviderTekto() {
        super(SpaceDimensions.TEKTO);
    }

    @Override
    public BlockState getStone() {
        return BuiltInRegistries.BLOCK
                .getOptional(Library.id("basalt"))
                .orElseThrow()
                .defaultBlockState();
    }

    @Override
    public boolean hasIce() {
        return true;
    }

    @Override
    public float getSunBrightness(float vanilla, Sky sky) {
        return super.getSunBrightness(vanilla, sky) * 0.7F;
    }

    public static BiomeGenLayers createBiomeGenerators(long seed) {
        GenLayer biomes =
                new GenLayerCelestialBiomes(
                        seed,
                        BiomeGenBaseTekto.polyvinylPlains,
                        BiomeGenBaseTekto.halogenHills,
                        BiomeGenBaseTekto.forest,
                        BiomeGenBaseTekto.vinylsands);

        biomes = new GenLayerZoom(1000L, biomes);
        biomes = new GenLayerZoom(1001L, biomes);
        biomes = new GenLayerZoom(1002L, biomes);
        biomes = new GenLayerZoom(1003L, biomes);
        biomes = new GenLayerZoom(1004L, biomes);
        biomes = new GenLayerZoom(1005L, biomes);

        GenLayer riverZoom = new GenLayerZoom(1000L, biomes);
        GenLayer river = new GenLayerRiver(1001L, riverZoom);
        GenLayer riverSmooth = new GenLayerSmooth(1000L, river);

        GenLayer biomeSmooth = new GenLayerSmooth(1000L, biomes);
        GenLayer riverMix = new GenLayerTektoRiverMix(100L, biomeSmooth, riverSmooth);
        GenLayer voronoiZoom = new GenLayerVoronoiZoom(10L, riverMix);

        return new BiomeGenLayers(riverMix, voronoiZoom, seed);
    }
}
