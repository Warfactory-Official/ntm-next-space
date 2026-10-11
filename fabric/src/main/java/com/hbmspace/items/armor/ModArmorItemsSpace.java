// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.items.armor;

import com.hbm.items.CreativeContents.Tab;
import com.hbm.items.CreativeContents;
import com.hbm.items.armor.ItemModInsert;
import com.hbm.lib.Library;
import com.hbm.registration.Reg;
import com.hbmspace.HbmSpace;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public final class ModArmorItemsSpace {
    public static final Reg.Handle<ItemModOxy> OXY_PLSS =
            HbmSpace.REG.item(
                    "oxy_plss", props -> new ItemModOxy(props, 16000, 10, 1), Item.Properties::new);
    public static final Reg.Handle<ItemModInsert> INSERT_CMB =
            HbmSpace.REG.item(
                    "insert_cmb",
                    props -> new ItemModInsert(props, 9999, 0.7F, 0.9F, 0.4F, 1F),
                    Item.Properties::new);
    public static final Reg.Handle<ItemLoxTank> LOX_TANK =
            HbmSpace.REG.item(
                    "lox_tank", ItemLoxTank::new, () -> new Item.Properties().stacksTo(16));

    static {
        CreativeContents.insertAfter(
                Tab.CONSUMABLES, Library.id("anchor_remote"), () -> new ItemStack(OXY_PLSS.get()));
        CreativeContents.insertAfter(
                Tab.CONSUMABLES,
                Library.id("insert_ghiorsium"),
                () -> new ItemStack(INSERT_CMB.get()));
        CreativeContents.insertAfter(
                Tab.CONSUMABLES, Library.id("jetpack_tank"), () -> new ItemStack(LOX_TANK.get()));
    }

    private ModArmorItemsSpace() {}
}
