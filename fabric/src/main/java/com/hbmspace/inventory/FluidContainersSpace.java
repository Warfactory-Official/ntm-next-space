// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.inventory;

import com.hbm.capability.FluidContainerRows.Row;
import com.hbm.capability.FluidContainerRows;
import com.hbm.config.BalanceConfig;
import com.hbm.inventory.fluid.NTMFluids;
import com.hbm.items.ModItems;
import com.hbmspace.blocks.ModBlocksSpace;
import com.hbmspace.blocks.TerrainBlocksSpace;
import com.hbmspace.blocks.ore.CelestialOre;
import com.hbmspace.dim.SolarSystem.Body;
import com.hbmspace.inventory.fluid.FluidsSpace;
import com.hbmspace.items.FoodItemsSpace;
import com.hbmspace.items.TerrainItemsSpace;
import java.util.function.IntSupplier;
import java.util.function.Supplier;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
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

    public static ItemStack filledBucket(@Nullable Fluid content) {
        if (content == NTMFluids.MERCURY
                || content == TerrainBlocksSpace.MERCURY_FLUID.source().get()) {
            return new ItemStack(TerrainItemsSpace.BUCKET_MERCURY.get());
        }
        if (content == FluidsSpace.bromine()
                || content == TerrainBlocksSpace.BROMINE_FLUID.source().get()) {
            return new ItemStack(TerrainItemsSpace.BUCKET_BROMINE.get());
        }
        if (content == FluidsSpace.ccl()
                || content == TerrainBlocksSpace.CCL_FLUID.source().get()) {
            return new ItemStack(TerrainItemsSpace.BUCKET_CCL.get());
        }
        if (content != null && (content == FluidsSpace.milk() || content == NativeMilk.VALUE)) {
            return new ItemStack(Items.MILK_BUCKET);
        }
        return ItemStack.EMPTY;
    }

    private static final class NativeMilk {
        private static final @Nullable Fluid VALUE =
                BuiltInRegistries.FLUID
                        .getOptional(Identifier.withDefaultNamespace("milk"))
                        .orElse(null);
    }

    private static void row(
            Supplier<? extends ItemLike> full,
            @Nullable Supplier<? extends ItemLike> empty,
            Supplier<Fluid> fluid,
            IntSupplier amount) {
        FluidContainerRows.addon(new Row(full, null, empty, fluid, amount));
    }
}
