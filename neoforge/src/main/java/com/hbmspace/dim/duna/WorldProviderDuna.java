// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-FileCopyrightText: 2024 George Paton
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.dim.duna;

import com.hbmspace.blocks.TerrainBlocksSpace;
import com.hbmspace.dim.BiomeGenBaseCelestial;
import com.hbmspace.dim.GenLayerBorder;
import com.hbmspace.dim.GenLayerCelestialBiomes;
import com.hbmspace.dim.GenLayerDiversify;
import com.hbmspace.dim.SpaceDimensions;
import com.hbmspace.dim.WorldChunkManagerCelestial.BiomeGenLayers;
import com.hbmspace.dim.WorldProviderCelestial;
import com.hbmspace.dim.duna.biome.BiomeGenBaseDuna;
import com.hbmspace.dim.legacy.layer.GenLayer;
import com.hbmspace.dim.legacy.layer.GenLayerFuzzyZoom;
import com.hbmspace.dim.legacy.layer.GenLayerRiver;
import com.hbmspace.dim.legacy.layer.GenLayerRiverMix;
import com.hbmspace.dim.legacy.layer.GenLayerSmooth;
import com.hbmspace.dim.legacy.layer.GenLayerVoronoiZoom;
import com.hbmspace.dim.legacy.layer.GenLayerZoom;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.attribute.BedRule;
import net.minecraft.world.attribute.EnvironmentAttributeSystem;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class WorldProviderDuna extends WorldProviderCelestial {

    public WorldProviderDuna() {
        super(SpaceDimensions.DUNA);
    }

    @Override
    public BlockState getStone() {
        return TerrainBlocksSpace.DUNA_ROCK.get().defaultBlockState();
    }

    @Override
    public int getHorizon(Level level) {
        return 52;
    }

    @Override
    public void updateWeather(ServerLevel level) {
        DunaDustStorm storm = DunaDustStorm.get(level);
        float intensity = storm.intensity;
        storm.tick(level.getRandom());
        if (storm.intensity != intensity) DunaDustStorm.broadcast(level);
    }

    @Override
    public void resetRainAndThunder(ServerLevel level) {
        DunaDustStorm.get(level).clear(level.getRandom());
        DunaDustStorm.broadcast(level);
    }

    @Override
    public void addLayers(EnvironmentAttributeSystem.Builder builder, Level level) {
        if (!(level instanceof ServerLevel server)) return;
        builder.addPositionalLayer(
                EnvironmentAttributes.MONSTERS_BURN,
                (value, pos, biomes) -> value && !DunaDustStorm.get(server).darkensDay());
        builder.addPositionalLayer(
                EnvironmentAttributes.BED_RULE,
                (value, pos, biomes) ->
                        value.canSleep() == BedRule.Rule.WHEN_DARK
                                        && DunaDustStorm.get(server).darkensDay()
                                ? new BedRule(
                                        BedRule.Rule.ALWAYS,
                                        value.canSetSpawn(),
                                        value.explodes(),
                                        value.errorMessage())
                                : value);
    }

    public static BiomeGenLayers createBiomeGenerators(long seed) {
        BiomeGenBaseCelestial plains = BiomeGenBaseDuna.dunaPlains;
        BiomeGenBaseCelestial lowlands = BiomeGenBaseDuna.dunaLowlands;
        BiomeGenBaseCelestial[] all = {
            plains,
            lowlands,
            BiomeGenBaseDuna.dunaPolar,
            BiomeGenBaseDuna.dunaHills,
            BiomeGenBaseDuna.dunaPolarHills
        };
        GenLayer biomes = new GenLayerCelestialBiomes(seed, all);

        biomes = new GenLayerFuzzyZoom(2000L, biomes);
        biomes = new GenLayerZoom(2001L, biomes);
        biomes = new GenLayerDiversify(1000L, biomes, 2, all);
        biomes = new GenLayerZoom(1000L, biomes);
        biomes = new GenLayerDiversify(1001L, biomes, 2, all);
        biomes = new GenLayerZoom(1001L, biomes);
        biomes = new GenLayerBorder(1300L, biomes, lowlands, plains, lowlands);
        biomes = new GenLayerZoom(1003L, biomes);
        biomes = new GenLayerSmooth(700L, biomes);
        biomes = new GenLayerZoom(1005L, biomes);
        biomes = new GenLayerSmooth(703L, biomes);
        biomes = new GenLayerFuzzyZoom(1000L, biomes);
        biomes = new GenLayerSmooth(705L, biomes);
        biomes = new GenLayerFuzzyZoom(1001L, biomes);
        biomes = new GenLayerZoom(1006L, biomes);

        GenLayer genLayerVoronoiZoom = new GenLayerVoronoiZoom(10L, biomes);

        GenLayer riverZoom = new GenLayerZoom(1000L, biomes);
        GenLayer river = new GenLayerRiver(1004L, riverZoom);
        river = new GenLayerZoom(105L, river);
        GenLayer riverMix = new GenLayerRiverMix(100L, biomes, river);

        return new BiomeGenLayers(riverMix, genLayerVoronoiZoom, seed);
    }
}
