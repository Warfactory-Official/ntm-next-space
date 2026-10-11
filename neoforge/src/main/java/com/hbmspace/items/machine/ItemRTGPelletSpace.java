// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.items.machine;

import com.hbm.items.machine.ItemRTGPellet;
import com.hbm.registration.Reg;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class ItemRTGPelletSpace extends ItemRTGPellet {

    private final Reg.Handle<? extends Item> residual;

    public ItemRTGPelletSpace(
            Properties props,
            int heat,
            Reg.Handle<? extends Item> residual,
            long halflife,
            double halflifeCount) {
        super(props, heat);
        this.residual = residual;
        setDecays(null, halflife, halflifeCount);
    }

    @Override
    public ItemStack getDecayItem() {
        return new ItemStack(residual.get());
    }
}
