// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.inventory.container;

import com.hbm.registration.NeoForgeRegistrar;
import com.hbm.registration.RegistryHandle;
import com.hbmspace.HbmSpace;
import com.hbmspace.blockentity.bomb.BlockEntityNukeAntimatter;
import net.minecraft.world.inventory.MenuType;

public final class BombMenusSpace {

    public static final RegistryHandle<MenuType<MenuNukeAntimatter>> NUKE_ANTIMATTER =
            NeoForgeRegistrar.registerBlockEntityMenu(
                    HbmSpace.MOD_ID + ":nuke_antimatter", BlockEntityNukeAntimatter.class);

    private BombMenusSpace() {}
}
