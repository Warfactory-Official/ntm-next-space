// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.items;

import com.hbm.items.CreativeContents.Tab;
import com.hbm.items.CreativeContents;
import com.hbm.items.ModItems;
import com.hbm.items.machine.ItemCassette.TrackType;
import com.hbm.items.machine.ItemCassette;
import com.hbm.lib.Library;
import com.hbm.registration.ItemFamily;
import com.hbmspace.HbmSpace;
import java.util.Locale;
import net.minecraft.world.item.Item;

public final class SirenItemsSpace {

    public static final ItemFamily<TrackType, ItemCassette> TRACKS =
            HbmSpace.REG
                    .family(
                            new TrackType[] {TrackType.DISEMBODIED, TrackType.SUICIDE},
                            type -> "siren_track_" + type.name().toLowerCase(Locale.ROOT),
                            (props, type) -> new ItemCassette(props),
                            () -> new Item.Properties().stacksTo(1))
                    .joins(ModItems.SIREN_TRACK);

    static {
        CreativeContents.insertAfter(
                Tab.TEMPLATES,
                Library.id("siren_track_razortrain"),
                () -> TRACKS.stack(TrackType.DISEMBODIED));
        CreativeContents.insertAfter(
                Tab.TEMPLATES,
                Library.id("siren_track_razortrain"),
                () -> TRACKS.stack(TrackType.SUICIDE));
    }

    private SirenItemsSpace() {}
}
