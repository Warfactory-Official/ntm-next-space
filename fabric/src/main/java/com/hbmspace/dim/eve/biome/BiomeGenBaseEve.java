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

package com.hbmspace.dim.eve.biome;

import com.hbmspace.HbmSpace;
import com.hbmspace.blocks.TerrainBlocksSpace;
import com.hbmspace.dim.BiomeGenBaseCelestial;
import net.minecraft.resources.Identifier;

public abstract class BiomeGenBaseEve extends BiomeGenBaseCelestial {
    public static final BiomeGenBaseCelestial evePlains = new BiomeGenEvePlains();
    public static final BiomeGenBaseCelestial eveOcean = new BiomeGenEveOcean();
    public static final BiomeGenBaseCelestial eveMountains = new BiomeGenEveMountains();
    public static final BiomeGenBaseCelestial eveSeismicPlains = new BiomeGenEveSeismicPlains();
    public static final BiomeGenBaseCelestial eveRiver = new BiomeGenEveRiver();

    protected BiomeGenBaseEve(String name, int id) {
        super(Identifier.fromNamespaceAndPath(HbmSpace.MOD_ID, name), id);
        this.waterColorMultiplier = 0x5b009a;
        this.setTemperatureRainfall(1.0F, 0.5F);

        this.topBlock = TerrainBlocksSpace.EVE_SILT.get();
        this.fillerBlock = TerrainBlocksSpace.EVE_ROCK.get();
    }
}
