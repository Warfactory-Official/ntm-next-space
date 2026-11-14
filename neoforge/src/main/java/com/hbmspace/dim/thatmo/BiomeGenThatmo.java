/*
 * SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
 * SPDX-FileCopyrightText: 2024-2025 JamesH_2
 * SPDX-FileCopyrightText: 2026 detree05
 * SPDX-License-Identifier: LGPL-3.0-only
 *
 * Copyright (C) 2026 movblock. All rights reserved.
 * Copyright (C) 2024-2025 JamesH_2. All rights reserved.
 * Copyright (C) 2026 detree05. All rights reserved.
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

package com.hbmspace.dim.thatmo;

import com.hbmspace.HbmSpace;
import com.hbmspace.dim.BiomeGenBaseCelestial;
import com.hbmspace.entity.MobEntitiesSpace;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Blocks;

public class BiomeGenThatmo extends BiomeGenBaseCelestial {

    public static final BiomeGenThatmo biome = new BiomeGenThatmo();

    private BiomeGenThatmo() {
        super(Identifier.fromNamespaceAndPath(HbmSpace.MOD_ID, "thatmo"), 87);
        this.setBiomeName("Thatmo");
        this.setDisableRain();
        this.setTemperatureRainfall(-1.0F, 0.0F);
        this.creatures.add(new SpawnListEntry(MobEntitiesSpace.TANKBOT, 6, 1, 1));
        this.creatures.add(new SpawnListEntry(MobEntitiesSpace.WAR_BEHEMOTH, 2, 1, 1));
        this.creatures.add(new SpawnListEntry(MobEntitiesSpace.MOON_COW, 10, 1, 1));

        this.setHeight(new Height(0.125F, 0.05F));

        this.topBlock = Blocks.SNOW_BLOCK;
        this.fillerBlock = Blocks.SNOW_BLOCK;
    }
}
