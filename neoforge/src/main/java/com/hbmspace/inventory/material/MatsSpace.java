/*
 * SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
 * SPDX-FileCopyrightText: 2025 70000hp
 * SPDX-FileCopyrightText: 2024-2026 George Paton
 * SPDX-FileCopyrightText: 2024 HbmMods (The Bobcat)
 * SPDX-License-Identifier: LGPL-3.0-only
 *
 * Copyright (C) 2026 movblock. All rights reserved.
 * Copyright (C) 2025 70000hp. All rights reserved.
 * Copyright (C) 2024-2026 George Paton. All rights reserved.
 * Copyright (C) 2024 HbmMods (The Bobcat). All rights reserved.
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

package com.hbmspace.inventory.material;

import com.hbm.inventory.material.Material;
import com.hbm.inventory.material.Materials;
import com.hbmspace.HbmSpace;

import static com.hbm.inventory.material.MaterialShape.*;
import static com.hbm.inventory.material.MaterialTagRegistry.AUSTRALIUM;
import static com.hbm.inventory.material.MaterialTagRegistry.BR;
import static com.hbm.inventory.material.MaterialTagRegistry.I;
import static com.hbm.inventory.material.MaterialTagRegistry.LAPIS;
import static com.hbm.inventory.material.MaterialTagRegistry.NETHERQUARTZ;
import static com.hbm.inventory.material.MaterialTagRegistry.P_WHITE;
import static com.hbm.inventory.material.SmeltingBehavior.*;
import static com.hbmspace.inventory.OreDictManagerSpace.*;

public final class MatsSpace {

    public static final int _EX = 24_000;

    public static final Material MAT_GLOWSTONE =
            space(
                    Material.define(
                                    Materials.BASE_VANILLA + 5,
                                    GLOWSTONE,
                                    NOT_SMELTABLE,
                                    0xFFFF00,
                                    0x535300,
                                    0xFFFF00)
                            .after(Materials.OBSIDIAN)
                            .autoShapes(FRAGMENT)
                            .memberShapes(DUST));
    public static final Material MAT_CONGLOMERATE =
            space(
                    Material.define(2993, CONGLOMERATE, ADDITIVE, 0x797979, 0x797979, 0x797979)
                            .after(Materials.CRYOLITE)
                            .memberShapes(ORE));
    public static final Material MAT_AUSTRALIUM =
            space(
                    Material.define(13800, AUSTRALIUM, SMELTABLE, 0xFFFF00, 0x935B00, 0xFFFF00)
                            .after(Materials.GHIORSIUM_336)
                            .autoShapes(FRAGMENT)
                            .memberShapes(NUGGET, BILLET, INGOT, DUST, BLOCK, ORE));
    public static final Material MAT_TASMANITE =
            space(
                    Material.define(13895, TASMANITE, SMELTABLE, 0xFFFF00, 0x935B00, 0xFFFF00)
                            .after(MAT_AUSTRALIUM)
                            .autoShapes(FRAGMENT)
                            .memberShapes(NUGGET, BILLET, INGOT)
                            .namedId(INGOT, "ingot_australium_lesser"));
    public static final Material MAT_AYERITE =
            space(
                    Material.define(13851, AYERITE, SMELTABLE, 0xFFFF00, 0x935B00, 0xFFFF00)
                            .after(MAT_TASMANITE)
                            .autoShapes(FRAGMENT)
                            .memberShapes(NUGGET, BILLET, INGOT)
                            .namedId(INGOT, "ingot_australium_greater"));
    public static final Material MAT_LAPIS =
            space(
                    Material.define(2701, LAPIS, NOT_SMELTABLE, 0x567DF9, 0x001793, 0x264DC9)
                            .after(Materials.COBALT)
                            .autoShapes(FRAGMENT, DUST, GEM, BLOCK)
                            .memberShapes(ORE));
    public static final Material MAT_PHOSPHORUS_W =
            space(
                    Material.define(1501, P_WHITE, NOT_SMELTABLE, 0xF5F5ED, 0xC4BD9A, 0xC4BD9A)
                            .after(Materials.RED_PHOSPHORUS)
                            .autoShapes(FRAGMENT, DUST)
                            .memberShapes(INGOT, BLOCK));
    public static final Material MAT_QUARTZ =
            space(
                    Material.define(1402, NETHERQUARTZ, NOT_SMELTABLE, 0xF7F5F2, 0x6F5D5A, 0xF7F5F2)
                            .after(Materials.ASBESTOS)
                            .autoShapes(FRAGMENT)
                            .memberShapes(GEM, DUST, ORE));
    public static final Material MAT_NICKEL =
            space(
                    Material.define(2800, NI, SMELTABLE, 0xE8D1C7, 0x87756E, 0xAE9572)
                            .after(Materials.OSMIRIDIUM)
                            .autoShapes(FRAGMENT, NUGGET, DUST, BLOCK)
                            .memberShapes(INGOT, PLATE, ORE)
                            .namedId(INGOT, "ingot_nickel")
                            .namedId(NUGGET, "nugget_nickel")
                            .namedId(DUST, "powder_nickel")
                            .namedId(PLATE, "plate_nickel"));
    public static final Material MAT_GALLIUM =
            space(
                    Material.define(3100, GALLIUM, SMELTABLE, 0x52687F, 0x52687F, 0x52687F)
                            .after(MAT_NICKEL)
                            .autoShapes(FRAGMENT, NUGGET, DUST, TINY_DUST)
                            .memberShapes(INGOT)
                            .namedId(INGOT, "ingot_gallium")
                            .namedId(NUGGET, "nugget_gallium")
                            .namedId(DUST, "powder_gallium")
                            .namedId(TINY_DUST, "powder_gallium_tiny"));
    public static final Material MAT_ZINC =
            space(
                    Material.define(3000, ZI, SMELTABLE, 0xD7CBDA, 0x7A7277, 0xA79DA8)
                            .after(MAT_GALLIUM)
                            .autoShapes(FRAGMENT, NUGGET, DUST, FINE_WIRE)
                            .memberShapes(INGOT, ORE)
                            .namedId(INGOT, "ingot_zinc")
                            .namedId(NUGGET, "nugget_zinc")
                            .namedId(DUST, "powder_zinc"));
    public static final Material MAT_BROMINE =
            space(
                    Material.define(3500, BR, NOT_SMELTABLE, 0xFF642B, 0x720000, 0xFF642B)
                            .after(MAT_ZINC)
                            .autoShapes(FRAGMENT)
                            .memberShapes(DUST));

    public static final Material MAT_IODINE =
            space(
                    Material.define(5300, I, NOT_SMELTABLE, 0x7A8796, 0x3F3049, 0x7A8796)
                            .after(MAT_BROMINE)
                            .autoShapes(FRAGMENT)
                            .memberShapes(DUST, ORE));

    public static final Material MAT_HAFNIUM =
            space(
                    Material.define(7200, HAFNIUM, SMELTABLE, 0xFFF8C7, 0x2E1600, 0xFFF8C7)
                            .after(MAT_IODINE)
                            .autoShapes(FRAGMENT, DUST)
                            .memberShapes(NUGGET, INGOT)
                            .namedId(INGOT, "ingot_hafnium")
                            .namedId(NUGGET, "nugget_hafnium"));
    public static final Material MAT_IRIDIUM =
            space(
                    Material.define(7700, IRIDIUM, SMELTABLE, 0xB8D0FF, 0xB8D0FF, 0xB8D0FF)
                            .after(MAT_HAFNIUM)
                            .autoShapes(INGOT)
                            .namedId(INGOT, "ingot_iridium"));
    public static final Material MAT_GAAS =
            space(
                    Material.define(_EX, GAAS, SMELTABLE, 0x6F4A57, 0x6F4A57, 0x6F4A57)
                            .after(Materials.SATURNITE)
                            .autoShapes(NUGGET, BILLET)
                            .memberShapes(INGOT)
                            .namedId(INGOT, "ingot_gaas", "ingot_gaas1")
                            .namedId(NUGGET, "nugget_gaas")
                            .namedId(BILLET, "billet_gaas", "billet_gaas1"));
    public static final Material MAT_STAINLESS =
            space(
                    Material.define(_EX + 1, STAINLESS, SMELTABLE, 0xD8D8D8, 0x474747, 0x4A4A4A)
                            .after(MAT_GAAS)
                            .autoShapes(PLATE, WELDED_PLATE, CAST_PLATE)
                            .memberShapes(INGOT)
                            .namedId(INGOT, "ingot_stainless")
                            .namedId(PLATE, "plate_stainless"));
    public static final Material MAT_RICH_MAGMA =
            space(
                    Material.define(_EX + 2, RICHMAGMA, SMELTABLE, 0x7F7F7F, 0x353555, 0xFF6212)
                            .after(MAT_STAINLESS)
                            .memberShapes(INGOT)
                            .namedId(INGOT, "ingot_magma"));
    public static final Material MAT_SEMTEX =
            space(
                    Material.define(_EX + 3, SEMTEX, NOT_SMELTABLE, 0xEDAA28, 0x825D16, 0xF0B090)
                            .after(MAT_RICH_MAGMA)
                            .autoShapes(FRAGMENT)
                            .memberShapes(INGOT, BLOCK));

    static {
        for (Material mat :
                new Material[] {
                    Materials.URANIUM_233,
                    Materials.URANIUM_235,
                    Materials.PLUTONIUM,
                    Materials.PLUTONIUM_239,
                    Materials.NEPTUNIUM_237,
                    Materials.COBALT_60,
                    Materials.GOLD_198,
                    Materials.LEAD_209,
                    Materials.SCHRABIDIUM,
                    Materials.SOLINIUM,
                    Materials.GHIORSIUM_336,
                    Materials.ARSENIC,
                    Materials.CADMIUM,
                    Materials.POLYMER
                }) {
            mat.addAutoShapes(FRAGMENT);
        }
        Materials.RUBBER.addAutoShapes(FRAGMENT, DUST).addNamedId(DUST, "powder_rubber");
        Materials.PVC.addAutoShapes(FRAGMENT, DUST).addNamedId(DUST, "powder_pvc");
        Materials.MINGRADE.addMemberShapes(BILLET).addNamedId(BILLET, "billet_red_copper");
        Materials.LANTHANUM.addNamedId(NUGGET, "nugget_lanthanium");
    }

    private static Material space(Material.Declaration declaration) {
        return declaration.owner(HbmSpace.MOD_ID).register();
    }

    private MatsSpace() {}
}
