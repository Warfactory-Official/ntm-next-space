/*
 * SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
 * SPDX-FileCopyrightText: 2023 JamesH_2
 * SPDX-License-Identifier: LGPL-3.0-only
 *
 * Copyright (C) 2026 movblock. All rights reserved.
 * Copyright (C) 2023 JamesH_2. All rights reserved.
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

import com.hbm.api.energymk2.EnergyItem;
import com.hbm.inventory.container.BlockEntityMenu;
import com.hbm.inventory.container.SyncedData;
import com.hbm.inventory.slot.SlotFiltered;
import com.hbmspace.blockentity.machine.MilkReformerBlockEntity;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class MilkReformerMenu extends BlockEntityMenu<MilkReformerBlockEntity> {

    private static final int[][] TANK_SLOTS = {{45, 88}, {95, 88}, {122, 88}, {149, 88}};

    private final SyncedData data;

    public MilkReformerMenu(int containerId, Inventory playerInv, MilkReformerBlockEntity be) {
        this(
                containerId,
                playerInv,
                be,
                playerInv.player.level().isClientSide()
                        ? SyncedData.client(MilkReformerBlockEntity.class)
                        : SyncedData.of(be),
                be);
    }

    private MilkReformerMenu(
            int containerId,
            Inventory playerInv,
            Container container,
            SyncedData data,
            MilkReformerBlockEntity be) {
        super(ModMenusSpace.MACHINE_MILK_REFORMER.get(), containerId, be, container);
        checkContainerSize(container, MilkReformerBlockEntity.SLOT_COUNT);
        this.data = data;

        addSlot(
                new SlotFiltered(
                        container,
                        MilkReformerBlockEntity.SLOT_BATTERY,
                        79,
                        8,
                        EnergyItem::matches));
        for (int i = 0; i < TANK_SLOTS.length; i++) {
            int x = TANK_SLOTS[i][0], y = TANK_SLOTS[i][1];
            addSlot(new Slot(container, MilkReformerBlockEntity.SLOT_INPUT_IN + i * 2, x, y));
            addSlot(
                    new SlotFiltered(
                            container,
                            MilkReformerBlockEntity.SLOT_INPUT_OUT + i * 2,
                            x,
                            y + 18,
                            SlotFiltered.NONE));
        }

        addStandardInventorySlots(playerInv, 8, 156);
        addDataSlots(data);
    }

    public long getPower() {
        return data.get("power");
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return quickMove(
                player,
                index,
                false,
                stack -> {
                    int machineEnd = MilkReformerBlockEntity.SLOT_COUNT;
                    if (index < machineEnd)
                        return moveItemStackTo(stack, machineEnd, slots.size(), true);
                    if (EnergyItem.matches(stack))
                        return moveItemStackTo(
                                stack,
                                MilkReformerBlockEntity.SLOT_BATTERY,
                                MilkReformerBlockEntity.SLOT_BATTERY + 1,
                                false);
                    for (int slot = MilkReformerBlockEntity.SLOT_INPUT_IN;
                            slot < machineEnd;
                            slot += 2) {
                        if (moveItemStackTo(stack, slot, slot + 1, false)) return true;
                    }
                    return false;
                });
    }
}
