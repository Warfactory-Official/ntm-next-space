// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.items.tool;

import com.hbm.items.CreativeContents.Tab;
import com.hbm.items.CreativeContents;
import com.hbm.lib.Library;
import com.hbm.registration.Reg;
import com.hbmspace.HbmSpace;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public final class WandTimeItemsSpace {

    public static final Reg.Handle<ItemWandTimeSpace> WAND_TIME =
            HbmSpace.REG.item(
                    "wand_time", ItemWandTimeSpace::new, () -> new Item.Properties().stacksTo(1));

    static {
        CreativeContents.insertBefore(
                Tab.CONSUMABLES,
                Library.id("structure_custommachine"),
                () -> new ItemStack(WAND_TIME.get()));
    }

    private WandTimeItemsSpace() {}
}
