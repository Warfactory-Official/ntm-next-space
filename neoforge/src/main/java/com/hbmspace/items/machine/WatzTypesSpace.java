/*
 * SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
 * SPDX-FileCopyrightText: 2023-2024 HbmMods (The Bobcat)
 * SPDX-FileCopyrightText: 2023 mayonnaise
 * SPDX-License-Identifier: LGPL-3.0-only
 *
 * Copyright (C) 2026 movblock. All rights reserved.
 * Copyright (C) 2023-2024 HbmMods (The Bobcat). All rights reserved.
 * Copyright (C) 2023 mayonnaise. All rights reserved.
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

package com.hbmspace.items.machine;

import com.hbm.items.machine.WatzPelletItem.WatzFuel;
import com.hbm.util.ResponseFunction;
import com.hbmspace.items.FuelItemsSpace;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

import static com.hbm.util.ResponseFunction.linear;
import static com.hbm.util.ResponseFunction.root;

public enum WatzTypesSpace implements WatzFuel {
    PU241(
            0x78817E,
            394240,
            1_950,
            25,
            0.0025D,
            linear(1.30D),
            root(2.66D / 18D).withOffset(24D * 24D)),
    AMRG(
            0x93767B,
            0x66474D,
            2_888,
            48,
            0.0035D,
            linear(1.33D),
            root(4.33D / 25.5D).withOffset(28D * 28D)),
    AMF(
            0x93767B,
            0x66474D,
            2_333,
            44,
            0.003D,
            linear(1.33D),
            root(4.11D / 22.2D).withOffset(27D * 27D)),
    CMRG(
            0xD8C2C4,
            0xAD9799,
            2_999,
            50,
            0.005D,
            linear(1.5D),
            root(5.5D / 25.5D).withOffset(30D * 28D)),
    CMF(
            0xD8C2C4,
            0xAD9799,
            2_444,
            48,
            0.0045D,
            linear(1.8D),
            root(5.0D / 20D).withOffset(26D * 24D)),
    BK247(
            0xC2C9C7,
            0x8D9592,
            3_000,
            55,
            0.012D,
            linear(1.5D),
            root(6.0D / 23.5D).withOffset(10D * 10D)),
    CF251(
            0x7879B4,
            0x4D4E89,
            1_250,
            60,
            0.001D,
            linear(1.7D),
            root(6.65D / 23.5D).withOffset(10D * 10D)),
    CF252(
            0x7879B4,
            0x4D4E89,
            1_050,
            120,
            0.0015D,
            linear(1.8D),
            root(8.85D / 28.8D).withOffset(10D * 10D)),
    ES253(
            0xB9BFB2,
            0x594E44,
            3_750,
            70,
            0.0001D,
            linear(1.3D),
            root(7.0D / 27.7D).withOffset(10D * 10D));

    private final int colorLight;
    private final int colorDark;
    private final double passive;
    private final double heatEmission;
    private final double mudContent;
    private final ResponseFunction burnFunc;
    private final ResponseFunction heatDiv;

    WatzTypesSpace(
            int colorLight,
            int colorDark,
            double passive,
            double heatEmission,
            double mudContent,
            ResponseFunction burnFunction,
            ResponseFunction heatDivisor) {
        this.colorLight = colorLight;
        this.colorDark = colorDark;
        this.passive = passive;
        this.heatEmission = heatEmission;
        this.mudContent = mudContent / 2D;
        this.burnFunc = burnFunction;
        this.heatDiv = heatDivisor;
    }

    @Override
    public double yield() {
        return 1_000_000_000D;
    }

    @Override
    public int colorLight() {
        return colorLight;
    }

    @Override
    public int colorDark() {
        return colorDark;
    }

    @Override
    public double passive() {
        return passive;
    }

    @Override
    public double heatEmission() {
        return heatEmission;
    }

    @Override
    public double mudContent() {
        return mudContent;
    }

    @Override
    public ResponseFunction burnFunc() {
        return burnFunc;
    }

    @Override
    public ResponseFunction heatDiv() {
        return heatDiv;
    }

    @Override
    public @Nullable ResponseFunction absorbFunc() {
        return null;
    }

    @Override
    public ItemStack depletedStack() {
        return FuelItemsSpace.WATZ_PELLET_DEPLETED.stack(this);
    }
}
