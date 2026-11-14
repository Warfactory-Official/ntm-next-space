/*
 * SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
 * SPDX-FileCopyrightText: 2024 George Paton
 * SPDX-License-Identifier: LGPL-3.0-only
 *
 * Copyright (C) 2026 movblock. All rights reserved.
 * Copyright (C) 2024 George Paton. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License version 3 only,
 * as published by the Free Software Foundation.
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this code. If not, see <https://www.gnu.org/licenses/>.
 *
 * Under GPLv3 §7(c) and §7(e), these additional terms apply to LGPL-covered
 * material authored by movblock:
 *
 * - the license grants no rights to the name "NTM NEXT: Space";
 * - a modified version must not be named NTM NEXT: Space or presented as
 *   the official release, and must be marked as different from it.
 *
 * Accurately identifying a work as derived from NTM NEXT: Space does not
 * violate these additional terms, provided it is clearly distinguished from
 * the official release and does not imply endorsement. Ports are encouraged
 * to acknowledge NTM NEXT: Space as their upstream project and provide a
 * link to it.
 */

package com.hbmspace.inventory.container;

import com.hbm.inventory.container.BlockEntityMenu;
import com.hbm.items.machine.FluidIdentifierItem;
import com.hbmspace.blockentity.machine.TransporterRocketBlockEntity;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class TransporterRocketMenu extends BlockEntityMenu<TransporterRocketBlockEntity> {

    public TransporterRocketMenu(
            int containerId, Inventory playerInventory, TransporterRocketBlockEntity pad) {
        super(StationMenusSpace.TRANSPORTER_ROCKET.get(), containerId, pad);
        checkContainerSize(pad, TransporterRocketBlockEntity.SLOTS);

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
        return quickMove(
                player,
                index,
                false,
                stack -> {
                    int machineEnd = TransporterRocketBlockEntity.SLOTS;
                    if (index < machineEnd)
                        return moveItemStackTo(stack, machineEnd, slots.size(), true);
                    if (stack.getItem() instanceof FluidIdentifierItem
                            && moveItemStackTo(stack, 16, 22, false)) return true;
                    return moveItemStackTo(stack, 0, machineEnd, false);
                });
    }
}
