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
import com.hbmspace.blockentity.machine.HydroponicBlockEntity;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class HydroponicMenu extends BlockEntityMenu<HydroponicBlockEntity> {

    private final SyncedData data;

    public HydroponicMenu(int containerId, Inventory playerInv, HydroponicBlockEntity be) {
        this(
                containerId,
                playerInv,
                be,
                playerInv.player.level().isClientSide()
                        ? SyncedData.client(HydroponicBlockEntity.class)
                        : SyncedData.of(be),
                be);
    }

    private HydroponicMenu(
            int containerId,
            Inventory playerInv,
            Container container,
            SyncedData data,
            HydroponicBlockEntity be) {
        super(IndustryMenusSpace.HYDROBAY.get(), containerId, be, container);
        checkContainerSize(container, HydroponicBlockEntity.SLOT_COUNT);
        this.data = data;

        addSlot(
                new SlotFiltered(
                        container,
                        HydroponicBlockEntity.SLOT_PLANT,
                        67,
                        18,
                        HydroponicBlockEntity::isPlantable));
        addSlot(
                new SlotFiltered(
                        container,
                        HydroponicBlockEntity.SLOT_FERTILIZER,
                        67,
                        54,
                        stack -> HydroponicBlockEntity.fertilizerStrength(stack) > 0));
        addSlot(
                new SlotFiltered(
                        container,
                        HydroponicBlockEntity.SLOT_BATTERY,
                        147,
                        54,
                        EnergyItem::matches));
        for (int i = 0; i < 3; i++) {
            addSlot(
                    new SlotFiltered(
                            container,
                            HydroponicBlockEntity.SLOT_OUTPUT_START + i,
                            111,
                            18 + i * 18,
                            SlotFiltered.NONE));
        }

        addStandardInventorySlots(playerInv, 8, 104);
        addDataSlots(data);
    }

    public long getPower() {
        return data.get("power");
    }

    public int getFertilizer() {
        return (int) data.get("fertilizer");
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return quickMoveFilteredUnsorted(player, index, HydroponicBlockEntity.SLOT_COUNT);
    }
}
