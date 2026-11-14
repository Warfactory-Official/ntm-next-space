/*
 * SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
 * SPDX-FileCopyrightText: 2024-2025 George Paton
 * SPDX-License-Identifier: LGPL-3.0-only
 *
 * Copyright (C) 2026 movblock. All rights reserved.
 * Copyright (C) 2024-2025 George Paton. All rights reserved.
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

package com.hbmspace.blocks.ore;

import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.generic.FluidReserve;
import com.hbm.items.tool.OilDetectorItem;
import com.hbmspace.HbmSpace;
import com.hbmspace.blocks.ore.OreFluidBlock.ReserveType;
import com.hbmspace.dim.SolarSystem.Body;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import org.jspecify.annotations.Nullable;

public final class ReservesSpace {
    public static final Reserve KERBIN_OIL =
            new Reserve(
                    ReserveType.OIL,
                    Body.KERBIN,
                    ModBlocks.ORE_OIL_EMPTY::get,
                    CelestialOre.OIL_EMPTY);
    public static final Reserve KERBIN_BEDROCK_OIL =
            new Reserve(ReserveType.OIL, Body.KERBIN, null, null);

    private static final int[][] COLUMNS = {
        {5, 0}, {-5, 0}, {0, 5}, {0, -5}, {10, 0}, {-10, 0}, {0, 10}, {0, -10}, {5, 5}, {-5, 5},
        {5, -5}, {-5, -5}
    };

    private ReservesSpace() {}

    static void install() {
        FluidReserve.RESERVE_OF.install(
                HbmSpace.MOD_ID,
                block -> {
                    Reserve reserve = reserve(block);
                    return reserve != null
                            ? reserve
                            : block instanceof FluidReserve other ? other : null;
                });
        OilDetectorItem.READING.install(
                HbmSpace.MOD_ID,
                (level, player) ->
                        reading(
                                level,
                                Mth.floor(player.getX()),
                                Mth.floor(player.getY()),
                                Mth.floor(player.getZ())));
    }

    private static @Nullable Reserve reserve(Block block) {
        if (block == ModBlocks.ORE_OIL.get()) return KERBIN_OIL;
        if (block == ModBlocks.ORE_BEDROCK_OIL.get()) return KERBIN_BEDROCK_OIL;
        return block instanceof OreFluidBlock ore ? ore.reserve : null;
    }

    public static Component reading(Level level, int x, int y, int z) {
        Reserve found = column(level, x, y, z);
        if (found != null) return message(found, "bullseye", ChatFormatting.DARK_GREEN);
        for (int[] column : COLUMNS) {
            found = column(level, x + column[0], y, z + column[1]);
            if (found != null) return message(found, "detected", ChatFormatting.GOLD);
        }
        return Component.translatable("item.hbm.oil_detector.noOil").withStyle(ChatFormatting.RED);
    }

    private static @Nullable Reserve column(Level level, int x, int y, int z) {
        for (int i = y + 15; i > 5; i--) {
            Reserve reserve = reserve(level.getBlockState(new BlockPos(x, i, z)).getBlock());
            if (reserve != null) return reserve;
        }
        return null;
    }

    private static Component message(Reserve reserve, String kind, ChatFormatting color) {
        String suffix = reserve.detectorSuffix();
        String key =
                suffix.isEmpty()
                        ? "item.hbm.oil_detector." + kind
                        : "item.hbmspace.oil_detector." + kind + suffix;
        return Component.translatable(key).withStyle(color);
    }
}
