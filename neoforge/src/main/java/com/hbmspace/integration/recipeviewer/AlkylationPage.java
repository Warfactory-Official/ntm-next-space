// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.integration.recipeviewer;

import com.hbm.integration.recipeviewer.GenericMachinePage;
import com.hbm.integration.recipeviewer.PageLayout;
import com.hbm.integration.recipeviewer.RecipePage;
import com.hbm.integration.recipeviewer.RecipePanel;
import com.hbm.inventory.fluid.FluidStackNTM;
import com.hbmspace.HbmSpace;
import com.hbmspace.blocks.IndustryBlocksSpace;
import com.hbmspace.inventory.recipes.AlkylationRecipe;
import com.hbmspace.inventory.recipes.AlkylationRecipes;
import java.util.List;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

public final class AlkylationPage extends RecipePage<AlkylationRecipe> {

    private static final int SHOWN_STEPS = 10;

    public AlkylationPage() {
        super(
                Identifier.fromNamespaceAndPath(HbmSpace.MOD_ID, "alkylation"),
                AlkylationRecipe.class);
    }

    @Override
    public List<AlkylationRecipe> rows() {
        return AlkylationRecipes.INSTANCE.recipes();
    }

    @Override
    public Identifier rowId(AlkylationRecipe row) {
        return row.recipeId().identifier();
    }

    @Override
    public Component title() {
        return Component.translatable("jei.hbmspace.alkylation");
    }

    @Override
    public ItemStack icon() {
        return new ItemStack(IndustryBlocksSpace.MACHINE_ALKYLATION.get());
    }

    @Override
    public List<ItemStack> catalysts() {
        return List.of(icon());
    }

    @Override
    public void layout(AlkylationRecipe recipe, PageLayout page) {
        int[][] inputs = GenericMachinePage.universalInputPositions(recipe.inputFluid.length);
        for (int i = 0; i < inputs.length; i++) {
            page.input(inputs[i][0], inputs[i][1]).background().fluid(shown(recipe.inputFluid[i]));
        }
        int[][] outputs = GenericMachinePage.outputPositions(recipe.outputFluid.length);
        for (int i = 0; i < outputs.length; i++) {
            page.output(outputs[i][0], outputs[i][1])
                    .background()
                    .fluid(shown(recipe.outputFluid[i]));
        }
        page.catalyst(75, 31).item(IndustryBlocksSpace.MACHINE_ALKYLATION.get());
    }

    private static FluidStackNTM shown(FluidStackNTM step) {
        return new FluidStackNTM(step.type(), step.amount() * SHOWN_STEPS, step.pressure());
    }

    @Override
    public void draw(AlkylationRecipe recipe, GuiGraphicsExtractor graphics) {
        RecipePanel.universalPage(graphics);
    }
}
