// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.inventory.recipes;

import com.hbm.inventory.recipes.loader.SimpleGenericRecipes;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.material.Fluid;
import org.jspecify.annotations.Nullable;

public class CryoRecipes extends SimpleGenericRecipes<CryoRecipe> {

    public static final CryoRecipes INSTANCE = new CryoRecipes();

    public static final int FEED = 100;

    @Override
    protected String registryName() {
        return "hbmspace:cryo_distill";
    }

    @Override
    protected RecipeSerializer<CryoRecipe> serializer() {
        return CryoRecipe.SERIALIZER;
    }

    public @Nullable CryoRecipe getOutput(@Nullable Fluid feed) {
        return byInputFluid().findLast(feed);
    }
}
