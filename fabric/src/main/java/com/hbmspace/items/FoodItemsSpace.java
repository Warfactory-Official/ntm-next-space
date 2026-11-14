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

package com.hbmspace.items;

import com.hbm.extprop.LivingHazards;
import com.hbm.items.CreativeContents.Tab;
import com.hbm.items.CreativeContents;
import com.hbm.items.ModItems;
import com.hbm.items.food.ConsumableTreatments;
import com.hbm.items.food.DrinkEnergyItem;
import com.hbm.items.food.FlaskItem;
import com.hbm.items.special.CustomLoreItem;
import com.hbm.items.tool.SwordAbilityItem;
import com.hbm.items.tool.ToolTier;
import com.hbm.registration.ItemFamily;
import com.hbm.registration.Reg;
import com.hbm.util.RegistryUtil;
import com.hbmspace.HbmSpace;
import com.hbmspace.items.food.AnimanItem;
import com.hbmspace.items.food.FlaskSpaceItem;
import com.hbmspace.items.weapon.BSword;
import net.minecraft.SharedConstants;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.component.Consumables;
import net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect;

public final class FoodItemsSpace {
    public static final Reg.Handle<Item> CHOCOLATE_MINT_BILLET =
            HbmSpace.REG.item("chocolate_mint_billet", Item::new, () -> food(5, 5F));
    public static final Reg.Handle<AnimanItem> ANIMAN =
            HbmSpace.REG.item(
                    "animan",
                    AnimanItem::new,
                    () ->
                            new Item.Properties()
                                    .food(
                                            ConsumableTreatments.MEDICINE_FOOD,
                                            ConsumableTreatments.MEDICINE_CONSUMABLE));
    public static final Reg.Handle<Item> S_CREAM =
            HbmSpace.REG.item(
                    "s_cream",
                    Item::new,
                    () ->
                            new Item.Properties()
                                    .food(
                                            foodOf(8, 1.0F),
                                            Consumables.defaultFood()
                                                    .onConsume(
                                                            new ApplyStatusEffectsConsumeEffect(
                                                                    new MobEffectInstance(
                                                                            MobEffects.REGENERATION,
                                                                            20
                                                                                    * SharedConstants
                                                                                            .TICKS_PER_SECOND,
                                                                            2),
                                                                    1.0F))
                                                    .build()));
    public static final Reg.Handle<Item> MIN_CREAM =
            HbmSpace.REG.item("min_cream", Item::new, () -> food(10, 1.0F));
    public static final Reg.Handle<Item> FLOUR =
            HbmSpace.REG.item("flour", Item::new, Item.Properties::new);
    public static final Reg.Handle<CustomLoreItem> COIN_AIRLINER =
            HbmSpace.REG.item(
                    "coin_airliner",
                    CustomLoreItem::new,
                    () -> new Item.Properties().rarity(Rarity.UNCOMMON));
    public static final Reg.Handle<Item> GLASS_EMPTY =
            HbmSpace.REG.item("glass_empty", Item::new, Item.Properties::new);
    public static final Reg.Handle<CustomLoreItem> POWDER_COFFEE =
            HbmSpace.REG.item("powder_coffee", CustomLoreItem::new, Item.Properties::new);
    public static final Reg.Handle<Item> BEAN_ROAST =
            HbmSpace.REG.item("bean_roast", Item::new, Item.Properties::new);
    public static final Reg.Handle<Item> CMUG_EMPTY =
            HbmSpace.REG.item("cmug_empty", Item::new, Item.Properties::new);
    public static final Reg.Handle<Item> TEACUP_EMPTY =
            HbmSpace.REG.item("teacup_empty", Item::new, Item.Properties::new);
    public static final Reg.Handle<DrinkEnergyItem> TEACUP =
            HbmSpace.REG.item(
                    "teacup",
                    props ->
                            new DrinkEnergyItem(
                                    props,
                                    null,
                                    false,
                                    player -> {
                                        player.heal(3F);
                                        player.addEffect(
                                                new MobEffectInstance(
                                                        MobEffects.RESISTANCE,
                                                        30 * SharedConstants.TICKS_PER_SECOND,
                                                        4));
                                    }),
                    () ->
                            new Item.Properties()
                                    .component(DataComponents.CONSUMABLE, Consumables.DEFAULT_DRINK)
                                    .craftRemainder(TEACUP_EMPTY.get()));
    public static final Reg.Handle<DrinkEnergyItem> BOTTLE_HONEY =
            HbmSpace.REG.item(
                    "bottle_honey",
                    props ->
                            new DrinkEnergyItem(
                                    props,
                                    null,
                                    false,
                                    player -> {
                                        player.heal(9F);
                                        LivingHazards.setDigamma(
                                                player,
                                                Math.max(LivingHazards.digamma(player) - 0.3D, 0D));
                                    }),
                    () ->
                            new Item.Properties()
                                    .component(DataComponents.CONSUMABLE, Consumables.DEFAULT_DRINK)
                                    .craftRemainder(Items.GLASS_BOTTLE));
    public static final Reg.Handle<DrinkEnergyItem> GLASS_SMILK =
            HbmSpace.REG.item(
                    "glass_smilk",
                    props -> new DrinkEnergyItem(props, null, false, player -> player.heal(6F)),
                    () ->
                            new Item.Properties()
                                    .component(DataComponents.CONSUMABLE, Consumables.DEFAULT_DRINK)
                                    .craftRemainder(GLASS_EMPTY.get()));
    public static final ItemFamily<FlaskSpaceItem.EnumInfusion, FlaskSpaceItem> FLASK_INFUSION =
            HbmSpace.REG
                    .family(
                            "flask_infusion",
                            FlaskSpaceItem.EnumInfusion.class,
                            FlaskSpaceItem::new,
                            () ->
                                    new Item.Properties()
                                            .component(
                                                    DataComponents.CONSUMABLE,
                                                    FlaskItem.CONSUMABLE))
                    .joins(ModItems.FLASK_INFUSION);
    public static final Reg.Handle<BSword> BUTTER_SWORD =
            HbmSpace.REG.item(
                    "butter_sword",
                    BSword::new,
                    () ->
                            SwordAbilityItem.swordProperties(
                                    ToolTier.VANILLA_WOOD,
                                    4.0F,
                                    0,
                                    ItemTags.WOODEN_TOOL_MATERIALS));

    static {
        CreativeContents.insertBefore(
                Tab.CONSUMABLES,
                RegistryUtil.id("bobmazon"),
                () -> new ItemStack(CHOCOLATE_MINT_BILLET.get()));
        after("gun_kit_2", ANIMAN);
        after("hbmspace:butter", S_CREAM, MIN_CREAM);
        after("chocolate", FLOUR);
        after("coin_ufo", COIN_AIRLINER);
        after("flask_infusion_shield", FLASK_INFUSION.handle(FlaskSpaceItem.EnumInfusion.NITAN));
        after(
                "bdcl",
                GLASS_SMILK,
                GLASS_EMPTY,
                POWDER_COFFEE,
                BEAN_ROAST,
                CMUG_EMPTY,
                TEACUP_EMPTY,
                TEACUP,
                BOTTLE_HONEY);
    }

    private FoodItemsSpace() {}

    private static FoodProperties foodOf(int nutrition, float saturation) {
        return new FoodProperties.Builder()
                .nutrition(nutrition)
                .saturationModifier(saturation)
                .build();
    }

    private static Item.Properties food(int nutrition, float saturation) {
        return new Item.Properties().food(foodOf(nutrition, saturation));
    }

    @SafeVarargs
    private static void after(String anchor, Reg.Handle<? extends Item>... entries) {
        Identifier id = anchor.contains(":") ? Identifier.parse(anchor) : RegistryUtil.id(anchor);
        for (Reg.Handle<? extends Item> entry : entries) {
            CreativeContents.insertAfter(Tab.CONSUMABLES, id, () -> new ItemStack(entry.get()));
        }
    }
}
