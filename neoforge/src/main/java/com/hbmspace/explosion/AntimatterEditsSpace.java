// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.explosion;

import com.hbm.blockentity.bomb.CustomNukeEntries.Entry;
import com.hbm.blockentity.bomb.CustomNukeEntries.Stage;
import com.hbm.blockentity.bomb.CustomNukeEntries;
import com.hbm.items.ModItems;
import com.hbmspace.HbmSpace;
import java.util.Map;
import net.minecraft.world.item.Item;

public final class AntimatterEditsSpace {

    private AntimatterEditsSpace() {}

    public static void install() {
        CustomNukeEntries.EDITS.install(HbmSpace.MOD_ID, AntimatterEditsSpace::entries);
    }

    private static void entries(Map<Item, Entry> table) {
        table.remove(ModItems.EGG_BALEFIRE_SHARD.get());
        table.remove(ModItems.EGG_BALEFIRE.get());
        table.put(ModItems.PARTICLE_AMAT.get(), new Entry(Stage.AMAT, 5F, false));
        table.put(ModItems.PELLET_ANTIMATTER.get(), new Entry(Stage.AMAT, 45F, false));
    }
}
