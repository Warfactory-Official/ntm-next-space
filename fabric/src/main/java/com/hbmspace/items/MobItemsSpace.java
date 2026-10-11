// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.items;

import com.hbm.items.CreativeContents.Tab;
import com.hbm.items.CreativeContents;
import com.hbm.items.machine.ItemBattery;
import com.hbm.items.special.ItemChopper;
import com.hbm.lib.Library;
import com.hbm.registration.Reg;
import com.hbmspace.HbmSpace;
import com.hbmspace.entity.MobsSpace;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public final class MobItemsSpace {
    public static final Reg.Handle<ItemBattery> CORE_ANGEL =
            HbmSpace.REG.item(
                    "core_angel",
                    props -> new ItemBattery(props, 16_000_000L, 16_000_000L, 800_000L),
                    () -> new Item.Properties().stacksTo(1));
    public static final Reg.Handle<ItemChopper> SPAWN_ANGEL =
            HbmSpace.REG.item(
                    "spawn_angel",
                    props -> new ItemChopper(props, () -> MobsSpace.BF_ANGEL.get(), 25, mob -> {}),
                    () -> new Item.Properties().stacksTo(1));

    static {
        CreativeContents.insertAfter(
                Tab.CONTROL, Library.id("cube_power"), () -> CORE_ANGEL.get().empty());
        CreativeContents.insertAfter(
                Tab.CONTROL, Library.id("cube_power"), () -> CORE_ANGEL.get().full());
        CreativeContents.insertAfter(
                Tab.CONSUMABLES, Library.id("spawn_duck"), () -> new ItemStack(SPAWN_ANGEL.get()));
    }

    private MobItemsSpace() {}
}
