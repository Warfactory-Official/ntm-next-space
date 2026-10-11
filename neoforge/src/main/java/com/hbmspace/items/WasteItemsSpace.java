// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.items;

import com.hbm.blockentity.machine.BlockEntityStorageDrum.AddonDecay;
import com.hbm.blockentity.machine.BlockEntityStorageDrum;
import com.hbm.inventory.recipes.SILEXRecipes.TinyWaste;
import com.hbm.inventory.recipes.SILEXRecipes;
import com.hbm.items.CreativeContents.Tab;
import com.hbm.items.CreativeContents;
import com.hbm.items.ModItems;
import com.hbm.items.special.ItemWasteLong;
import com.hbm.items.special.ItemWasteShort;
import com.hbm.lib.Library;
import com.hbm.registration.ItemFamily;
import com.hbmspace.HbmSpace;
import java.util.List;
import net.minecraft.world.item.Item;

public final class WasteItemsSpace {

    public enum WasteClassSpace implements ItemWasteShort.Kind {
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

        public ItemWasteShort.WasteClass drumYield() {
            return ItemWasteShort.WasteClass.VALUES[meta % ItemWasteLong.WasteClass.VALUES.length];
        }
    }

    public static final ItemFamily<WasteClassSpace, ItemWasteShort> NUCLEAR_WASTE_SHORT =
            waste("nuclear_waste_short", ModItems.NUCLEAR_WASTE_SHORT);
    public static final ItemFamily<WasteClassSpace, ItemWasteShort> NUCLEAR_WASTE_SHORT_TINY =
            waste("nuclear_waste_short_tiny", ModItems.NUCLEAR_WASTE_SHORT_TINY);
    public static final ItemFamily<WasteClassSpace, ItemWasteShort> NUCLEAR_WASTE_SHORT_DEPLETED =
            waste("nuclear_waste_short_depleted", ModItems.NUCLEAR_WASTE_SHORT_DEPLETED);
    public static final ItemFamily<WasteClassSpace, ItemWasteShort>
            NUCLEAR_WASTE_SHORT_DEPLETED_TINY =
                    waste(
                            "nuclear_waste_short_depleted_tiny",
                            ModItems.NUCLEAR_WASTE_SHORT_DEPLETED_TINY);
    public static final List<ItemFamily<WasteClassSpace, ItemWasteShort>> WASTE_SHORT =
            List.of(
                    NUCLEAR_WASTE_SHORT,
                    NUCLEAR_WASTE_SHORT_TINY,
                    NUCLEAR_WASTE_SHORT_DEPLETED,
                    NUCLEAR_WASTE_SHORT_DEPLETED_TINY);

    static {
        for (ItemFamily<WasteClassSpace, ItemWasteShort> family : WASTE_SHORT) {
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
            ItemWasteShort.WasteClass yield = type.drumYield();
            BlockEntityStorageDrum.addon(
                    new AddonDecay(
                            () -> NUCLEAR_WASTE_SHORT.get(type),
                            () -> NUCLEAR_WASTE_SHORT_DEPLETED.stack(type),
                            1,
                            yield.liquid,
                            yield.gas),
                    new AddonDecay(
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

    private static ItemFamily<WasteClassSpace, ItemWasteShort> waste(
            String name, ItemFamily<?, ?> host) {
        return HbmSpace.REG
                .family(name, WasteClassSpace.class, ItemWasteShort::new, Item.Properties::new)
                .joins(host);
    }

    private static void after(
            String anchor,
            ItemFamily<WasteClassSpace, ItemWasteShort> family,
            WasteClassSpace... types) {
        for (WasteClassSpace type : types) {
            CreativeContents.insertAfter(Tab.PARTS, Library.id(anchor), () -> family.stack(type));
        }
    }
}
