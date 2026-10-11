// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.inventory.recipes;

import com.hbm.inventory.recipes.loader.GenericRecipe;
import com.hbm.inventory.recipes.loader.GenericRecipes;
import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.RecipeSerializer;

public class CryoRecipe extends GenericRecipe {

    public static final MapCodec<CryoRecipe> MAP_CODEC = GenericRecipe.mapCodec(CryoRecipe::new);
    public static final StreamCodec<RegistryFriendlyByteBuf, CryoRecipe> STREAM_CODEC =
            GenericRecipe.streamCodec(CryoRecipe::new);
    public static final RecipeSerializer<CryoRecipe> SERIALIZER =
            new RecipeSerializer<>(MAP_CODEC, STREAM_CODEC);

    public CryoRecipe(String name) {
        super(name);
    }

    @Override
    public GenericRecipes<?, ?> table() {
        return CryoRecipes.INSTANCE;
    }
}
