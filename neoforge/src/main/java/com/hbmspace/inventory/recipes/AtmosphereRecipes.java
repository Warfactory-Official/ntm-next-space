// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.inventory.recipes;

import com.hbm.inventory.recipes.loader.SimpleGenericRecipes;
import net.minecraft.world.item.crafting.RecipeSerializer;

public class AtmosphereRecipes extends SimpleGenericRecipes<AtmosphereRecipe> {

    public static final AtmosphereRecipes INSTANCE = new AtmosphereRecipes();

    public static final int SCALE = 64;

    @Override
    protected String registryName() {
        return "hbmspace:atmosphere";
    }

    @Override
    protected RecipeSerializer<AtmosphereRecipe> serializer() {
        return AtmosphereRecipe.SERIALIZER;
    }
}
