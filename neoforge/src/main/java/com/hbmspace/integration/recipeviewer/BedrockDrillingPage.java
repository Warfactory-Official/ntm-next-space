// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.integration.recipeviewer;

import com.hbm.blocks.ModBlocks;
import com.hbm.integration.recipeviewer.GenericMachinePage;
import com.hbm.integration.recipeviewer.PageIds;
import com.hbm.integration.recipeviewer.PageLayout;
import com.hbm.integration.recipeviewer.RecipePage;
import com.hbmspace.HbmSpace;
import com.hbmspace.dim.CelestialBody;
import com.hbmspace.dim.SolarSystem;
import com.hbmspace.items.special.CelestialBedrockOre;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class BedrockDrillingPage extends RecipePage<BedrockDrillingPage.Recipe> {

    public BedrockDrillingPage() {
        super(Identifier.fromNamespaceAndPath(HbmSpace.MOD_ID, "bedrock_drilling"), Recipe.class);
    }

    @Override
    public List<Recipe> rows() {
        List<Recipe> rows = new ArrayList<>();
        for (SolarSystem.Body type : SolarSystem.Body.values()) {
            CelestialBody body = type.getBody();
            if (body == null) continue;
            List<ItemStack> outputs = new ArrayList<>();
            if (CelestialBedrockOre.get(type) != null)
                outputs.add(CelestialBedrockOre.sample(type));
            if (body.hasIce) outputs.add(new ItemStack(Items.PACKED_ICE, 32));
            rows.add(new Recipe(PageIds.derived(id(), body.name), body, outputs));
        }
        return rows;
    }

    @Override
    public Identifier rowId(Recipe row) {
        return row.id();
    }

    @Override
    public Component title() {
        return Component.translatable("jei.hbmspace.bedrock_drilling");
    }

    @Override
    public ItemStack icon() {
        return new ItemStack(ModBlocks.MACHINE_EXCAVATOR.get());
    }

    @Override
    public List<ItemStack> catalysts() {
        return List.of(icon());
    }

    @Override
    public void layout(Recipe recipe, PageLayout page) {
        int[][] outputs = GenericMachinePage.outputPositions(recipe.outputs().size());
        for (int i = 0; i < outputs.length; i++) {
            page.output(outputs[i][0], outputs[i][1]).background().item(recipe.outputs().get(i));
        }
        page.catalyst(75, 31).item(ModBlocks.MACHINE_EXCAVATOR.get());
    }

    @Override
    public void draw(Recipe recipe, GuiGraphicsExtractor graphics) {
        AtmosphereExtractionPage.drawBody(graphics, recipe.body());
    }

    public record Recipe(Identifier id, CelestialBody body, List<ItemStack> outputs) {}
}
