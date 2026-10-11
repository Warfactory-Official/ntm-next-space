// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.items.machine;

import com.hbm.items.machine.EnumWavelengths;
import net.minecraft.ChatFormatting;

public final class WavelengthsSpace {
    public static final EnumWavelengths XRAY =
            EnumWavelengths.REGISTRY.addAfter(
                    EnumWavelengths.UV,
                    new EnumWavelengths(
                            "XRAY",
                            "wavelengths.name.xray",
                            "wavelengths.waveRange.xray",
                            0xF8E6A7,
                            0xF8E6A7,
                            ChatFormatting.GOLD));

    private WavelengthsSpace() {}
}
