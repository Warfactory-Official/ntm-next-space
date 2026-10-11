// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace;

import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.generic.BlockBobble.BobbleType;
import com.hbm.blocks.generic.BlockBobble;
import com.hbm.handler.BobmazonOffer;
import com.hbm.handler.BobmazonOffers;
import com.hbm.handler.BobmazonRequirement;
import com.hbm.items.CreativeContents.Tab;
import com.hbm.items.CreativeContents;
import com.hbm.items.ModItems;
import com.hbm.items.machine.ItemMold;
import com.hbm.lib.CapeRoster;
import com.hbm.lib.Library;
import com.hbm.registration.FabricRegistrar;
import com.hbmspace.blocks.TrinketsSpace;
import com.hbmspace.blocks.bomb.BombPlacementLog;
import com.hbmspace.blocks.generic.BobbleTypeSpace;
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
        food(
                ModItems.BIO_WAFER,
                new FoodProperties.Builder().nutrition(4).saturationModifier(0.6F));
        food(ModItems.FOODITEM, new FoodProperties.Builder().nutrition(2).saturationModifier(0.6F));
        food(ModItems.PUDDING, new FoodProperties.Builder().nutrition(6).saturationModifier(0.6F));
        food(
                ModItems.PANCAKE,
                new FoodProperties.Builder().nutrition(20).saturationModifier(1.0F).alwaysEdible());
        FabricRegistrar.modifyDefaultComponents(
                ModItems.COFFEE,
                components ->
                        components.set(
                                DataComponents.USE_REMAINDER,
                                new UseRemainder(
                                        new ItemStackTemplate(FoodItemsSpace.CMUG_EMPTY.get()))));

        CreativeContents.remove(Tab.CONSUMABLES, Library.id("bottled_cloud"));
        AntimatterEditsSpace.install();

        ItemMold.MoldMulti.named("blade")
                .addon(MatsSpace.MAT_STAINLESS, PartsItemsSpace.BLADE_SYNGAS.id());
        BobmazonOffers.addonSpecial(
                stack -> stack.is(ModItems.CRUCIBLE.get()),
                () ->
                        new BobmazonOffer(
                                new ItemStack(ModItems.PELLET_CHARGED.get()),
                                BobmazonRequirement.STEEL,
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
        BlockBobble.addonContribution(
                BobbleType.PEEP, "Coilgun, Leadburster, Congo Lake models, and the 737");
        BlockBobble.addonContribution(
                BobbleType.MELLOW,
                "Celestial mechanics, rocketry,$atmospheric chemistry, orbital stations,$Dyson swarms");
        BlockBobble.TYPES.moveAfter(BobbleTypeSpace.JAMESH_2, BobbleType.PEEP);
        CreativeContents.remove(Tab.BLOCKS, Library.id("bobblehead_peep"));
        CreativeContents.insertBefore(
                Tab.BLOCKS, Library.id("bobblehead_microwave"), BobbleType.PEEP::stack);

        CreativeContents.remove(Tab.BLOCKS, Library.id("sellafield_bedrock"));
        CreativeContents.remove(Tab.BLOCKS, Library.id("ore_sellafield_diamond"));
        CreativeContents.insertAfter(
                Tab.BLOCKS,
                Library.id("sellafield_slaked"),
                () -> new ItemStack(ModBlocks.ORE_SELLAFIELD_DIAMOND.get()));
        CreativeContents.insertAfter(
                Tab.BLOCKS,
                Library.id("ore_sellafield_diamond"),
                () -> new ItemStack(ModBlocks.SELLAFIELD_BEDROCK.get()));
    }

    private static void food(ItemLike item, FoodProperties.Builder food) {
        FabricRegistrar.modifyDefaultComponents(
                item, components -> components.set(DataComponents.FOOD, food.build()));
    }
}
