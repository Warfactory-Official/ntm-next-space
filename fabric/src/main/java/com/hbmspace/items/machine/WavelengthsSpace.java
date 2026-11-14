/*
 * SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright (C) 2026 movblock. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * You may not use this file under the Apache License except in
 * compliance with that License. You may obtain a copy at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Apache License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License for the specific language governing permissions
 * and limitations under the License.
 */

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
