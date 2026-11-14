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

import com.hbm.items.ModItems;
import com.hbm.items.special.SpawnEggItem;
import com.hbm.registration.Reg;
import com.hbmspace.entity.MobEntitiesSpace;
import com.hbmspace.entity.MobsSpace;
import java.util.List;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public final class VanillaCreativeSpace {

    public record Insertion(
            ResourceKey<CreativeModeTab> tab, Item before, List<ItemStack> stacks) {}

    private VanillaCreativeSpace() {}

    public static List<Insertion> insertions() {
        List<Reg.Handle<SpawnEggItem>> eggs = MobEntitiesSpace.EGGS;
        return List.of(
                new Insertion(
                        CreativeModeTabs.COMBAT,
                        ModItems.SCHRABIDIUM_SWORD.get(),
                        List.of(new ItemStack(FoodItemsSpace.BUTTER_SWORD.get()))),
                new Insertion(
                        CreativeModeTabs.SPAWN_EGGS,
                        anchor("entity_mob_nuclear_creeper_spawn_egg"),
                        List.of(
                                new ItemStack(MobsSpace.SIEGE_UFO_EGG.get()),
                                        new ItemStack(eggs.get(0).get()),
                                new ItemStack(eggs.get(1).get()),
                                        new ItemStack(MobsSpace.BF_ANGEL_EGG.get()))),
                new Insertion(
                        CreativeModeTabs.SPAWN_EGGS,
                        anchor("entity_mob_hunter_chopper_spawn_egg"),
                        List.of(new ItemStack(eggs.get(2).get()))),
                new Insertion(
                        CreativeModeTabs.SPAWN_EGGS,
                        anchor("entity_fbi_spawn_egg"),
                        List.of(
                                new ItemStack(eggs.get(3).get()),
                                new ItemStack(eggs.get(4).get()),
                                new ItemStack(eggs.get(5).get()),
                                new ItemStack(eggs.get(6).get()),
                                new ItemStack(eggs.get(7).get()),
                                new ItemStack(eggs.get(8).get()),
                                new ItemStack(MobsSpace.SIEGE_CRAFT_EGG.get()))));
    }

    private static Item anchor(String path) {
        return BuiltInRegistries.ITEM
                .getOptional(Identifier.fromNamespaceAndPath("hbm", path))
                .orElseThrow();
    }
}
