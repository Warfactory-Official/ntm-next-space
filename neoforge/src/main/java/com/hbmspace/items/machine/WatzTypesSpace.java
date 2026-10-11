// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-FileCopyrightText: 2023-2024 HbmMods (The Bobcat)
// SPDX-FileCopyrightText: 2023 mayonnaise
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.items.machine;

import com.hbm.items.machine.ItemWatzPellet.WatzFuel;
import com.hbm.util.function.Function.FunctionLinear;
import com.hbm.util.function.Function.FunctionSqrt;
import com.hbm.util.function.Function;
import com.hbmspace.items.FuelItemsSpace;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public enum WatzTypesSpace implements WatzFuel {
    PU241(
            0x78817E,
            394240,
            1_950,
            25,
            0.0025D,
            new FunctionLinear(1.30D),
            new FunctionSqrt(2.66D / 18D).withOff(24D * 24D)),
    AMRG(
            0x93767B,
            0x66474D,
            2_888,
            48,
            0.0035D,
            new FunctionLinear(1.33D),
            new FunctionSqrt(4.33D / 25.5D).withOff(28D * 28D)),
    AMF(
            0x93767B,
            0x66474D,
            2_333,
            44,
            0.003D,
            new FunctionLinear(1.33D),
            new FunctionSqrt(4.11D / 22.2D).withOff(27D * 27D)),
    CMRG(
            0xD8C2C4,
            0xAD9799,
            2_999,
            50,
            0.005D,
            new FunctionLinear(1.5D),
            new FunctionSqrt(5.5D / 25.5D).withOff(30D * 28D)),
    CMF(
            0xD8C2C4,
            0xAD9799,
            2_444,
            48,
            0.0045D,
            new FunctionLinear(1.8D),
            new FunctionSqrt(5.0D / 20D).withOff(26D * 24D)),
    BK247(
            0xC2C9C7,
            0x8D9592,
            3_000,
            55,
            0.012D,
            new FunctionLinear(1.5D),
            new FunctionSqrt(6.0D / 23.5D).withOff(10D * 10D)),
    CF251(
            0x7879B4,
            0x4D4E89,
            1_250,
            60,
            0.001D,
            new FunctionLinear(1.7D),
            new FunctionSqrt(6.65D / 23.5D).withOff(10D * 10D)),
    CF252(
            0x7879B4,
            0x4D4E89,
            1_050,
            120,
            0.0015D,
            new FunctionLinear(1.8D),
            new FunctionSqrt(8.85D / 28.8D).withOff(10D * 10D)),
    ES253(
            0xB9BFB2,
            0x594E44,
            3_750,
            70,
            0.0001D,
            new FunctionLinear(1.3D),
            new FunctionSqrt(7.0D / 27.7D).withOff(10D * 10D));

    private final int colorLight;
    private final int colorDark;
    private final double passive;
    private final double heatEmission;
    private final double mudContent;
    private final Function burnFunc;
    private final Function heatDiv;

    WatzTypesSpace(
            int colorLight,
            int colorDark,
            double passive,
            double heatEmission,
            double mudContent,
            Function burnFunction,
            Function heatDivisor) {
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
    public Function burnFunc() {
        return burnFunc;
    }

    @Override
    public Function heatDiv() {
        return heatDiv;
    }

    @Override
    public @Nullable Function absorbFunc() {
        return null;
    }

    @Override
    public ItemStack depletedStack() {
        return FuelItemsSpace.WATZ_PELLET_DEPLETED.stack(this);
    }
}
