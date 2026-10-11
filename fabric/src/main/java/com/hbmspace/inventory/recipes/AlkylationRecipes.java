// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.inventory.recipes;

import com.hbm.inventory.recipes.loader.SimpleGenericRecipes;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.material.Fluid;
import org.jspecify.annotations.Nullable;

public class AlkylationRecipes extends SimpleGenericRecipes<AlkylationRecipe> {

    public static final AlkylationRecipes INSTANCE = new AlkylationRecipes();

    public static final int FEED = 100;

    @Override
    protected String registryName() {
        return "hbmspace:alkylation";
    }

    @Override
    protected RecipeSerializer<AlkylationRecipe> serializer() {
        return AlkylationRecipe.SERIALIZER;
    }

    public @Nullable AlkylationRecipe getOutput(@Nullable Fluid feed) {
        return byInputFluid().findLast(feed);
    }
}
