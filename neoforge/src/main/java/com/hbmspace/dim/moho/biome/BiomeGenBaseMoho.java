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

package com.hbmspace.dim.moho.biome;

import com.hbm.util.RegistryUtil;
import com.hbmspace.HbmSpace;
import com.hbmspace.blocks.ModBlocksSpace;
import com.hbmspace.dim.BiomeGenBaseCelestial;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;

public abstract class BiomeGenBaseMoho extends BiomeGenBaseCelestial {

    public static final BiomeGenBaseCelestial mohoCrag = new BiomeGenMohoCrag();
    public static final BiomeGenBaseCelestial mohoBasalt = new BiomeGenMohoBasalt();
    public static final BiomeGenBaseCelestial mohoLavaSea = new BiomeGenMohoLavaSea();
    public static final BiomeGenBaseCelestial mohoPlateau = new BiomeGenMohoPlateau();

    protected final Block basalt =
            BuiltInRegistries.BLOCK.getOptional(RegistryUtil.id("basalt")).orElseThrow();

    protected BiomeGenBaseMoho(String name, int id) {
        super(Identifier.fromNamespaceAndPath(HbmSpace.MOD_ID, name), id);
        this.setDisableRain();

        this.setTemperatureRainfall(1.0F, 0.0F);
        this.topBlock = ModBlocksSpace.MOHO_REGOLITH.get();
        this.fillerBlock = ModBlocksSpace.MOHO_REGOLITH.get();
    }
}
