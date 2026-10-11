// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.inventory.container;

import com.hbm.inventory.container.BlockEntityMenu;
import com.hbmspace.blockentity.machine.BlockEntityMachineWarController;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class MenuMachineWarController extends BlockEntityMenu<BlockEntityMachineWarController> {

    public static final int SLOT_COUNT = 4;

    public MenuMachineWarController(
            int containerId, Inventory playerInv, BlockEntityMachineWarController be) {
        super(WarMenusSpace.WAR_CONTROLLER.get(), containerId, be);
        checkContainerSize(be, SLOT_COUNT);

        addSlot(new Slot(be, 0, 6, 52));
        addSlot(new Slot(be, BlockEntityMachineWarController.SLOT_DRIVE, 29, 11));
        addSlot(new Slot(be, BlockEntityMachineWarController.SLOT_CHIP, 49, 11));
        addSlot(new Slot(be, 3, 29, 31));

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(playerInv, col + row * 9 + 9, 8 + col * 18, 103 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) addSlot(new Slot(playerInv, col, 8 + col * 18, 162));
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return quickMove(
                player,
                index,
                false,
                stack ->
                        index < SLOT_COUNT
                                ? moveItemStackTo(stack, SLOT_COUNT, slots.size(), true)
                                : moveItemStackTo(stack, 0, SLOT_COUNT, false));
    }
}
