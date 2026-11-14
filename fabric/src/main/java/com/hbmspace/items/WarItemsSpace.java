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

import com.hbm.items.machine.SatelliteChipItem;
import com.hbm.registration.Reg;
import com.hbm.saveddata.satellites.SatelliteType;
import com.hbmspace.HbmSpace;
import com.hbmspace.saveddata.satellites.SatelliteItems;
import com.hbmspace.saveddata.satellites.SatelliteRailgun;
import com.hbmspace.saveddata.satellites.SatellitesSpace;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;

public final class WarItemsSpace {
    public static final Reg.Handle<SatelliteChipItem> SAT_WAR =
            HbmSpace.REG.item(
                    "sat_war", SatelliteChipItem::new, () -> new Item.Properties().stacksTo(1));

    public static final SatelliteType WAR =
            SatelliteType.register(
                    "war",
                    Identifier.fromNamespaceAndPath(HbmSpace.MOD_ID, "sat_war"),
                    "WAR",
                    "SatelliteRailgun",
                    SatelliteRailgun::new,
                    SatelliteRailgun.CODEC,
                    null);

    static {
        SatellitesSpace.satellite(WAR, 0.0F, 0.0F, 0.0F, SAT_WAR);
        SatelliteItems.description(WAR, "war");
    }

    private WarItemsSpace() {}
}
