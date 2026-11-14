/*
 * SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
 * SPDX-FileCopyrightText: 2024 George Paton
 * SPDX-License-Identifier: LGPL-3.0-only
 *
 * Copyright (C) 2026 movblock. All rights reserved.
 * Copyright (C) 2024 George Paton. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License version 3 only,
 * as published by the Free Software Foundation.
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this code. If not, see <https://www.gnu.org/licenses/>.
 *
 * Under GPLv3 §7(c) and §7(e), these additional terms apply to LGPL-covered
 * material authored by movblock:
 *
 * - the license grants no rights to the name "NTM NEXT: Space";
 * - a modified version must not be named NTM NEXT: Space or presented as
 *   the official release, and must be marked as different from it.
 *
 * Accurately identifying a work as derived from NTM NEXT: Space does not
 * violate these additional terms, provided it is clearly distinguished from
 * the official release and does not imply endorsement. Ports are encouraged
 * to acknowledge NTM NEXT: Space as their upstream project and provide a
 * link to it.
 */

package com.hbmspace.dim.duna;

import com.hbmspace.HbmSpace;
import com.hbmspace.blocks.ModBlocksSpace;
import com.hbmspace.blocks.TerrainBlocksSpace;
import com.hbmspace.dim.SpaceDimensions;
import com.hbmspace.dim.WorldProviderCelestial;
import com.hbmspace.dim.duna.biome.BiomeGenBaseDuna;
import com.hbmspace.world.gen.Terrace;
import com.hbmspace.world.gen.layer.BiomeLayer;
import com.hbmspace.world.gen.layer.BiomeLayers;
import com.hbmspace.world.gen.layer.CelestialLayers;
import net.minecraft.resources.Identifier;
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

    public static Terrace terrace() {
        BlockState sands = ModBlocksSpace.DUNA_SANDS.get().defaultBlockState();
        return new Terrace(
                BiomeGenBaseDuna.dunaHills.key,
                Identifier.fromNamespaceAndPath(HbmSpace.MOD_ID, "duna/plateau"),
                0.05D,
                12,
                6,
                2,
                TerrainBlocksSpace.DUNA_ROCK.get().defaultBlockState(),
                sands,
                sands);
    }

    public static CelestialLayers.Stack layers(long seed) {
        int plains = BiomeGenBaseDuna.dunaPlains.biomeID;
        int lowlands = BiomeGenBaseDuna.dunaLowlands.biomeID;
        int[] all = {
            plains,
            lowlands,
            BiomeGenBaseDuna.dunaPolar.biomeID,
            BiomeGenBaseDuna.dunaHills.biomeID,
            BiomeGenBaseDuna.dunaPolarHills.biomeID
        };
        BiomeLayer biomes = BiomeLayers.pick(seed, 0L, all);

        biomes = BiomeLayers.fuzzyZoom(seed, 2000L, biomes);
        biomes = BiomeLayers.zoom(seed, 2001L, biomes);
        biomes = BiomeLayers.diversify(seed, 1000L, biomes, 2, all);
        biomes = BiomeLayers.zoom(seed, 1000L, biomes);
        biomes = BiomeLayers.diversify(seed, 1001L, biomes, 2, all);
        biomes = BiomeLayers.zoom(seed, 1001L, biomes);
        biomes = BiomeLayers.border(biomes, lowlands, plains, lowlands);
        biomes = BiomeLayers.zoom(seed, 1003L, biomes);
        biomes = BiomeLayers.smooth(seed, 700L, biomes);
        biomes = BiomeLayers.zoom(seed, 1005L, biomes);
        biomes = BiomeLayers.smooth(seed, 703L, biomes);
        biomes = BiomeLayers.fuzzyZoom(seed, 1000L, biomes);
        biomes = BiomeLayers.smooth(seed, 705L, biomes);
        biomes = BiomeLayers.fuzzyZoom(seed, 1001L, biomes);
        biomes = BiomeLayers.zoom(seed, 1006L, biomes);

        BiomeLayer river = BiomeLayers.river(BiomeLayers.zoom(seed, 1000L, biomes));
        river = BiomeLayers.zoom(seed, 105L, river);

        return new CelestialLayers.Stack(
                biomes, BiomeLayers.riverMix(biomes, river, BiomeLayers.RIVER));
    }
}
