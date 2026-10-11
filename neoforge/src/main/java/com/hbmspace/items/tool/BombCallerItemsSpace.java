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

public final class BombCallerItemsSpace {

    public static final Reg.Handle<ItemAirlinerCaller> AIRLINER =
            HbmSpace.REG.item(
                    "bomb_caller_airliner",
                    ItemAirlinerCaller::new,
                    () -> new Item.Properties().stacksTo(1));

    static {
        CreativeContents.insertAfter(
                Tab.CONSUMABLES,
                Library.id("bomb_caller_atomic_bomb"),
                () -> new ItemStack(AIRLINER.get()));
    }

    private BombCallerItemsSpace() {}
}
