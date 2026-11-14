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
import com.hbm.registration.Registrar;
import com.hbm.registration.RegistryHandle;
import com.hbm.util.RegistryUtil;
import com.hbmspace.HbmSpace;
import com.hbmspace.items.tool.TransporterLinkerItem;
import java.util.List;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public final class StationItemsSpace {
    public static final RegistryHandle<DataComponentType<List<GlobalPos>>> TRANSPORTER_LINKS =
            Registrar.registerDataComponent(
                    HbmSpace.MOD_ID + ":transporter_links",
                    DataComponentType.<List<GlobalPos>>builder()
                            .persistent(GlobalPos.CODEC.listOf())
                            .networkSynchronized(GlobalPos.STREAM_CODEC.apply(ByteBufCodecs.list()))
                            .build());

    public static final Reg.Handle<TransporterLinkerItem> TRANSPORTER_LINKER =
            HbmSpace.REG.item(
                    "transporter_linker",
                    TransporterLinkerItem::new,
                    () -> new Item.Properties().stacksTo(1));

    static {
        CreativeContents.insertAfter(
                Tab.CONTROL,
                RegistryUtil.id("inf_water_mk2"),
                () -> new ItemStack(TRANSPORTER_LINKER.get()));
    }

    private StationItemsSpace() {}
}
