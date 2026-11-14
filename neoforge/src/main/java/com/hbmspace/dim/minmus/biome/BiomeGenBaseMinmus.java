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

package com.hbmspace.dim.minmus.biome;

import com.hbmspace.HbmSpace;
import com.hbmspace.blocks.ModBlocksSpace;
import com.hbmspace.dim.BiomeGenBaseCelestial;
import net.minecraft.resources.Identifier;

public abstract class BiomeGenBaseMinmus extends BiomeGenBaseCelestial {

    public static final BiomeGenBaseCelestial minmusPlains = new BiomeGenMinmusHills();
    public static final BiomeGenBaseCelestial minmusCanyon = new BiomeGenMinmusBasin();

    protected BiomeGenBaseMinmus(String name, int id) {
        super(Identifier.fromNamespaceAndPath(HbmSpace.MOD_ID, name), id);
        this.setTemperatureRainfall(-1.0F, 0.0F);

        this.topBlock = ModBlocksSpace.MINMUS_REGOLITH.get();
        this.fillerBlock = ModBlocksSpace.MINMUS_REGOLITH.get();
    }
}
