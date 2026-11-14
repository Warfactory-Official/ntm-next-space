/*
 * SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
 * SPDX-FileCopyrightText: 2023 DUODEC
 * SPDX-FileCopyrightText: 2021 HbmMods (The Bobcat)
 * SPDX-FileCopyrightText: 2023 JamesH_2
 * SPDX-License-Identifier: LGPL-3.0-only
 *
 * Copyright (C) 2026 movblock. All rights reserved.
 * Copyright (C) 2023 DUODEC. All rights reserved.
 * Copyright (C) 2021 HbmMods (The Bobcat). All rights reserved.
 * Copyright (C) 2023 JamesH_2. All rights reserved.
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

package com.hbmspace.blocks.generic;

import com.hbm.blocks.generic.BobbleBlock.Bobble;
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
