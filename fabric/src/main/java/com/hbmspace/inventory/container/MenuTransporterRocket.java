// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.inventory.container;

import com.hbm.inventory.container.BlockEntityMenu;
import com.hbmspace.blockentity.machine.BlockEntityTransporterRocket;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class MenuTransporterRocket extends BlockEntityMenu<BlockEntityTransporterRocket> {

    public MenuTransporterRocket(
            int containerId, Inventory playerInventory, BlockEntityTransporterRocket pad) {
        super(StationMenusSpace.TRANSPORTER_ROCKET.get(), containerId, pad);
        checkContainerSize(pad, BlockEntityTransporterRocket.SLOTS);

        addSlots(pad, 0, 8, 34, 2, 4);
        addSlots(pad, 8, 98, 34, 2, 4);
        addSlots(pad, 16, 8, 106, 1, 4);
        addSlots(pad, 20, 188, 90, 1, 2);

        addStandardInventorySlots(playerInventory, 8, 154);
    }

    private void addSlots(Container inv, int from, int x, int y, int rows, int cols) {
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                addSlot(new Slot(inv, col + row * cols + from, x + col * 18, y + row * 18));
            }
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return quickMoveUnsorted(player, index, BlockEntityTransporterRocket.SLOTS);
    }
}
