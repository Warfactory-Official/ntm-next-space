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
import com.hbmspace.inventory.recipes.CryoRecipe;
import com.hbmspace.inventory.recipes.CryoRecipes;
import java.util.List;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

public final class CryoPage extends RecipePage<CryoRecipe> {

    private static final int SHOWN_STEPS = 10;

    public CryoPage() {
        super(Identifier.fromNamespaceAndPath(HbmSpace.MOD_ID, "cryo_distill"), CryoRecipe.class);
    }

    @Override
    public List<CryoRecipe> rows() {
        return CryoRecipes.INSTANCE.recipes();
    }

    @Override
    public Identifier rowId(CryoRecipe row) {
        return row.recipeId().identifier();
    }

    @Override
    public Component title() {
        return Component.translatable("jei.hbmspace.cryo_distill");
    }

    @Override
    public ItemStack icon() {
        return new ItemStack(IndustryBlocksSpace.MACHINE_CRYO_DISTILL.get());
    }

    @Override
    public List<ItemStack> catalysts() {
        return List.of(icon());
    }

    @Override
    public void layout(CryoRecipe recipe, PageLayout page) {
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
        page.catalyst(75, 31).item(IndustryBlocksSpace.MACHINE_CRYO_DISTILL.get());
    }

    private static FluidStackNTM shown(FluidStackNTM step) {
        return new FluidStackNTM(step.type(), step.amount() * SHOWN_STEPS, step.pressure());
    }

    @Override
    public void draw(CryoRecipe recipe, GuiGraphicsExtractor graphics) {
        RecipePanel.universalPage(graphics);
    }
}
