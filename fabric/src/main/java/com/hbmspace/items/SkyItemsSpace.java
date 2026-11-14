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
import com.hbm.items.special.SatelliteItem;
import com.hbm.registration.Reg;
import com.hbm.saveddata.satellites.SatelliteType;
import com.hbm.util.RegistryUtil;
import com.hbmspace.HbmSpace;
import com.hbmspace.saveddata.satellites.SatelliteDysonRelay;
import com.hbmspace.saveddata.satellites.SatelliteItems;
import com.hbmspace.saveddata.satellites.SatellitesSpace;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public final class SkyItemsSpace {
    public static final Reg.Handle<Item> SWARM_MEMBER =
            HbmSpace.REG.item("swarm_member", Item::new, Item.Properties::new);

    public static final Reg.Handle<SatelliteItem> SAT_DYSON_RELAY =
            HbmSpace.REG.item(
                    "sat_dyson_relay", SatelliteItem::new, () -> new Item.Properties().stacksTo(1));

    public static final SatelliteType DYSON_RELAY =
            SatelliteType.register(
                    "dyson_relay",
                    Identifier.fromNamespaceAndPath(HbmSpace.MOD_ID, "sat_dyson_relay"),
                    "DYSON_RELAY",
                    "SatelliteDysonRelay",
                    SatelliteDysonRelay::new,
                    SatelliteDysonRelay.CODEC,
                    null);

    static {
        SatellitesSpace.satellite(DYSON_RELAY, 1.0F, 0.9F, 0.8F, SAT_DYSON_RELAY);
        SatelliteItems.description(DYSON_RELAY, "dyson_relay");

        CreativeContents.insertAfter(
                Tab.MISSILES,
                RegistryUtil.id("sat_chip"),
                () -> new ItemStack(SAT_DYSON_RELAY.get()));
        CreativeContents.insertAfter(
                Tab.PARTS,
                RegistryUtil.id("crystal_charred"),
                () -> new ItemStack(SWARM_MEMBER.get()));
    }

    private SkyItemsSpace() {}
}
