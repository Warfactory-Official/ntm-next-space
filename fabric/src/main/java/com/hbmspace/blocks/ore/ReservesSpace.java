// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.blocks.ore;

import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.generic.FluidReserveBlock;
import com.hbm.items.tool.ItemOilDetector;
import com.hbmspace.HbmSpace;
import com.hbmspace.blocks.ore.BlockOreFluid.ReserveType;
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
        FluidReserveBlock.RESERVE_OF.install(
                HbmSpace.MOD_ID,
                block -> {
                    Reserve reserve = reserve(block);
                    return reserve != null
                            ? reserve
                            : block instanceof FluidReserveBlock other ? other : null;
                });
        ItemOilDetector.READING.install(
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
        return block instanceof BlockOreFluid ore ? ore.reserve : null;
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
