// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.inventory.container;

import com.hbm.registration.NeoForgeRegistrar;
import com.hbm.registration.RegistryHandle;
import com.hbmspace.HbmSpace;
import com.hbmspace.blockentity.machine.BlockEntityMachineMilkReformer;
import net.minecraft.world.inventory.MenuType;

public final class ModMenusSpace {
    public static final RegistryHandle<MenuType<MenuMachineMilkReformer>> MACHINE_MILK_REFORMER =
            NeoForgeRegistrar.registerBlockEntityMenu(
                    HbmSpace.MOD_ID + ":machine_milk_reformer",
                    BlockEntityMachineMilkReformer.class);

    private ModMenusSpace() {}
}
