// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.items;

import com.hbm.registration.Reg;
import com.hbmspace.HbmSpace;
import net.minecraft.world.item.Item;

public final class HiddenItemsSpace {
    public static final Reg.Handle<Item> DIVINE_SHARD =
            HbmSpace.REG.item("divine_shard", Item::new, Item.Properties::new);

    private HiddenItemsSpace() {}
}
