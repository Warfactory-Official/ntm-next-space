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

package com.hbmspace.items.tool;

import com.hbm.items.CreativeContents.Tab;
import com.hbm.items.CreativeContents;
import com.hbm.registration.Reg;
import com.hbm.util.RegistryUtil;
import com.hbmspace.HbmSpace;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public final class BombCallerItemsSpace {

    public static final Reg.Handle<AirlinerCallerItem> AIRLINER =
            HbmSpace.REG.item(
                    "bomb_caller_airliner",
                    AirlinerCallerItem::new,
                    () -> new Item.Properties().stacksTo(1));

    static {
        CreativeContents.insertAfter(
                Tab.CONSUMABLES,
                RegistryUtil.id("bomb_caller_atomic_bomb"),
                () -> new ItemStack(AIRLINER.get()));
    }

    private BombCallerItemsSpace() {}
}
