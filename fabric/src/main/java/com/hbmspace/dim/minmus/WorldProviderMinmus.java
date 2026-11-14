/*
 * SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright (C) 2026 movblock. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * You may not use this file under the Apache License except in
 * compliance with that License. You may obtain a copy at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Apache License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License for the specific language governing permissions
 * and limitations under the License.
 */

package com.hbmspace.dim.minmus;

import com.hbmspace.blocks.ModBlocksSpace;
import com.hbmspace.dim.SpaceDimensions;
import com.hbmspace.dim.WorldProviderCelestial;
import com.hbmspace.dim.minmus.biome.BiomeGenBaseMinmus;
import com.hbmspace.world.gen.layer.BiomeLayer;
import com.hbmspace.world.gen.layer.BiomeLayers;
import com.hbmspace.world.gen.layer.CelestialLayers;
import net.minecraft.world.level.block.state.BlockState;

public class WorldProviderMinmus extends WorldProviderCelestial {

    public WorldProviderMinmus() {
        super(SpaceDimensions.MINMUS);
    }

    @Override
    public BlockState getStone() {
        return ModBlocksSpace.MINMUS_STONE.get().defaultBlockState();
    }

    public static CelestialLayers.Stack layers(long seed) {
        int canyon = BiomeGenBaseMinmus.minmusCanyon.biomeID;
        int plains = BiomeGenBaseMinmus.minmusPlains.biomeID;
        BiomeLayer biomes = BiomeLayers.pick(seed, 0L, canyon, plains);

        biomes = BiomeLayers.fuzzyZoom(seed, 2000L, biomes);
        biomes = BiomeLayers.zoom(seed, 2001L, biomes);
        biomes = BiomeLayers.diversify(seed, 1000L, biomes, 2, canyon, plains);
        biomes = BiomeLayers.zoom(seed, 1000L, biomes);
        biomes = BiomeLayers.diversify(seed, 1001L, biomes, 2, canyon, plains);
        biomes = BiomeLayers.zoom(seed, 1001L, biomes);
        biomes = BiomeLayers.border(biomes, canyon, plains, canyon);
        biomes = BiomeLayers.zoom(seed, 1003L, biomes);
        biomes = BiomeLayers.smooth(seed, 700L, biomes);
        biomes = BiomeLayers.border(biomes, plains, canyon, plains);
        biomes = BiomeLayers.zoom(seed, 1006L, biomes);

        return new CelestialLayers.Stack(biomes);
    }
}
