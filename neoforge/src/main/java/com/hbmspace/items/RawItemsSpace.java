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
import com.hbm.registration.Reg;
import com.hbm.util.RegistryUtil;
import com.hbmspace.HbmSpace;
import java.util.List;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public final class RawItemsSpace {
    public static final Reg.Handle<Item> NICKEL = raw("nickel", "nickel_pure", "Nickel");
    public static final Reg.Handle<Item> ZINC = raw("zinc", "zinc", "Zinc");
    public static final Reg.Handle<Item> AUSTRALIUM = raw("australium", "australium", "Australium");
    public static final Reg.Handle<Item> NIOBIUM = raw("niobium", "niobium", "Niobium");
    public static final Reg.Handle<Item> LANTHANIUM = raw("lanthanium", "lanthanum", "Lanthanium");

    static {
        var anchor = RegistryUtil.id("raw_lithium");
        for (Reg.Handle<Item> item : List.of(NICKEL, ZINC, AUSTRALIUM, NIOBIUM, LANTHANIUM)) {
            CreativeContents.insertAfter(Tab.PARTS, anchor, () -> new ItemStack(item.get()));
            anchor = item.id();
        }
    }

    private RawItemsSpace() {}

    private static Reg.Handle<Item> raw(String id, String material, String name) {
        return HbmSpace.REG.item("raw_" + id, Item::new, Item.Properties::new);
    }
}
