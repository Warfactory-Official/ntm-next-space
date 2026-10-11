// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.blocks;

import com.hbm.blocks.machine.storage.MachineCapacitor;
import com.hbm.items.CreativeContents.Tab;
import com.hbm.items.CreativeContents;
import com.hbm.lib.Library;
import com.hbm.registration.Reg;
import com.hbmspace.HbmSpace;
import com.hbmspace.items.block.ItemCapacitorComplex;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

public final class CapacitorBlocksSpace {

    public static final long COMPLEX_POWER = 100_000_000_000_000_000L;

    public static final Reg.Handle<MachineCapacitor> CAPACITOR_COMPLEX =
            HbmSpace.REG.blockItem(
                    "capacitor_complex",
                    props -> new MachineCapacitor(COMPLEX_POWER, props),
                    () ->
                            BlockBehaviour.Properties.of()
                                    .mapColor(MapColor.METAL)
                                    .strength(5.0F, 6.0F)
                                    .requiresCorrectToolForDrops()
                                    .noOcclusion(),
                    ItemCapacitorComplex::new);

    static {
        CreativeContents.insertAfter(
                Tab.MACHINES,
                Library.id("capacitor_copper"),
                () -> new ItemStack(CAPACITOR_COMPLEX.get()));
    }

    private CapacitorBlocksSpace() {}
}
