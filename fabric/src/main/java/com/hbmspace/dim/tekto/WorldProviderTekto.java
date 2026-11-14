/*
 * SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
 * SPDX-FileCopyrightText: 2024 JamesH_2
 * SPDX-License-Identifier: LGPL-3.0-only
 *
 * Copyright (C) 2026 movblock. All rights reserved.
 * Copyright (C) 2024 JamesH_2. All rights reserved.
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

package com.hbmspace.dim.tekto;

import com.hbm.util.RegistryUtil;
import com.hbmspace.dim.SpaceDimensions;
import com.hbmspace.dim.WorldProviderCelestial;
import com.hbmspace.dim.tekto.biome.BiomeGenBaseTekto;
import com.hbmspace.world.gen.layer.BiomeLayer;
import com.hbmspace.world.gen.layer.BiomeLayers;
import com.hbmspace.world.gen.layer.CelestialLayers;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.state.BlockState;

public class WorldProviderTekto extends WorldProviderCelestial {

    public WorldProviderTekto() {
        super(SpaceDimensions.TEKTO);
    }

    @Override
    public BlockState getStone() {
        return BuiltInRegistries.BLOCK
                .getOptional(RegistryUtil.id("basalt"))
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

    public static CelestialLayers.Stack layers(long seed) {
        int plains = BiomeGenBaseTekto.polyvinylPlains.biomeID;
        BiomeLayer biomes =
                BiomeLayers.pick(
                        seed,
                        0L,
                        plains,
                        BiomeGenBaseTekto.halogenHills.biomeID,
                        BiomeGenBaseTekto.forest.biomeID,
                        BiomeGenBaseTekto.vinylsands.biomeID);

        for (long salt = 1000L; salt <= 1005L; salt++)
            biomes = BiomeLayers.zoom(seed, salt, biomes);

        BiomeLayer river = BiomeLayers.river(BiomeLayers.zoom(seed, 1000L, biomes));
        river = BiomeLayers.smooth(seed, 1000L, river);
        BiomeLayer smoothed = BiomeLayers.smooth(seed, 1000L, biomes);

        return new CelestialLayers.Stack(
                BiomeLayers.riverMix(
                        smoothed, river, BiomeGenBaseTekto.tetrachloricRiver.biomeID, plains));
    }
}
