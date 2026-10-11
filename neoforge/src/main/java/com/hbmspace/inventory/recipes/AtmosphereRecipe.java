// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.inventory.recipes;

import com.hbm.inventory.recipes.loader.GenericRecipe;
import com.hbm.inventory.recipes.loader.GenericRecipes;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.RecipeSerializer;

public class AtmosphereRecipe extends GenericRecipe {

    public static final MapCodec<AtmosphereRecipe> MAP_CODEC =
            GenericRecipe.mapCodec(AtmosphereRecipe::new)
                    .validate(
                            row ->
                                    row.inputFluid.length == 0 || row.outputFluid.length != 1
                                            ? DataResult.error(
                                                    () ->
                                                            "an atmospheric reaction takes gases into exactly one gas")
                                            : DataResult.success(row));
    public static final StreamCodec<RegistryFriendlyByteBuf, AtmosphereRecipe> STREAM_CODEC =
            GenericRecipe.streamCodec(AtmosphereRecipe::new);
    public static final RecipeSerializer<AtmosphereRecipe> SERIALIZER =
            new RecipeSerializer<>(MAP_CODEC, STREAM_CODEC);

    public AtmosphereRecipe(String name) {
        super(name);
    }

    @Override
    public GenericRecipes<?, ?> table() {
        return AtmosphereRecipes.INSTANCE;
    }
}
