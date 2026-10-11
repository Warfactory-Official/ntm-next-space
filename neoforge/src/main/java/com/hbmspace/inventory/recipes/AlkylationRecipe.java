// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.inventory.recipes;

import com.hbm.inventory.recipes.loader.GenericRecipe;
import com.hbm.inventory.recipes.loader.GenericRecipes;
import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.RecipeSerializer;

public class AlkylationRecipe extends GenericRecipe {

    public static final MapCodec<AlkylationRecipe> MAP_CODEC =
            GenericRecipe.mapCodec(AlkylationRecipe::new);
    public static final StreamCodec<RegistryFriendlyByteBuf, AlkylationRecipe> STREAM_CODEC =
            GenericRecipe.streamCodec(AlkylationRecipe::new);
    public static final RecipeSerializer<AlkylationRecipe> SERIALIZER =
            new RecipeSerializer<>(MAP_CODEC, STREAM_CODEC);

    public AlkylationRecipe(String name) {
        super(name);
    }

    @Override
    public GenericRecipes<?, ?> table() {
        return AlkylationRecipes.INSTANCE;
    }
}
