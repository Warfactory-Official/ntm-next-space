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

import com.hbm.integration.recipeviewer.GenericMachinePage;
import com.hbm.integration.recipeviewer.PageIds;
import com.hbm.integration.recipeviewer.PageLayout;
import com.hbm.integration.recipeviewer.RecipePage;
import com.hbm.integration.recipeviewer.RecipePanel;
import com.hbm.util.GameTime;
import com.hbmspace.HbmSpace;
import com.hbmspace.blocks.AtmosphereBlocksSpace;
import com.hbmspace.dim.CelestialBody;
import com.hbmspace.dim.SolarSystem;
import com.hbmspace.dim.trait.CBT_Atmosphere;
import com.hbmspace.handler.atmosphere.AtmosphereText;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;

public final class AtmosphereExtractionPage extends RecipePage<AtmosphereExtractionPage.Recipe> {

    public AtmosphereExtractionPage() {
        super(
                Identifier.fromNamespaceAndPath(HbmSpace.MOD_ID, "atmosphere_extraction"),
                Recipe.class);
    }

    @Override
    public List<Recipe> rows() {
        List<Recipe> rows = new ArrayList<>();
        for (SolarSystem.Body type : SolarSystem.Body.values()) {
            CelestialBody body = type.getBody();
            if (body == null) continue;
            CBT_Atmosphere atmosphere = body.getDefaultTrait(CBT_Atmosphere.class);
            if (atmosphere == null) continue;

            List<Fluid> fluids = new ArrayList<>();
            List<Double> pressures = new ArrayList<>();
            for (CBT_Atmosphere.FluidEntry entry : atmosphere.fluids) {
                fluids.add(entry.fluid);
                pressures.add(entry.pressure);
            }
            rows.add(new Recipe(PageIds.derived(id(), body.name), body, fluids, pressures));
        }
        return rows;
    }

    @Override
    public Identifier rowId(Recipe row) {
        return row.id();
    }

    @Override
    public Component title() {
        return Component.translatable("jei.hbmspace.atmosphere_extraction");
    }

    @Override
    public ItemStack icon() {
        return new ItemStack(AtmosphereBlocksSpace.MACHINE_ATMO_VENT.get());
    }

    @Override
    public List<ItemStack> catalysts() {
        return List.of(icon());
    }

    @Override
    public void layout(Recipe recipe, PageLayout page) {
        int[][] outputs = GenericMachinePage.outputPositions(recipe.fluids().size());
        for (int i = 0; i < outputs.length; i++) {
            String atm = AtmosphereText.roundDecimal(recipe.pressures().get(i), 3) + "atm";
            page.output(outputs[i][0], outputs[i][1])
                    .background()
                    .fluid(recipe.fluids().get(i))
                    .tooltip((shown, lines) -> lines.accept(Component.literal(atm)));
        }
        page.catalyst(75, 31).item(AtmosphereBlocksSpace.MACHINE_ATMO_VENT.get());
    }

    @Override
    public void draw(Recipe recipe, GuiGraphicsExtractor graphics) {
        drawBody(graphics, recipe.body());
    }

    static void drawBody(GuiGraphicsExtractor graphics, CelestialBody body) {
        RecipePanel.universalPage(graphics);
        float scroll = (GameTime.now() % 4000) / 4000F;
        graphics.blit(body.texture, 32, 12, 64, 44, scroll, 1 + scroll, 0F, 1F);
        RecipePanel.centred(
                graphics, Component.translatable("body." + body.name), 48, 50, 0xFF000000);
    }

    public record Recipe(
            Identifier id, CelestialBody body, List<Fluid> fluids, List<Double> pressures) {}
}
