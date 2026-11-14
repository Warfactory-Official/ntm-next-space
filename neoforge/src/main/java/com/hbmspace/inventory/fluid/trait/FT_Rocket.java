/*
 * SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
 * SPDX-FileCopyrightText: 2024 George Paton
 * SPDX-FileCopyrightText: 2024 JamesH_2
 * SPDX-License-Identifier: LGPL-3.0-only
 *
 * Copyright (C) 2026 movblock. All rights reserved.
 * Copyright (C) 2024 George Paton. All rights reserved.
 * Copyright (C) 2024 JamesH_2. All rights reserved.
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

package com.hbmspace.inventory.fluid.trait;

import com.hbm.inventory.fluid.trait.FluidTrait;
import com.hbm.util.NumberFormatter;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

public class FT_Rocket implements FluidTrait {

    private final int isp;
    private final long thrust;

    public FT_Rocket(int isp, long thrust) {
        this.isp = isp;
        this.thrust = thrust;
    }

    public int getISP() {
        return isp;
    }

    public long getThrust() {
        return thrust;
    }

    @Override
    public void describe(Consumer<Component> info) {
        info.accept(
                Component.literal("[")
                        .append(Component.translatable("hbmfluid.trait.rocket"))
                        .append("]")
                        .withStyle(ChatFormatting.LIGHT_PURPLE));
        if (isp > 0) {
            info.accept(
                    Component.empty()
                            .append(
                                    Component.translatable("hbmfluid.trait.provides")
                                            .withStyle(ChatFormatting.YELLOW))
                            .append(Component.literal(" ").withStyle(ChatFormatting.YELLOW))
                            .append(
                                    Component.translatable(
                                                    "hbmfluid.trait.rocket.isp",
                                                    NumberFormatter.formatCompact(isp))
                                            .withStyle(ChatFormatting.RED))
                            .append(Component.literal(" ").withStyle(ChatFormatting.YELLOW))
                            .append(
                                    Component.translatable("hbmfluid.trait.perBucket")
                                            .withStyle(ChatFormatting.YELLOW)));
        }
        info.accept(
                Component.literal("[")
                        .append(Component.translatable("hbmfluid.trait.thrust"))
                        .append("]")
                        .withStyle(ChatFormatting.RED));
        if (thrust > 0) {
            info.accept(
                    Component.empty()
                            .append(
                                    Component.translatable("hbmfluid.trait.provides")
                                            .withStyle(ChatFormatting.YELLOW))
                            .append(Component.literal(" ").withStyle(ChatFormatting.YELLOW))
                            .append(
                                    Component.translatable(
                                                    "hbmfluid.trait.thrust.newtons",
                                                    NumberFormatter.formatCompact(thrust))
                                            .withStyle(ChatFormatting.RED))
                            .append(Component.literal(" ").withStyle(ChatFormatting.YELLOW))
                            .append(
                                    Component.translatable("hbmfluid.trait.thrust.perBucket")
                                            .withStyle(ChatFormatting.YELLOW)));
        }
    }
}
