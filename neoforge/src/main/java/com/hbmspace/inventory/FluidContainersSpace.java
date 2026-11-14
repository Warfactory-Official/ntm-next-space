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

package com.hbmspace.inventory;

import com.hbm.capability.FluidContainerRows.Row;
import com.hbm.capability.FluidContainerRows;
import com.hbm.config.BalanceConfig;
import com.hbm.inventory.fluid.NTMFluids;
import com.hbm.items.ModItems;
import com.hbmspace.blocks.ModBlocksSpace;
import com.hbmspace.blocks.ore.CelestialOre;
import com.hbmspace.dim.SolarSystem.Body;
import com.hbmspace.inventory.fluid.FluidsSpace;
import com.hbmspace.items.FoodItemsSpace;
import java.util.function.IntSupplier;
import java.util.function.Supplier;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.material.Fluid;
import org.jspecify.annotations.Nullable;

public final class FluidContainersSpace {

    static {
        for (Body body : CelestialOre.OIL.bodies()) {
            row(() -> CelestialOre.OIL.block(body), null, () -> NTMFluids.OIL, () -> 250);
        }
        row(CelestialOre.GAS::host, null, () -> NTMFluids.GAS, () -> 100);
        for (Body body : CelestialOre.GAS.bodies()) {
            row(() -> CelestialOre.GAS.block(body), null, () -> NTMFluids.GAS, () -> 100);
        }
        IntSupplier shale = () -> BalanceConfig.enable528 ? 50 : 250;
        row(CelestialOre.SHALE::host, null, () -> NTMFluids.GAS, shale);
        for (Body body : CelestialOre.SHALE.bodies()) {
            row(() -> CelestialOre.SHALE.block(body), null, () -> NTMFluids.GAS, shale);
        }
        row(
                FoodItemsSpace.GLASS_SMILK::get,
                FoodItemsSpace.GLASS_EMPTY::get,
                FluidsSpace::smilk,
                () -> 100);
        row(ModItems.COFFEE::get, FoodItemsSpace.CMUG_EMPTY::get, FluidsSpace::coffee, () -> 100);
        row(
                FoodItemsSpace.TEACUP::get,
                FoodItemsSpace.TEACUP_EMPTY::get,
                FluidsSpace::tea,
                () -> 100);
        row(
                FoodItemsSpace.BOTTLE_HONEY::get,
                () -> Items.GLASS_BOTTLE,
                FluidsSpace::honey,
                () -> 100);
        row(ModBlocksSpace.DRY_ICE::get, null, () -> NTMFluids.CARBONDIOXIDE, () -> 1000);
    }

    private FluidContainersSpace() {}

    private static void row(
            Supplier<? extends ItemLike> full,
            @Nullable Supplier<? extends ItemLike> empty,
            Supplier<Fluid> fluid,
            IntSupplier amount) {
        FluidContainerRows.addon(new Row(full, null, empty, fluid, amount));
    }
}
