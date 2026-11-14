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

import com.hbm.items.CreativeContents.Tab;
import com.hbm.items.CreativeContents;
import com.hbm.items.machine.BatteryItem;
import com.hbm.items.special.ChopperItem;
import com.hbm.registration.ItemSubtype;
import com.hbm.registration.Reg;
import com.hbm.util.RegistryUtil;
import com.hbmspace.HbmSpace;
import com.hbmspace.entity.MobsSpace;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public final class MobItemsSpace {
    public static final Reg.Handle<BatteryItem> CORE_ANGEL =
            HbmSpace.REG
                    .item(
                            "core_angel",
                            props -> new BatteryItem(props, 16_000_000L, 16_000_000L, 800_000L),
                            () -> new Item.Properties().stacksTo(1))
                    .subtypes(ItemSubtype.BATTERY_CHARGE);
    public static final Reg.Handle<ChopperItem> SPAWN_ANGEL =
            HbmSpace.REG.item(
                    "spawn_angel",
                    props -> new ChopperItem(props, () -> MobsSpace.BF_ANGEL.get(), 25, mob -> {}),
                    () -> new Item.Properties().stacksTo(1));

    static {
        CreativeContents.insertAfter(
                Tab.CONTROL, RegistryUtil.id("cube_power"), () -> CORE_ANGEL.get().empty());
        CreativeContents.insertAfter(
                Tab.CONTROL, RegistryUtil.id("cube_power"), () -> CORE_ANGEL.get().full());
        CreativeContents.insertAfter(
                Tab.CONSUMABLES,
                RegistryUtil.id("spawn_duck"),
                () -> new ItemStack(SPAWN_ANGEL.get()));
    }

    private MobItemsSpace() {}
}
