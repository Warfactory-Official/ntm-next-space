// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.items;

import com.hbm.items.CreativeContents.Tab;
import com.hbm.items.CreativeContents;
import com.hbm.items.special.ItemSatellite;
import com.hbm.lib.Library;
import com.hbm.registration.Reg;
import com.hbm.saveddata.satellites.SatelliteType;
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

    public static final Reg.Handle<ItemSatellite> SAT_DYSON_RELAY =
            HbmSpace.REG.item(
                    "sat_dyson_relay", ItemSatellite::new, () -> new Item.Properties().stacksTo(1));

    public static final SatelliteType DYSON_RELAY =
            SatelliteType.register(
                    "dyson_relay",
                    Identifier.fromNamespaceAndPath(HbmSpace.MOD_ID, "sat_dyson_relay"),
                    "DYSON_RELAY",
                    SatelliteDysonRelay::new,
                    SatelliteDysonRelay.CODEC,
                    null);

    static {
        SatellitesSpace.satellite(DYSON_RELAY, 1.0F, 0.9F, 0.8F, SAT_DYSON_RELAY);
        SatelliteItems.description(DYSON_RELAY, "dyson_relay");

        CreativeContents.insertAfter(
                Tab.MISSILES, Library.id("sat_chip"), () -> new ItemStack(SAT_DYSON_RELAY.get()));
        CreativeContents.insertAfter(
                Tab.PARTS, Library.id("crystal_charred"), () -> new ItemStack(SWARM_MEMBER.get()));
    }

    private SkyItemsSpace() {}
}
