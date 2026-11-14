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

import com.hbm.blockentity.machine.StorageDrumBlockEntity.DecayExtension;
import com.hbm.blockentity.machine.StorageDrumBlockEntity;
import com.hbm.inventory.recipes.SILEXRecipes.TinyWaste;
import com.hbm.inventory.recipes.SILEXRecipes;
import com.hbm.items.CreativeContents.Tab;
import com.hbm.items.CreativeContents;
import com.hbm.items.ModItems;
import com.hbm.items.special.WasteLongItem;
import com.hbm.items.special.WasteShortItem;
import com.hbm.registration.ItemFamily;
import com.hbm.util.RegistryUtil;
import com.hbmspace.HbmSpace;
import java.util.List;
import net.minecraft.world.item.Item;

public final class WasteItemsSpace {

    public enum WasteClassSpace implements WasteShortItem.Kind {
        AMERICIUM241("Americium-241", 7),
        BERKELIUM247("Berkelium-247", 9),
        CURIUM244("Curium-244", 10),
        CURIUM245("Curium-245", 11);

        private final String label;
        private final int meta;

        WasteClassSpace(String label, int meta) {
            this.label = label;
            this.meta = meta;
        }

        @Override
        public String label() {
            return label;
        }

        public WasteShortItem.WasteClass drumYield() {
            return WasteShortItem.WasteClass.VALUES[meta % WasteLongItem.WasteClass.VALUES.length];
        }
    }

    public static final ItemFamily<WasteClassSpace, WasteShortItem> NUCLEAR_WASTE_SHORT =
            waste("nuclear_waste_short", ModItems.NUCLEAR_WASTE_SHORT);
    public static final ItemFamily<WasteClassSpace, WasteShortItem> NUCLEAR_WASTE_SHORT_TINY =
            waste("nuclear_waste_short_tiny", ModItems.NUCLEAR_WASTE_SHORT_TINY);
    public static final ItemFamily<WasteClassSpace, WasteShortItem> NUCLEAR_WASTE_SHORT_DEPLETED =
            waste("nuclear_waste_short_depleted", ModItems.NUCLEAR_WASTE_SHORT_DEPLETED);
    public static final ItemFamily<WasteClassSpace, WasteShortItem>
            NUCLEAR_WASTE_SHORT_DEPLETED_TINY =
                    waste(
                            "nuclear_waste_short_depleted_tiny",
                            ModItems.NUCLEAR_WASTE_SHORT_DEPLETED_TINY);
    public static final List<ItemFamily<WasteClassSpace, WasteShortItem>> WASTE_SHORT =
            List.of(
                    NUCLEAR_WASTE_SHORT,
                    NUCLEAR_WASTE_SHORT_TINY,
                    NUCLEAR_WASTE_SHORT_DEPLETED,
                    NUCLEAR_WASTE_SHORT_DEPLETED_TINY);

    static {
        for (ItemFamily<WasteClassSpace, WasteShortItem> family : WASTE_SHORT) {
            String base =
                    family.handle(WasteClassSpace.AMERICIUM241)
                            .id()
                            .getPath()
                            .replace("_americium241", "");
            after(base + "_americium242", family, WasteClassSpace.AMERICIUM241);
            after(
                    base + "_schrabidium",
                    family,
                    WasteClassSpace.BERKELIUM247,
                    WasteClassSpace.CURIUM244,
                    WasteClassSpace.CURIUM245);
        }
        for (WasteClassSpace type : WasteClassSpace.values()) {
            WasteShortItem.WasteClass yield = type.drumYield();
            StorageDrumBlockEntity.registerDecay(
                    new DecayExtension(
                            () -> NUCLEAR_WASTE_SHORT.get(type),
                            () -> NUCLEAR_WASTE_SHORT_DEPLETED.stack(type),
                            1,
                            yield.liquid,
                            yield.gas),
                    new DecayExtension(
                            () -> NUCLEAR_WASTE_SHORT_TINY.get(type),
                            () -> NUCLEAR_WASTE_SHORT_DEPLETED_TINY.stack(type),
                            10,
                            yield.liquid / 10,
                            yield.gas / 10));
            SILEXRecipes.addonTinyWaste(
                    new TinyWaste(
                            () -> NUCLEAR_WASTE_SHORT_TINY.get(type),
                            () -> NUCLEAR_WASTE_SHORT.get(type)),
                    new TinyWaste(
                            () -> NUCLEAR_WASTE_SHORT_DEPLETED_TINY.get(type),
                            () -> NUCLEAR_WASTE_SHORT_DEPLETED.get(type)));
        }
    }

    private WasteItemsSpace() {}

    private static ItemFamily<WasteClassSpace, WasteShortItem> waste(
            String name, ItemFamily<?, ?> host) {
        return HbmSpace.REG
                .family(name, WasteClassSpace.class, WasteShortItem::new, Item.Properties::new)
                .joins(host);
    }

    private static void after(
            String anchor,
            ItemFamily<WasteClassSpace, WasteShortItem> family,
            WasteClassSpace... types) {
        for (WasteClassSpace type : types) {
            CreativeContents.insertAfter(
                    Tab.PARTS, RegistryUtil.id(anchor), () -> family.stack(type));
        }
    }
}
