// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.inventory.recipes;

import com.hbm.inventory.recipes.SolderingRecipes;
import com.hbm.inventory.recipes.ingredient.CountIngredient;
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
            if (SolderingRecipes.matchesGroup(inv, waferSlots, recipe.wafer())
                    && SolderingRecipes.matchesGroup(inv, pcbSlots, recipe.pcb)) return recipe;
        }
        return null;
    }

    public record Index(List<CountIngredient> wafer, List<CountIngredient> pcb) {}
}
