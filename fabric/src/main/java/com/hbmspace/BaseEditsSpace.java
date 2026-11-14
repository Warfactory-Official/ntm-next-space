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

package com.hbmspace;

import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.generic.BobbleBlock.BobbleType;
import com.hbm.blocks.generic.BobbleBlock;
import com.hbm.handler.BobmazonOffer;
import com.hbm.handler.BobmazonOffers;
import com.hbm.handler.BobmazonRequirement;
import com.hbm.items.CreativeContents.Tab;
import com.hbm.items.CreativeContents;
import com.hbm.items.ModItems;
import com.hbm.items.machine.MoldItem;
import com.hbm.lib.CapeRoster;
import com.hbm.registration.Registrar;
import com.hbm.util.RegistryUtil;
import com.hbmspace.blocks.TrinketsSpace;
import com.hbmspace.blocks.bomb.BombPlacementLog;
import com.hbmspace.blocks.generic.BobbleTypeSpace;
import com.hbmspace.blocks.machine.PolymerSawmillSpace;
import com.hbmspace.blocks.machine.SpaceFurnaceSpeeds;
import com.hbmspace.explosion.AntimatterEditsSpace;
import com.hbmspace.inventory.material.MatsSpace;
import com.hbmspace.items.FoodItemsSpace;
import com.hbmspace.items.PartsItemsSpace;
import java.util.UUID;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.component.UseRemainder;
import net.minecraft.world.level.ItemLike;

final class BaseEditsSpace {

    private BaseEditsSpace() {}

    static void install() {
        SpaceFurnaceSpeeds.install();
        PolymerSawmillSpace.install();
        food(
                ModItems.BIO_WAFER,
                new FoodProperties.Builder().nutrition(4).saturationModifier(0.6F));
        food(ModItems.FOODITEM, new FoodProperties.Builder().nutrition(2).saturationModifier(0.6F));
        food(ModItems.PUDDING, new FoodProperties.Builder().nutrition(6).saturationModifier(0.6F));
        food(
                ModItems.PANCAKE,
                new FoodProperties.Builder().nutrition(20).saturationModifier(1.0F).alwaysEdible());
        Registrar.modifyDefaultComponents(
                ModItems.COFFEE,
                components ->
                        components.set(
                                DataComponents.USE_REMAINDER,
                                new UseRemainder(
                                        new ItemStackTemplate(FoodItemsSpace.CMUG_EMPTY.get()))));

        CreativeContents.remove(Tab.CONSUMABLES, RegistryUtil.id("bottled_cloud"));
        AntimatterEditsSpace.install();

        MoldItem.MoldMulti.named("blade")
                .addon(MatsSpace.MAT_STAINLESS, PartsItemsSpace.BLADE_SYNGAS.id());
        BobmazonOffers.addonSpecial(
                stack -> stack.is(ModItems.CRUCIBLE.get()),
                () ->
                        new BobmazonOffer(
                                new ItemStack(ModItems.PELLET_CHARGED.get()),
                                BobmazonRequirement.BLAST_FURNACE,
                                16));
        CapeRoster.addon(
                UUID.fromString("85d54b8c-1862-4c86-b351-5ef1b998aa32"),
                Identifier.fromNamespaceAndPath(HbmSpace.MOD_ID, "models/capes/capegwen"));
        CapeRoster.addon(
                UUID.fromString("99b9efa5-8c38-4b22-86c2-baf97789d2fb"),
                Identifier.fromNamespaceAndPath(HbmSpace.MOD_ID, "models/capes/capemellow"));
        BombPlacementLog.install();

        TrinketsSpace.PLUSHIES.joins(ModBlocks.PLUSHIES);
        TrinketsSpace.BOBBLEHEADS.joins(ModBlocks.BOBBLEHEADS);
        BobbleBlock.addonContribution(
                BobbleType.PEEP, "Coilgun, Leadburster, Congo Lake models, and the 737");
        BobbleBlock.addonContribution(
                BobbleType.MELLOW,
                "Celestial mechanics, rocketry,$atmospheric chemistry, orbital stations,$Dyson swarms");
        BobbleBlock.TYPES.moveAfter(BobbleTypeSpace.JAMESH_2, BobbleType.PEEP);
        CreativeContents.remove(Tab.BLOCKS, RegistryUtil.id("bobblehead_peep"));
        CreativeContents.insertBefore(
                Tab.BLOCKS, RegistryUtil.id("bobblehead_microwave"), BobbleType.PEEP::stack);

        CreativeContents.remove(Tab.BLOCKS, RegistryUtil.id("sellafield_bedrock"));
        CreativeContents.remove(Tab.BLOCKS, RegistryUtil.id("ore_sellafield_diamond"));
        CreativeContents.insertAfter(
                Tab.BLOCKS,
                RegistryUtil.id("sellafield_slaked"),
                () -> new ItemStack(ModBlocks.ORE_SELLAFIELD_DIAMOND.get()));
        CreativeContents.insertAfter(
                Tab.BLOCKS,
                RegistryUtil.id("ore_sellafield_diamond"),
                () -> new ItemStack(ModBlocks.SELLAFIELD_BEDROCK.get()));
    }

    private static void food(ItemLike item, FoodProperties.Builder food) {
        Registrar.modifyDefaultComponents(
                item, components -> components.set(DataComponents.FOOD, food.build()));
    }
}
