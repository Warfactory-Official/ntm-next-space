/*
 * SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
 * SPDX-FileCopyrightText: 2025-2026 George Paton
 * SPDX-License-Identifier: LGPL-3.0-only
 *
 * Copyright (C) 2026 movblock. All rights reserved.
 * Copyright (C) 2025-2026 George Paton. All rights reserved.
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

package com.hbmspace.items.tool;

import com.hbm.inventory.fluid.NTMFluidProperties;
import com.hbm.packet.PacketWire;
import com.hbm.packet.layout.PlayerInformLayout;
import com.hbm.util.TickPhase;
import com.hbmspace.dim.trait.CBT_Atmosphere;
import com.hbmspace.handler.atmosphere.AtmosphereText;
import com.hbmspace.handler.atmosphere.ChunkAtmosphereManager;
import java.util.Locale;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public class AtmosphereScannerItem extends Item {
    public static final int INFO_ID = 969;
    private static final int MILLIS = 4_000;

    public AtmosphereScannerItem(Properties props) {
        super(props);
    }

    @Override
    public void inventoryTick(
            ItemStack stack, ServerLevel level, Entity owner, @Nullable EquipmentSlot slot) {
        if (!(owner instanceof ServerPlayer player) || !TickPhase.every(owner, 5)) return;

        CBT_Atmosphere atmosphere = ChunkAtmosphereManager.proxy.getAtmosphere(player);

        boolean hasAtmosphere = false;
        if (atmosphere != null) {
            for (int i = 0; i < atmosphere.fluids.size(); i++) {
                CBT_Atmosphere.FluidEntry entry = atmosphere.fluids.get(i);
                if (entry.pressure > 0.0001) {
                    String pressure =
                            String.format(
                                    Locale.ROOT,
                                    "%.4f",
                                    AtmosphereText.roundDecimal(entry.pressure, 4));
                    Component line =
                            NTMFluidProperties.getDisplayName(entry.fluid)
                                    .copy()
                                    .withStyle(ChatFormatting.AQUA)
                                    .append(
                                            Component.literal(": " + pressure + "atm")
                                                    .withStyle(ChatFormatting.RESET));
                    PacketWire.sendTo(PlayerInformLayout.encode(line, INFO_ID + i, MILLIS), player);
                    hasAtmosphere = true;
                }
            }
        }

        if (!hasAtmosphere) {
            Component vacuum =
                    Component.translatable("desc.hbmspace.atmosphereScanner.nearVacuum")
                            .withStyle(ChatFormatting.YELLOW);
            PacketWire.sendTo(PlayerInformLayout.encode(vacuum, INFO_ID, MILLIS), player);
        }
    }
}
