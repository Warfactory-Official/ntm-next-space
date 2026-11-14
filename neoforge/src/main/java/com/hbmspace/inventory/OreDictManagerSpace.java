/*
 * SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
 * SPDX-FileCopyrightText: 2025 70000hp
 * SPDX-FileCopyrightText: 2025 Damielle
 * SPDX-FileCopyrightText: 2024-2025 George Paton
 * SPDX-FileCopyrightText: 2021-2024 HbmMods (The Bobcat)
 * SPDX-FileCopyrightText: 2023-2025 JamesH_2
 * SPDX-FileCopyrightText: 2023 TheNewGuy
 * SPDX-License-Identifier: LGPL-3.0-only
 *
 * Copyright (C) 2026 movblock. All rights reserved.
 * Copyright (C) 2025 70000hp. All rights reserved.
 * Copyright (C) 2025 Damielle. All rights reserved.
 * Copyright (C) 2024-2025 George Paton. All rights reserved.
 * Copyright (C) 2021-2024 HbmMods (The Bobcat). All rights reserved.
 * Copyright (C) 2023-2025 JamesH_2. All rights reserved.
 * Copyright (C) 2023 TheNewGuy. All rights reserved.
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

package com.hbmspace.inventory;

import com.hbm.inventory.material.MaterialTags;
import com.hbmspace.HbmSpace;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public final class OreDictManagerSpace {

    public static final MaterialTags GLOWSTONE = MaterialTags.of("glowstone");
    public static final MaterialTags NI = MaterialTags.of("nickel_pure");
    public static final MaterialTags STAINLESS = MaterialTags.of("stainless_steel");
    public static final MaterialTags SEMTEX = MaterialTags.of("semtex");
    public static final MaterialTags HAFNIUM = MaterialTags.of("hafnium");
    public static final MaterialTags IRIDIUM = MaterialTags.of("iridium");
    public static final MaterialTags ZI = MaterialTags.of("zinc");
    public static final MaterialTags GALLIUM = MaterialTags.of("gallium");
    public static final MaterialTags GAAS = MaterialTags.of("gallium_arsenide");
    public static final MaterialTags CONGLOMERATE = MaterialTags.of("conglomerate");
    public static final MaterialTags RICHMAGMA = MaterialTags.of("rich_magma");
    public static final MaterialTags TASMANITE = MaterialTags.of("tasmanite");
    public static final MaterialTags AYERITE = MaterialTags.of("ayerite");
    public static final MaterialTags CM242 = MaterialTags.of("curium_242");
    public static final MaterialTags CM243 = MaterialTags.of("curium_243");
    public static final MaterialTags CM244 = MaterialTags.of("curium_244");
    public static final MaterialTags CM245 = MaterialTags.of("curium_245");
    public static final MaterialTags CM246 = MaterialTags.of("curium_246");
    public static final MaterialTags CM247 = MaterialTags.of("curium_247");
    public static final MaterialTags CMRG = MaterialTags.of("curium_rg");
    public static final MaterialTags CMF = MaterialTags.of("curium_fuel");
    public static final MaterialTags BK247 = MaterialTags.of("berkelium_247");
    public static final MaterialTags CF251 = MaterialTags.of("californium_251");
    public static final MaterialTags CF252 = MaterialTags.of("californium_252");
    public static final MaterialTags ES253 = MaterialTags.of("einsteinium_253");
    public static final MaterialTags ES255 = MaterialTags.of("einsteinium_255");
    public static final MaterialTags CN989 = MaterialTags.of("chinesium_989");
    public static final MaterialTags MEN = MaterialTags.of("menthol");
    public static final MaterialTags NIM = MaterialTags.of("nickel");
    public static final MaterialTags PENTLANDITE = MaterialTags.of("pentlandite");
    public static final TagKey<Item> KEY_STONE = tag("c", "stones");
    public static final TagKey<Item> KEY_CHEESE = tag("c", "foods/cheese");
    public static final TagKey<Item> KEY_BUTTER = tag("c", "foods/butter");
    public static final TagKey<Item> KEY_STRAWBERRY = tag("c", "crops/strawberry");
    public static final TagKey<Item> KEY_STRAWBERRY_ICECREAM =
            tag("c", "foods/strawberry_ice_cream");
    public static final TagKey<Item> KEY_STRAWBERRY_MILKSHAKE =
            tag("c", "foods/strawberry_milkshake");
    public static final TagKey<Item> ANY_COAL_COKE_DUST =
            tag(HbmSpace.MOD_ID, "dusts/any_coal_coke");

    private OreDictManagerSpace() {}

    private static TagKey<Item> tag(String namespace, String path) {
        return TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(namespace, path));
    }
}
