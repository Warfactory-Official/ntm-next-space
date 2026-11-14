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

package com.hbmspace.dim.duna.biome;

import com.hbmspace.HbmSpace;
import com.hbmspace.blocks.ModBlocksSpace;
import com.hbmspace.blocks.TerrainBlocksSpace;
import com.hbmspace.dim.BiomeGenBaseCelestial;
import net.minecraft.resources.Identifier;

public abstract class BiomeGenBaseDuna extends BiomeGenBaseCelestial {

    public static final BiomeGenBaseCelestial dunaPlains = new BiomeGenDunaPlains();
    public static final BiomeGenBaseCelestial dunaLowlands = new BiomeGenDunaLowlands();
    public static final BiomeGenBaseCelestial dunaPolar = new BiomeGenDunaPolar();
    public static final BiomeGenBaseCelestial dunaHills = new BiomeGenDunaHills();
    public static final BiomeGenBaseCelestial dunaPolarHills = new BiomeGenDunaPolarHills();

    protected BiomeGenBaseDuna(String name, int id) {
        super(Identifier.fromNamespaceAndPath(HbmSpace.MOD_ID, name), id);
        this.setDisableRain();
        this.setTemperatureRainfall(-1.0F, 0.0F);

        this.topBlock = ModBlocksSpace.DUNA_SANDS.get();
        this.fillerBlock = TerrainBlocksSpace.DUNA_ROCK.get();
    }
}
