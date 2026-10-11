// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-FileCopyrightText: 2023 DUODEC
// SPDX-FileCopyrightText: 2021 HbmMods (The Bobcat)
// SPDX-FileCopyrightText: 2023 JamesH_2
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.blocks.generic;

import com.hbm.blocks.generic.BlockBobble.Bobble;
import com.hbm.items.special.ScrapType;
import com.hbmspace.blocks.TrinketsSpace;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public enum BobbleTypeSpace implements Bobble {
    GWEN(
            "Gwen",
            "Gwen",
            "Numero Uno Homie",
            "HELP ME I'M TRAPPED IN THIS FUCKING POLYRESIN PRISON YOU NEED TO LET ME OUT PLEASE SMASH IT OPEN DO IT NOW CRACK IT",
            true,
            ScrapType.BOARD_BLANK),
    JUICE(
            "Juicy_Lad",
            "Juicy_Lad",
            "The Mojave Testing for this funny fork",
            "\"What should the Inscription be?\",$ \"Uhh.. I'll think about it.\"",
            true,
            ScrapType.BOARD_BLANK),
    JAMESH_2(
            "JamesH_2",
            "JamesH_2",
            "The fork itself",
            "COME ON AND SLAM",
            true,
            ScrapType.BOARD_BLANK),
    MRKIMKIMORA(
            "MrKimkimora",
            "MrKimkimora",
            "Part of NTM:Space models,$textures and russian localization",
            "I know your IP. Your IP is 127.0.0.1.",
            false,
            ScrapType.BOARD_BLANK);

    private final String name;
    private final String label;
    private final String contribution;
    private final String inscription;
    private final boolean skinLayers;
    public final ScrapType scrap;

    BobbleTypeSpace(
            String name,
            String label,
            String contribution,
            String inscription,
            boolean skinLayers,
            ScrapType scrap) {
        this.name = name;
        this.label = label;
        this.contribution = contribution;
        this.inscription = inscription;
        this.skinLayers = skinLayers;
        this.scrap = scrap;
    }

    @Override
    public String fullName() {
        return name;
    }

    @Override
    public String label() {
        return label;
    }

    @Override
    public @Nullable String contribution() {
        return contribution;
    }

    @Override
    public @Nullable String inscription() {
        return inscription;
    }

    @Override
    public boolean skinLayers() {
        return skinLayers;
    }

    @Override
    public ItemStack stack() {
        return TrinketsSpace.BOBBLEHEADS.stack(this);
    }
}
