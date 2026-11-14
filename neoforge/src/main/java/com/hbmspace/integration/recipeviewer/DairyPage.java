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

package com.hbmspace.integration.recipeviewer;

import com.hbm.integration.recipeviewer.PageIds;
import com.hbm.integration.recipeviewer.PageLayout;
import com.hbm.integration.recipeviewer.RecipePage;
import com.hbm.integration.recipeviewer.RecipePanel;
import com.hbmspace.HbmSpace;
import com.hbmspace.blockentity.machine.MilkReformerBlockEntity;
import com.hbmspace.blocks.ModBlocksSpace;
import com.hbmspace.inventory.fluid.FluidsSpace;
import java.util.List;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;

public final class DairyPage extends RecipePage<DairyPage.Recipe> {

    public DairyPage() {
        super(Identifier.fromNamespaceAndPath(HbmSpace.MOD_ID, "dairy"), Recipe.class);
    }

    @Override
    public List<Recipe> rows() {
        int[] out = MilkReformerBlockEntity.OUTPUTS;
        return List.of(
                new Recipe(
                        PageIds.derived(id(), "milk"),
                        FluidsSpace.milk(),
                        MilkReformerBlockEntity.INPUT,
                        List.of(FluidsSpace.emilk(), FluidsSpace.cmilk(), FluidsSpace.cream()),
                        out));
    }

    @Override
    public Identifier rowId(Recipe row) {
        return row.id();
    }

    @Override
    public Component title() {
        return ModBlocksSpace.MACHINE_MILK_REFORMER.get().getName();
    }

    @Override
    public ItemStack icon() {
        return new ItemStack(ModBlocksSpace.MACHINE_MILK_REFORMER.get());
    }

    @Override
    public List<ItemStack> catalysts() {
        return List.of(icon());
    }

    @Override
    public void layout(Recipe recipe, PageLayout page) {
        page.input(48, 24).background().fluid(recipe.input(), recipe.inputAmount());
        for (int i = 0; i < recipe.outputs().size(); i++) {
            page.output(102 + i * 18, 24)
                    .background()
                    .fluid(recipe.outputs().get(i), recipe.outputAmounts()[i]);
        }
        page.catalyst(75, 31).item(ModBlocksSpace.MACHINE_MILK_REFORMER.get());
    }

    @Override
    public void draw(Recipe recipe, GuiGraphicsExtractor graphics) {
        RecipePanel.universalPage(graphics);
    }

    public record Recipe(
            Identifier id,
            Fluid input,
            int inputAmount,
            List<Fluid> outputs,
            int[] outputAmounts) {}
}
