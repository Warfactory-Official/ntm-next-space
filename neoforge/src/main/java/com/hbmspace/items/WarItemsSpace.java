// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.items;

import com.hbm.items.machine.ItemSatelliteChip;
import com.hbm.registration.Reg;
import com.hbm.saveddata.satellites.SatelliteType;
import com.hbmspace.HbmSpace;
import com.hbmspace.saveddata.satellites.SatelliteItems;
import com.hbmspace.saveddata.satellites.SatelliteRailgun;
import com.hbmspace.saveddata.satellites.SatellitesSpace;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;

public final class WarItemsSpace {
    public static final Reg.Handle<ItemSatelliteChip> SAT_WAR =
            HbmSpace.REG.item(
                    "sat_war", ItemSatelliteChip::new, () -> new Item.Properties().stacksTo(1));

    public static final SatelliteType WAR =
            SatelliteType.register(
                    "war",
                    Identifier.fromNamespaceAndPath(HbmSpace.MOD_ID, "sat_war"),
                    "WAR",
                    SatelliteRailgun::new,
                    SatelliteRailgun.CODEC,
                    null);

    static {
        SatellitesSpace.satellite(WAR, 0.0F, 0.0F, 0.0F, SAT_WAR);
        SatelliteItems.description(WAR, "war");
    }

    private WarItemsSpace() {}
}
