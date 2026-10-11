// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-FileCopyrightText: 2025 70000hp
// SPDX-FileCopyrightText: 2025 Damielle
// SPDX-FileCopyrightText: 2024-2025 George Paton
// SPDX-FileCopyrightText: 2021-2024 HbmMods (The Bobcat)
// SPDX-FileCopyrightText: 2023-2025 JamesH_2
// SPDX-FileCopyrightText: 2023 TheNewGuy
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.inventory;

import com.hbm.inventory.OreDictManager.DictFrame;
import com.hbmspace.HbmSpace;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public final class OreDictManagerSpace {

    public static final DictFrame GLOWSTONE = new DictFrame("glowstone");
    public static final DictFrame NI = new DictFrame("nickel_pure");
    public static final DictFrame STAINLESS = new DictFrame("stainless_steel");
    public static final DictFrame SEMTEX = new DictFrame("semtex");
    public static final DictFrame HAFNIUM = new DictFrame("hafnium");
    public static final DictFrame IRIDIUM = new DictFrame("iridium");
    public static final DictFrame ZI = new DictFrame("zinc");
    public static final DictFrame GALLIUM = new DictFrame("gallium");
    public static final DictFrame GAAS = new DictFrame("gallium_arsenide");
    public static final DictFrame CONGLOMERATE = new DictFrame("conglomerate");
    public static final DictFrame RICHMAGMA = new DictFrame("rich_magma");
    public static final DictFrame TASMANITE = new DictFrame("tasmanite");
    public static final DictFrame AYERITE = new DictFrame("ayerite");
    public static final DictFrame CM242 = new DictFrame("curium_242");
    public static final DictFrame CM243 = new DictFrame("curium_243");
    public static final DictFrame CM244 = new DictFrame("curium_244");
    public static final DictFrame CM245 = new DictFrame("curium_245");
    public static final DictFrame CM246 = new DictFrame("curium_246");
    public static final DictFrame CM247 = new DictFrame("curium_247");
    public static final DictFrame CMRG = new DictFrame("curium_rg");
    public static final DictFrame CMF = new DictFrame("curium_fuel");
    public static final DictFrame BK247 = new DictFrame("berkelium_247");
    public static final DictFrame CF251 = new DictFrame("californium_251");
    public static final DictFrame CF252 = new DictFrame("californium_252");
    public static final DictFrame ES253 = new DictFrame("einsteinium_253");
    public static final DictFrame ES255 = new DictFrame("einsteinium_255");
    public static final DictFrame CN989 = new DictFrame("chinesium_989");
    public static final DictFrame MEN = new DictFrame("menthol");
    public static final DictFrame NIM = new DictFrame("nickel");
    public static final DictFrame PENTLANDITE = new DictFrame("pentlandite");
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
