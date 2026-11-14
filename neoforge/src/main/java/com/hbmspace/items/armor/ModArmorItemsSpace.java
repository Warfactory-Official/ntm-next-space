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

package com.hbmspace.items.armor;

import com.hbm.items.CreativeContents.Tab;
import com.hbm.items.CreativeContents;
import com.hbm.items.armor.ModInsertItem;
import com.hbm.registration.Reg;
import com.hbm.util.RegistryUtil;
import com.hbmspace.HbmSpace;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public final class ModArmorItemsSpace {
    public static final Reg.Handle<ModOxyItem> OXY_PLSS =
            HbmSpace.REG.item(
                    "oxy_plss", props -> new ModOxyItem(props, 16000, 10, 1), Item.Properties::new);
    public static final Reg.Handle<ModInsertItem> INSERT_CMB =
            HbmSpace.REG.item(
                    "insert_cmb",
                    props -> new ModInsertItem(props, 9999, 0.7F, 0.9F, 0.4F, 1F),
                    Item.Properties::new);
    public static final Reg.Handle<LoxTankItem> LOX_TANK =
            HbmSpace.REG.item(
                    "lox_tank", LoxTankItem::new, () -> new Item.Properties().stacksTo(16));

    static {
        CreativeContents.insertAfter(
                Tab.CONSUMABLES,
                RegistryUtil.id("anchor_remote"),
                () -> new ItemStack(OXY_PLSS.get()));
        CreativeContents.insertAfter(
                Tab.CONSUMABLES,
                RegistryUtil.id("insert_ghiorsium"),
                () -> new ItemStack(INSERT_CMB.get()));
        CreativeContents.insertAfter(
                Tab.CONSUMABLES,
                RegistryUtil.id("jetpack_tank"),
                () -> new ItemStack(LOX_TANK.get()));
    }

    private ModArmorItemsSpace() {}
}
