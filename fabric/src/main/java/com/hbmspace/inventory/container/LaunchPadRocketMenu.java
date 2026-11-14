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
import com.hbm.inventory.fluid.tank.FluidTankNTM;
import com.hbm.inventory.slot.SlotFiltered;
import com.hbm.items.ModItems;
import com.hbmspace.blockentity.bomb.LaunchPadRocketBlockEntity;
import com.hbmspace.inventory.SlotRocket;
import com.hbmspace.items.VOTVdriveItem;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import static com.hbmspace.blockentity.bomb.LaunchPadRocketBlockEntity.SLOT_COUNT;

public class LaunchPadRocketMenu extends BlockEntityMenu<LaunchPadRocketBlockEntity> {

    public LaunchPadRocketMenu(
            int containerId, Inventory playerInv, LaunchPadRocketBlockEntity pad) {
        super(RocketryMenusSpace.LAUNCH_PAD_ROCKET.get(), containerId, pad);
        checkContainerSize(pad, SLOT_COUNT);

        addSlot(new SlotRocket(pad, LaunchPadRocketBlockEntity.SLOT_ROCKET, 37, 21));
        addSlot(
                new SlotFiltered(
                        pad,
                        LaunchPadRocketBlockEntity.SLOT_DRIVE,
                        37,
                        39,
                        stack -> stack.getItem() instanceof VOTVdriveItem));
        addSlot(
                new SlotFiltered(
                        pad,
                        LaunchPadRocketBlockEntity.SLOT_BATTERY,
                        167,
                        90,
                        stack ->
                                EnergyItem.matches(stack)
                                        || stack.is(ModItems.BATTERY_CREATIVE.get())));
        addSlot(
                new SlotFiltered(
                        pad,
                        LaunchPadRocketBlockEntity.SLOT_FUEL_IN,
                        77,
                        21,
                        stack ->
                                stack.is(ModItems.ROCKET_FUEL.get())
                                        || FluidTankNTM.isFluidContainer(stack)));
        addSlot(
                new SlotFiltered(
                        pad, LaunchPadRocketBlockEntity.SLOT_FUEL_OUT, 77, 39, SlotFiltered.NONE));

        addStandardInventorySlots(playerInv, 14, 154);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return quickMoveUnsorted(player, index, SLOT_COUNT);
    }
}
