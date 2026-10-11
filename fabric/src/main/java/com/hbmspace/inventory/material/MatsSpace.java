// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-FileCopyrightText: 2025 70000hp
// SPDX-FileCopyrightText: 2024-2026 George Paton
// SPDX-FileCopyrightText: 2024 HbmMods (The Bobcat)
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.inventory.material;

import com.hbm.inventory.material.NTMMaterial;
import com.hbmspace.HbmSpace;

import static com.hbm.inventory.OreDictManager.AUSTRALIUM;
import static com.hbm.inventory.OreDictManager.BR;
import static com.hbm.inventory.OreDictManager.I;
import static com.hbm.inventory.OreDictManager.LAPIS;
import static com.hbm.inventory.OreDictManager.NETHERQUARTZ;
import static com.hbm.inventory.OreDictManager.P_WHITE;
import static com.hbm.inventory.material.MaterialShapes.*;
import static com.hbm.inventory.material.Mats.*;
import static com.hbmspace.inventory.OreDictManagerSpace.*;

public final class MatsSpace {

    public static final int _EX = 24_000;

    public static final NTMMaterial MAT_GLOWSTONE =
            makeNonSmeltable(_VS + 5, GLOWSTONE, 0xFFFF00, 0x535300, 0xFFFF00)
                    .setAutogen(FRAGMENT)
                    .setTagOnly(DUST)
                    .n()
                    .namespace(HbmSpace.MOD_ID)
                    .after(MAT_OBSIDIAN);
    public static final NTMMaterial MAT_CONGLOMERATE =
            makeAdditive(2993, CONGLOMERATE, 0x797979, 0x797979, 0x797979)
                    .setTagOnly(ORE)
                    .m()
                    .namespace(HbmSpace.MOD_ID)
                    .after(MAT_CRYOLITE);
    public static final NTMMaterial MAT_AUSTRALIUM =
            makeSmeltable(13800, AUSTRALIUM, 0xFFFF00, 0x935B00, 0xFFFF00)
                    .setAutogen(FRAGMENT)
                    .setTagOnly(NUGGET, BILLET, INGOT, DUST, BLOCK, ORE)
                    .m()
                    .namespace(HbmSpace.MOD_ID)
                    .after(MAT_GHIORSIUM);
    public static final NTMMaterial MAT_TASMANITE =
            makeSmeltable(13895, TASMANITE, 0xFFFF00, 0x935B00, 0xFFFF00)
                    .setAutogen(FRAGMENT)
                    .setTagOnly(NUGGET, BILLET, INGOT)
                    .legacy(INGOT, "ingot_australium_lesser")
                    .m()
                    .namespace(HbmSpace.MOD_ID)
                    .after(MAT_AUSTRALIUM);
    public static final NTMMaterial MAT_AYERITE =
            makeSmeltable(13851, AYERITE, 0xFFFF00, 0x935B00, 0xFFFF00)
                    .setAutogen(FRAGMENT)
                    .setTagOnly(NUGGET, BILLET, INGOT)
                    .legacy(INGOT, "ingot_australium_greater")
                    .m()
                    .namespace(HbmSpace.MOD_ID)
                    .after(MAT_TASMANITE);
    public static final NTMMaterial MAT_LAPIS =
            makeNonSmeltable(2701, LAPIS, 0x567DF9, 0x001793, 0x264DC9)
                    .setAutogen(FRAGMENT, DUST, GEM, BLOCK)
                    .setTagOnly(ORE)
                    .m()
                    .namespace(HbmSpace.MOD_ID)
                    .after(MAT_COBALT);
    public static final NTMMaterial MAT_PHOSPHORUS_W =
            makeNonSmeltable(1501, P_WHITE, 0xF5F5ED, 0xC4BD9A, 0xC4BD9A)
                    .setAutogen(FRAGMENT, DUST)
                    .setTagOnly(INGOT, BLOCK)
                    .n()
                    .namespace(HbmSpace.MOD_ID)
                    .after(MAT_PHOSPHORUS);
    public static final NTMMaterial MAT_QUARTZ =
            makeNonSmeltable(1402, NETHERQUARTZ, 0xF7F5F2, 0x6F5D5A, 0xF7F5F2)
                    .setAutogen(FRAGMENT)
                    .setTagOnly(GEM, DUST, ORE)
                    .n()
                    .namespace(HbmSpace.MOD_ID)
                    .after(MAT_ASBESTOS);
    public static final NTMMaterial MAT_NICKEL =
            makeSmeltable(2800, NI, 0xE8D1C7, 0x87756E, 0xAE9572)
                    .setAutogen(FRAGMENT, NUGGET, DUST, BLOCK)
                    .setTagOnly(INGOT, PLATE, ORE)
                    .legacy(INGOT, "ingot_nickel")
                    .legacy(NUGGET, "nugget_nickel")
                    .legacy(DUST, "powder_nickel")
                    .legacy(PLATE, "plate_nickel")
                    .m()
                    .namespace(HbmSpace.MOD_ID)
                    .after(MAT_OSMIRIDIUM);
    public static final NTMMaterial MAT_GALLIUM =
            makeSmeltable(3100, GALLIUM, 0x52687F, 0x52687F, 0x52687F)
                    .setAutogen(FRAGMENT, NUGGET, DUST, DUSTTINY)
                    .setTagOnly(INGOT)
                    .legacy(INGOT, "ingot_gallium")
                    .legacy(NUGGET, "nugget_gallium")
                    .legacy(DUST, "powder_gallium")
                    .legacy(DUSTTINY, "powder_gallium_tiny")
                    .m()
                    .namespace(HbmSpace.MOD_ID)
                    .after(MAT_NICKEL);
    public static final NTMMaterial MAT_ZINC =
            makeSmeltable(3000, ZI, 0xD7CBDA, 0x7A7277, 0xA79DA8)
                    .setAutogen(FRAGMENT, NUGGET, DUST, WIRE)
                    .setTagOnly(INGOT, ORE)
                    .legacy(INGOT, "ingot_zinc")
                    .legacy(NUGGET, "nugget_zinc")
                    .legacy(DUST, "powder_zinc")
                    .m()
                    .namespace(HbmSpace.MOD_ID)
                    .after(MAT_GALLIUM);
    public static final NTMMaterial MAT_BROMINE =
            makeNonSmeltable(3500, BR, 0xFF642B, 0x720000, 0xFF642B)
                    .setAutogen(FRAGMENT)
                    .setTagOnly(DUST)
                    .m()
                    .namespace(HbmSpace.MOD_ID)
                    .after(MAT_ZINC);

    public static final NTMMaterial MAT_IODINE =
            makeNonSmeltable(5300, I, 0x7A8796, 0x3F3049, 0x7A8796)
                    .setAutogen(FRAGMENT)
                    .setTagOnly(DUST, ORE)
                    .m()
                    .namespace(HbmSpace.MOD_ID)
                    .after(MAT_BROMINE);

    public static final NTMMaterial MAT_HAFNIUM =
            makeSmeltable(7200, HAFNIUM, 0xFFF8C7, 0x2E1600, 0xFFF8C7)
                    .setAutogen(FRAGMENT, DUST)
                    .setTagOnly(NUGGET, INGOT)
                    .legacy(INGOT, "ingot_hafnium")
                    .legacy(NUGGET, "nugget_hafnium")
                    .m()
                    .namespace(HbmSpace.MOD_ID)
                    .after(MAT_IODINE);
    public static final NTMMaterial MAT_IRIDIUM =
            makeSmeltable(7700, IRIDIUM, 0xB8D0FF, 0xB8D0FF, 0xB8D0FF)
                    .setAutogen(INGOT)
                    .legacy(INGOT, "ingot_iridium")
                    .m()
                    .namespace(HbmSpace.MOD_ID)
                    .after(MAT_HAFNIUM);
    public static final NTMMaterial MAT_GAAS =
            makeSmeltable(_EX, GAAS, 0x6F4A57, 0x6F4A57, 0x6F4A57)
                    .setAutogen(NUGGET, BILLET)
                    .setTagOnly(INGOT)
                    .legacy(INGOT, "ingot_gaas", "ingot_gaas1")
                    .legacy(NUGGET, "nugget_gaas")
                    .legacy(BILLET, "billet_gaas", "billet_gaas1")
                    .m()
                    .namespace(HbmSpace.MOD_ID)
                    .after(MAT_SATURN);
    public static final NTMMaterial MAT_STAINLESS =
            makeSmeltable(_EX + 1, STAINLESS, 0xD8D8D8, 0x474747, 0x4A4A4A)
                    .setAutogen(PLATE, WELDEDPLATE, CASTPLATE)
                    .setTagOnly(INGOT)
                    .legacy(INGOT, "ingot_stainless")
                    .legacy(PLATE, "plate_stainless")
                    .m()
                    .namespace(HbmSpace.MOD_ID)
                    .after(MAT_GAAS);
    public static final NTMMaterial MAT_RICH_MAGMA =
            makeSmeltable(_EX + 2, RICHMAGMA, 0x7F7F7F, 0x353555, 0xFF6212)
                    .setTagOnly(INGOT)
                    .legacy(INGOT, "ingot_magma")
                    .n()
                    .namespace(HbmSpace.MOD_ID)
                    .after(MAT_STAINLESS);
    public static final NTMMaterial MAT_SEMTEX =
            makeNonSmeltable(_EX + 3, SEMTEX, 0xEDAA28, 0x825D16, 0xF0B090)
                    .setAutogen(FRAGMENT)
                    .setTagOnly(INGOT, BLOCK)
                    .n()
                    .namespace(HbmSpace.MOD_ID)
                    .after(MAT_RICH_MAGMA);

    static {
        for (NTMMaterial mat :
                new NTMMaterial[] {
                    MAT_U233,
                    MAT_U235,
                    MAT_PLUTONIUM,
                    MAT_PU239,
                    MAT_NEPTUNIUM,
                    MAT_CO60,
                    MAT_AU198,
                    MAT_PB209,
                    MAT_SCHRABIDIUM,
                    MAT_SOLINIUM,
                    MAT_GHIORSIUM,
                    MAT_ARSENIC,
                    MAT_CADMIUM,
                    MAT_POLYMER
                }) {
            mat.setAutogen(FRAGMENT);
        }
        MAT_RUBBER.setAutogen(FRAGMENT, DUST).legacy(DUST, "powder_rubber");
        MAT_PVC.setAutogen(FRAGMENT, DUST).legacy(DUST, "powder_pvc");
        MAT_MINGRADE.setTagOnly(BILLET).legacy(BILLET, "billet_red_copper");
        MAT_LANTHANIUM.legacy(NUGGET, "nugget_lanthanium");
    }

    private MatsSpace() {}
}
