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
import com.hbm.inventory.slot.SlotFiltered;
import com.hbm.items.ModItems;
import com.hbmspace.blockentity.machine.DriveProcessorBlockEntity;
import com.hbmspace.items.ComponentItemsSpace;
import com.hbmspace.items.VOTVdriveItem;
import com.hbmspace.items.weapon.MissilePartsSpace;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import static com.hbmspace.blockentity.machine.DriveProcessorBlockEntity.SLOT_COUNT;

public class DriveProcessorMenu extends BlockEntityMenu<DriveProcessorBlockEntity> {

    public DriveProcessorMenu(
            int containerId, Inventory playerInv, DriveProcessorBlockEntity machine) {
        super(RocketryMenusSpace.MACHINE_DRIVE_PROCESSOR.get(), containerId, machine);
        checkContainerSize(machine, SLOT_COUNT);

        addSlot(
                new SlotFiltered(
                        machine,
                        DriveProcessorBlockEntity.SLOT_DRIVE,
                        30,
                        18,
                        stack -> stack.getItem() instanceof VOTVdriveItem));
        addSlot(
                new SlotFiltered(
                        machine,
                        DriveProcessorBlockEntity.SLOT_CLONE,
                        50,
                        38,
                        stack -> stack.is(MissilePartsSpace.HARD_DRIVE.get())));
        addSlot(
                new SlotFiltered(
                        machine,
                        DriveProcessorBlockEntity.SLOT_CIRCUIT,
                        81,
                        24,
                        stack ->
                                stack.is(ComponentItemsSpace.CIRCUIT_PROCESST1.get())
                                        || stack.is(ComponentItemsSpace.CIRCUIT_PROCESST2.get())
                                        || stack.is(ComponentItemsSpace.CIRCUIT_PROCESST3.get())));
        addSlot(
                new SlotFiltered(
                        machine,
                        DriveProcessorBlockEntity.SLOT_BATTERY,
                        134,
                        72,
                        stack ->
                                EnergyItem.matches(stack)
                                        || stack.is(ModItems.BATTERY_CREATIVE.get())));

        addStandardInventorySlots(playerInv, 8, 125);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return quickMoveUnsorted(player, index, SLOT_COUNT);
    }
}
