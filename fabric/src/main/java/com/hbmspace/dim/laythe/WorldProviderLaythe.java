/*
 * SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
 * SPDX-FileCopyrightText: 2024-2025 George Paton
 * SPDX-License-Identifier: LGPL-3.0-only
 *
 * Copyright (C) 2026 movblock. All rights reserved.
 * Copyright (C) 2024-2025 George Paton. All rights reserved.
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

package com.hbmspace.dim.laythe;

import com.hbmspace.dim.SpaceDimensions;
import com.hbmspace.dim.WorldProviderCelestial;
import com.hbmspace.dim.laythe.biome.BiomeGenBaseLaythe;
import com.hbmspace.world.gen.layer.BiomeLayer;
import com.hbmspace.world.gen.layer.BiomeLayers;
import com.hbmspace.world.gen.layer.CelestialLayers;

public class WorldProviderLaythe extends WorldProviderCelestial {

    public WorldProviderLaythe() {
        super(SpaceDimensions.LAYTHE);
    }

    @Override
    public boolean hasLife() {
        return true;
    }

    public static CelestialLayers.Stack layers(long seed) {
        int island = BiomeGenBaseLaythe.laytheIsland.biomeID;
        int ocean = BiomeGenBaseLaythe.laytheOcean.biomeID;
        int coast = BiomeGenBaseLaythe.laytheCoast.biomeID;
        int polar = BiomeGenBaseLaythe.laythePolar.biomeID;
        BiomeLayer biomes = BiomeLayers.pick(seed, 0L, island, ocean, coast);
        biomes = BiomeLayers.enclosed(biomes, ocean, polar);

        biomes = BiomeLayers.zoom(seed, 1040L, biomes);
        biomes = BiomeLayers.zoom(seed, 1000L, biomes);
        biomes = BiomeLayers.enclosed(biomes, ocean, polar);

        biomes = BiomeLayers.diversify(seed, 1000L, biomes, 2, island, ocean, coast);
        biomes = BiomeLayers.border(biomes, ocean, island, coast);
        biomes = BiomeLayers.islands(seed, 200L, biomes, island, coast, ocean);

        biomes = BiomeLayers.zoom(seed, 1200L, biomes);
        biomes = BiomeLayers.zoom(seed, 1001L, biomes);

        biomes = BiomeLayers.border(biomes, ocean, island, coast);
        biomes = BiomeLayers.border(biomes, ocean, coast, coast);

        biomes = BiomeLayers.zoom(seed, 1003L, biomes);
        biomes = BiomeLayers.border(biomes, ocean, coast, coast);
        biomes = BiomeLayers.smooth(seed, 700L, biomes);

        biomes = BiomeLayers.zoom(seed, 1006L, biomes);

        return new CelestialLayers.Stack(biomes);
    }
}
