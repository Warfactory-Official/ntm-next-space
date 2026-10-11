// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.items;

import com.hbm.items.CreativeContents.Tab;
import com.hbm.items.CreativeContents;
import com.hbm.lib.Library;
import com.hbm.registration.Reg;
import com.hbmspace.HbmSpace;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public final class ConsumableItemsSpace {
    public static final Reg.Handle<Item> BUTTER =
            HbmSpace.REG.item("butter", Item::new, Item.Properties::new);

    static {
        CreativeContents.insertAfter(
                Tab.CONSUMABLES, Library.id("pancake"), () -> new ItemStack(BUTTER.get()));
    }

    private ConsumableItemsSpace() {}
}
