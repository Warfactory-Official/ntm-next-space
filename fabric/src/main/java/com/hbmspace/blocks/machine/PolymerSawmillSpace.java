// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.blocks.machine;

import com.hbm.items.ModItems;
import com.hbmspace.blocks.FloraBlocksSpace;
import com.hbmspace.blocks.TerrainBlocksSpace;
import com.hbmspace.items.PartsItemsSpace;
import java.util.List;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public final class PolymerSawmillSpace {
    private PolymerSawmillSpace() {}

    public static boolean stick(ItemStack stack) {
        return stack.is(PartsItemsSpace.STICK_VINYL.get())
                || stack.is(PartsItemsSpace.STICK_PVC.get());
    }

    public static boolean polymer(ItemStack stack) {
        return stick(stack)
                || stack.is(TerrainBlocksSpace.VINYL_PLANKS.get().asItem())
                || stack.is(TerrainBlocksSpace.PVC_PLANKS.get().asItem())
                || stack.is(FloraBlocksSpace.VINYL_LOG.get().asItem())
                || stack.is(FloraBlocksSpace.PVC_LOG.get().asItem());
    }

    public static @Nullable ItemStack output(ItemStack input) {
        if (stick(input)) return new ItemStack(ModItems.DUST.get());
        if (input.is(TerrainBlocksSpace.VINYL_PLANKS.get().asItem()))
            return new ItemStack(PartsItemsSpace.STICK_VINYL.get(), 6);
        if (input.is(TerrainBlocksSpace.PVC_PLANKS.get().asItem()))
            return new ItemStack(PartsItemsSpace.STICK_PVC.get(), 6);
        if (input.is(FloraBlocksSpace.VINYL_LOG.get().asItem()))
            return new ItemStack(TerrainBlocksSpace.VINYL_PLANKS.get(), 6);
        if (input.is(FloraBlocksSpace.PVC_LOG.get().asItem()))
            return new ItemStack(TerrainBlocksSpace.PVC_PLANKS.get(), 6);
        return null;
    }

    public static List<Item> inputs() {
        return List.of(
                FloraBlocksSpace.VINYL_LOG.get().asItem(),
                FloraBlocksSpace.PVC_LOG.get().asItem(),
                PartsItemsSpace.STICK_VINYL.get(),
                PartsItemsSpace.STICK_PVC.get(),
                TerrainBlocksSpace.VINYL_PLANKS.get().asItem(),
                TerrainBlocksSpace.PVC_PLANKS.get().asItem());
    }
}
