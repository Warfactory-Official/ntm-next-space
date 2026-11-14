/*
 * SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
 * SPDX-FileCopyrightText: 2025 JamesH_2
 * SPDX-License-Identifier: LGPL-3.0-only
 *
 * Copyright (C) 2026 movblock. All rights reserved.
 * Copyright (C) 2025 JamesH_2. All rights reserved.
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
import com.hbmspace.blockentity.machine.WarControllerBlockEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class WarControllerMenu extends BlockEntityMenu<WarControllerBlockEntity> {

    public static final int SLOT_COUNT = 4;

    public WarControllerMenu(int containerId, Inventory playerInv, WarControllerBlockEntity be) {
        super(WarMenusSpace.WAR_CONTROLLER.get(), containerId, be);
        checkContainerSize(be, SLOT_COUNT);

        addSlot(new Slot(be, 0, 6, 52));
        addSlot(new Slot(be, WarControllerBlockEntity.SLOT_DRIVE, 29, 11));
        addSlot(new Slot(be, WarControllerBlockEntity.SLOT_CHIP, 49, 11));
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
