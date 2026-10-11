// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.integration.recipeviewer;

import com.hbm.integration.recipeviewer.GenericMachinePage;
import com.hbm.integration.recipeviewer.PageLayout;
import com.hbm.integration.recipeviewer.RecipePage;
import com.hbm.integration.recipeviewer.RecipePanel;
import com.hbm.inventory.recipes.ingredient.CountIngredient;
import com.hbmspace.HbmSpace;
import com.hbmspace.blocks.IndustryBlocksSpace;
import com.hbmspace.inventory.recipes.VacuumCircuitRecipe;
import com.hbmspace.inventory.recipes.VacuumCircuitRecipes;
import java.util.List;
import java.util.Locale;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

public final class VacuumCircuitPage extends RecipePage<VacuumCircuitRecipe> {

    public VacuumCircuitPage() {
        super(
                Identifier.fromNamespaceAndPath(HbmSpace.MOD_ID, "vacuum_circuit"),
                VacuumCircuitRecipe.class);
    }

    @Override
    public List<VacuumCircuitRecipe> rows() {
        return VacuumCircuitRecipes.INSTANCE.recipes();
    }

    @Override
    public Identifier rowId(VacuumCircuitRecipe row) {
        return row.recipeId().identifier();
    }

    @Override
    public Component title() {
        return Component.translatable(
                IndustryBlocksSpace.MACHINE_VACUUM_CIRCUIT.get().getDescriptionId());
    }

    @Override
    public ItemStack icon() {
        return new ItemStack(IndustryBlocksSpace.MACHINE_VACUUM_CIRCUIT.get());
    }

    @Override
    public List<ItemStack> catalysts() {
        return List.of(icon());
    }

    @Override
    public void layout(VacuumCircuitRecipe recipe, PageLayout page) {
        CountIngredient[] wafer = recipe.wafer();
        int[][] positions =
                GenericMachinePage.universalInputPositions(wafer.length + recipe.pcb.length);
        int slot = 0;
        for (CountIngredient input : wafer) {
            page.input(positions[slot][0], positions[slot][1])
                    .background()
                    .items(input.displayStacks());
            slot++;
        }
        for (CountIngredient input : recipe.pcb) {
            page.input(positions[slot][0], positions[slot][1])
                    .background()
                    .items(input.displayStacks());
            slot++;
        }
        page.output(102, 24).background().item(recipe.output());
        page.catalyst(75, 31).item(IndustryBlocksSpace.MACHINE_VACUUM_CIRCUIT.get());
    }

    @Override
    public void draw(VacuumCircuitRecipe recipe, GuiGraphicsExtractor graphics) {
        RecipePanel.universalPage(graphics);
        RecipePanel.rightAligned(
                graphics,
                Component.translatable(
                        "jei.hbm.duration", String.format(Locale.US, "%,d", recipe.duration)),
                160,
                43,
                RecipePanel.TEXT);
        RecipePanel.rightAligned(
                graphics,
                Component.translatable(
                        "jei.hbm.consumption_padded",
                        String.format(Locale.US, "%,d", recipe.power)),
                160,
                55,
                RecipePanel.TEXT);
    }
}
