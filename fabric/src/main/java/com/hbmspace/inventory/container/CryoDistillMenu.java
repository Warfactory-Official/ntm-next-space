/*
 * SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright (C) 2026 movblock. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * You may not use this file under the Apache License except in
 * compliance with that License. You may obtain a copy at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Apache License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License for the specific language governing permissions
 * and limitations under the License.
 */

package com.hbmspace.inventory.container;

import com.hbm.api.energymk2.EnergyItem;
import com.hbm.inventory.container.BlockEntityMenu;
import com.hbm.inventory.container.SyncedData;
import com.hbm.inventory.slot.SlotFiltered;
import com.hbm.items.machine.FluidIdentifierItem;
import com.hbmspace.blockentity.machine.CryoDistillBlockEntity;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class CryoDistillMenu extends BlockEntityMenu<CryoDistillBlockEntity> {

    private static final int[] UNLOAD_X = {80, 98, 116, 134};

    private final SyncedData data;

    public CryoDistillMenu(int containerId, Inventory playerInv, CryoDistillBlockEntity be) {
        this(
                containerId,
                playerInv,
                be,
                playerInv.player.level().isClientSide()
                        ? SyncedData.client(CryoDistillBlockEntity.class)
                        : SyncedData.of(be),
                be);
    }

    private CryoDistillMenu(
            int containerId,
            Inventory playerInv,
            Container container,
            SyncedData data,
            CryoDistillBlockEntity be) {
        super(IndustryMenusSpace.MACHINE_CRYO_DISTILL.get(), containerId, be, container);
        checkContainerSize(container, CryoDistillBlockEntity.SLOT_COUNT);
        this.data = data;

        addSlot(
                new SlotFiltered(
                        container,
                        CryoDistillBlockEntity.SLOT_BATTERY,
                        26,
                        90,
                        EnergyItem::matches));
        for (int i = 0; i < 3; i++) unload(container, i);
        addSlot(
                new SlotFiltered(
                        container,
                        CryoDistillBlockEntity.SLOT_FLUID_ID,
                        26,
                        108,
                        stack -> stack.getItem() instanceof FluidIdentifierItem));
        unload(container, 3);

        addStandardInventorySlots(playerInv, 8, 156);
        addDataSlots(data);
    }

    private void unload(Container container, int tank) {
        int[] slots = CryoDistillBlockEntity.SLOT_UNLOAD[tank];
        addSlot(new Slot(container, slots[0], UNLOAD_X[tank], 90));
        addSlot(new SlotFiltered(container, slots[1], UNLOAD_X[tank], 108, SlotFiltered.NONE));
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
                    int machineEnd = CryoDistillBlockEntity.SLOT_COUNT;
                    if (index < machineEnd)
                        return moveItemStackTo(stack, machineEnd, slots.size(), true);
                    if (stack.getItem() instanceof FluidIdentifierItem)
                        return moveItemStackTo(
                                stack,
                                CryoDistillBlockEntity.SLOT_FLUID_ID,
                                CryoDistillBlockEntity.SLOT_FLUID_ID + 1,
                                false);
                    if (EnergyItem.matches(stack))
                        return moveItemStackTo(
                                stack,
                                CryoDistillBlockEntity.SLOT_BATTERY,
                                CryoDistillBlockEntity.SLOT_BATTERY + 1,
                                false);
                    for (int[] pair : CryoDistillBlockEntity.SLOT_UNLOAD) {
                        if (moveItemStackTo(stack, pair[0], pair[0] + 1, false)) return true;
                    }
                    return false;
                });
    }
}
