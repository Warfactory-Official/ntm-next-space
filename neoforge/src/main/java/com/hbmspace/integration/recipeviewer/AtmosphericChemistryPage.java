// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.integration.recipeviewer;

import com.hbm.integration.recipeviewer.GenericMachinePage;
import com.hbm.integration.recipeviewer.PageLayout;
import com.hbm.integration.recipeviewer.RecipePage;
import com.hbm.integration.recipeviewer.RecipePanel;
import com.hbm.inventory.fluid.FluidStackNTM;
import com.hbmspace.HbmSpace;
import com.hbmspace.blocks.AtmosphereBlocksSpace;
import com.hbmspace.inventory.recipes.AtmosphereRecipe;
import com.hbmspace.inventory.recipes.AtmosphereRecipes;
import java.util.List;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

public final class AtmosphericChemistryPage extends RecipePage<AtmosphereRecipe> {

    public AtmosphericChemistryPage() {
        super(
                Identifier.fromNamespaceAndPath(HbmSpace.MOD_ID, "atmospheric_chemistry"),
                AtmosphereRecipe.class);
    }

    @Override
    public List<AtmosphereRecipe> rows() {
        return AtmosphereRecipes.INSTANCE.recipes();
    }

    @Override
    public Identifier rowId(AtmosphereRecipe row) {
        return row.recipeId().identifier();
    }

    @Override
    public Component title() {
        return Component.translatable("jei.hbmspace.atmospheric_chemistry");
    }

    @Override
    public ItemStack icon() {
        return new ItemStack(AtmosphereBlocksSpace.MACHINE_ATMO_EMITTER.get());
    }

    @Override
    public List<ItemStack> catalysts() {
        return List.of(icon());
    }

    @Override
    public void layout(AtmosphereRecipe recipe, PageLayout page) {
        int[][] inputs = GenericMachinePage.universalInputPositions(recipe.inputFluid.length);
        for (int i = 0; i < inputs.length; i++) {
            FluidStackNTM gas = recipe.inputFluid[i];
            page.input(inputs[i][0], inputs[i][1]).background().fluid(gas);
        }
        int[][] outputs = GenericMachinePage.outputPositions(recipe.outputFluid.length);
        for (int i = 0; i < outputs.length; i++) {
            page.output(outputs[i][0], outputs[i][1]).background().fluid(recipe.outputFluid[i]);
        }
        page.catalyst(75, 31).item(AtmosphereBlocksSpace.MACHINE_ATMO_EMITTER.get());
    }

    @Override
    public void draw(AtmosphereRecipe recipe, GuiGraphicsExtractor graphics) {
        RecipePanel.universalPage(graphics);
    }
}
