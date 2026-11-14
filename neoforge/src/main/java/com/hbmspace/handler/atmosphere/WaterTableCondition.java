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

package com.hbmspace.handler.atmosphere;

import com.hbm.api.environment.Environment;
import com.hbm.inventory.fluid.NTMFluidProperties;
import com.hbm.items.ModItems;
import com.hbm.items.machine.FluidContainerInfiniteItem;
import com.hbmspace.items.ModDataComponentsSpace;
import java.util.function.Consumer;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

final class WaterTableCondition implements FluidContainerInfiniteItem.Condition {

    private static boolean requiresTable(FluidContainerInfiniteItem item) {
        return item == ModItems.INF_WATER.get() || item == ModItems.INF_WATER_MK2.get();
    }

    @Override
    public boolean operates(FluidContainerInfiniteItem item, ItemStack stack) {
        return !stack.has(ModDataComponentsSpace.INCOMPATIBLE_TABLE.get());
    }

    @Override
    public void sample(FluidContainerInfiniteItem item, ItemStack stack, ServerLevel level) {
        if (!requiresTable(item)) return;
        Fluid table = Environment.groundwater(level);
        if (table == item.fluid()) {
            if (stack.has(ModDataComponentsSpace.INCOMPATIBLE_TABLE.get())) {
                stack.remove(ModDataComponentsSpace.INCOMPATIBLE_TABLE.get());
            }
            return;
        }
        Fluid shown = table == null ? Fluids.EMPTY : table;
        if (stack.get(ModDataComponentsSpace.INCOMPATIBLE_TABLE.get()) != shown) {
            stack.set(ModDataComponentsSpace.INCOMPATIBLE_TABLE.get(), shown);
        }
    }

    @Override
    public void describe(
            FluidContainerInfiniteItem item, ItemStack stack, Consumer<Component> adder) {
        Fluid current = stack.get(ModDataComponentsSpace.INCOMPATIBLE_TABLE.get());
        if (current == null) return;
        boolean dry = current == Fluids.EMPTY;
        adder.accept(
                Component.translatable(
                        dry
                                ? "desc.hbmspace.infiniteFluid.dry"
                                : "desc.hbmspace.infiniteFluid.incompatible"));
        adder.accept(
                Component.translatable(
                        "desc.hbmspace.infiniteFluid.requires",
                        NTMFluidProperties.getDisplayName(item.fluid())));
        adder.accept(
                dry
                        ? Component.translatable("desc.hbmspace.infiniteFluid.release")
                        : Component.translatable(
                                "desc.hbmspace.infiniteFluid.current",
                                NTMFluidProperties.getDisplayName(current)));
    }
}
