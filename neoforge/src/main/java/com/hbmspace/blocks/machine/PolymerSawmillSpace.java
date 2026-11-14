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

package com.hbmspace.blocks.machine;

import com.hbm.blockentity.machine.thermal.TimberSawBlockEntity;
import com.hbm.items.ModItems;
import com.hbmspace.HbmSpace;
import com.hbmspace.blocks.FloraBlocksSpace;
import com.hbmspace.blocks.TerrainBlocksSpace;
import com.hbmspace.items.PartsItemsSpace;
import java.util.List;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public final class PolymerSawmillSpace {
    private PolymerSawmillSpace() {}

    public static void install() {
        TimberSawBlockEntity.CUTTING.install(
                HbmSpace.MOD_ID,
                new TimberSawBlockEntity.CuttingRule() {
                    @Override
                    public @Nullable ItemStack output(ItemStack input) {
                        return PolymerSawmillSpace.output(input);
                    }

                    @Override
                    public float sawdust(ItemStack input, ItemStack result, float chance) {
                        return stick(input) || polymer(result) ? 0F : chance;
                    }
                });
    }

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
