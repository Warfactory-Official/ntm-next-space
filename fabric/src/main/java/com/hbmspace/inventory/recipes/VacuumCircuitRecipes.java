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

package com.hbmspace.inventory.recipes;

import com.hbm.inventory.recipes.ingredient.CountIngredient;
import com.hbm.inventory.recipes.ingredient.IngredientGroup;
import com.hbm.inventory.recipes.loader.GenericRecipes;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.Container;
import net.minecraft.world.item.crafting.RecipeSerializer;
import org.jspecify.annotations.Nullable;

public class VacuumCircuitRecipes
        extends GenericRecipes<VacuumCircuitRecipe, VacuumCircuitRecipes.Index> {

    public static final VacuumCircuitRecipes INSTANCE = new VacuumCircuitRecipes();

    public static List<CountIngredient> wafer() {
        return INSTANCE.index().wafer();
    }

    public static List<CountIngredient> pcb() {
        return INSTANCE.index().pcb();
    }

    @Override
    protected String registryName() {
        return "hbmspace:vacuum_circuit";
    }

    @Override
    protected RecipeSerializer<VacuumCircuitRecipe> serializer() {
        return VacuumCircuitRecipe.SERIALIZER;
    }

    @Override
    protected Index indexRows(List<VacuumCircuitRecipe> rows) {
        List<CountIngredient> wafer = new ArrayList<>();
        List<CountIngredient> pcb = new ArrayList<>();
        for (VacuumCircuitRecipe recipe : rows) {
            wafer.addAll(List.of(recipe.wafer()));
            pcb.addAll(List.of(recipe.pcb));
        }
        return new Index(List.copyOf(wafer), List.copyOf(pcb));
    }

    public @Nullable VacuumCircuitRecipe getRecipe(
            Container inv, int[] waferSlots, int[] pcbSlots) {
        for (VacuumCircuitRecipe recipe : recipes()) {
            if (IngredientGroup.matches(recipe.wafer(), inv, waferSlots)
                    && IngredientGroup.matches(recipe.pcb, inv, pcbSlots)) return recipe;
        }
        return null;
    }

    public record Index(List<CountIngredient> wafer, List<CountIngredient> pcb) {}
}
