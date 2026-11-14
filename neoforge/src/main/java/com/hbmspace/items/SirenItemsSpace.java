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
import com.hbm.items.ModItems;
import com.hbm.items.machine.CassetteItem.TrackType;
import com.hbm.items.machine.CassetteItem;
import com.hbm.registration.ItemFamily;
import com.hbm.util.RegistryUtil;
import com.hbmspace.HbmSpace;
import java.util.Locale;
import net.minecraft.world.item.Item;

public final class SirenItemsSpace {

    public static final ItemFamily<TrackType, CassetteItem> TRACKS =
            HbmSpace.REG
                    .family(
                            new TrackType[] {TrackType.DISEMBODIED, TrackType.SUICIDE},
                            type -> "siren_track_" + type.name().toLowerCase(Locale.ROOT),
                            (props, type) -> new CassetteItem(props),
                            () -> new Item.Properties().stacksTo(1))
                    .joins(ModItems.SIREN_TRACK);

    static {
        CreativeContents.insertAfter(
                Tab.TEMPLATES,
                RegistryUtil.id("siren_track_razortrain"),
                () -> TRACKS.stack(TrackType.DISEMBODIED));
        CreativeContents.insertAfter(
                Tab.TEMPLATES,
                RegistryUtil.id("siren_track_razortrain"),
                () -> TRACKS.stack(TrackType.SUICIDE));
    }

    private SirenItemsSpace() {}
}
