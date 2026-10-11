// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.inventory.container;

import com.hbm.inventory.container.BlockEntityMenu;
import com.hbm.inventory.slot.SlotFiltered;
import com.hbmspace.blockentity.machine.BlockEntityMachineStardar;
import com.hbmspace.items.ItemVOTVdrive;
import com.hbmspace.items.weapon.MissilePartsSpace;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class MenuMachineStardar extends BlockEntityMenu<BlockEntityMachineStardar> {

    public MenuMachineStardar(
            int containerId, Inventory playerInv, BlockEntityMachineStardar stardar) {
        super(RocketryMenusSpace.MACHINE_STARDAR.get(), containerId, stardar);
        checkContainerSize(stardar, 1);

        addSlot(
                new SlotFiltered(
                        stardar,
                        0,
                        150,
                        124,
                        stack ->
                                stack.is(MissilePartsSpace.HARD_DRIVE.get())
                                        || stack.getItem() instanceof ItemVOTVdrive));

        addStandardInventorySlots(playerInv, 8, 174);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return quickMoveUnsorted(player, index, 1);
    }
}
