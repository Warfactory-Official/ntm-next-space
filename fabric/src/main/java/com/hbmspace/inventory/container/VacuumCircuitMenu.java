/*
 * SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
 * SPDX-FileCopyrightText: 2025 TheVitya2127
 * SPDX-License-Identifier: LGPL-3.0-only
 *
 * Copyright (C) 2026 movblock. All rights reserved.
 * Copyright (C) 2025 TheVitya2127. All rights reserved.
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
import com.hbm.inventory.recipes.ingredient.CountIngredient;
import com.hbm.inventory.slot.SlotFiltered;
import com.hbm.inventory.slot.SlotRecipeOutput;
import com.hbm.inventory.slot.SlotUpgrade;
import com.hbm.items.machine.upgrade.MachineUpgradeItem;
import com.hbmspace.blockentity.machine.VacuumCircuitBlockEntity;
import com.hbmspace.inventory.recipes.VacuumCircuitRecipes;
import java.util.List;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class VacuumCircuitMenu extends BlockEntityMenu<VacuumCircuitBlockEntity> {

    private final SyncedData data;

    public VacuumCircuitMenu(int containerId, Inventory playerInv, VacuumCircuitBlockEntity be) {
        this(
                containerId,
                playerInv,
                be,
                playerInv.player.level().isClientSide()
                        ? SyncedData.client(VacuumCircuitBlockEntity.class)
                        : SyncedData.of(be),
                be);
    }

    private VacuumCircuitMenu(
            int containerId,
            Inventory playerInv,
            Container container,
            SyncedData data,
            VacuumCircuitBlockEntity be) {
        super(IndustryMenusSpace.MACHINE_VACUUM_CIRCUIT.get(), containerId, be, container);
        checkContainerSize(container, VacuumCircuitBlockEntity.SLOT_COUNT);
        this.data = data;

        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 2; j++)
                addSlot(SlotFiltered.gated(container, i * 2 + j, 26 + j * 18, 18 + i * 18));
        }
        addSlot(
                new SlotRecipeOutput(
                        playerInv.player, container, VacuumCircuitBlockEntity.SLOT_OUTPUT, 98, 27));
        addSlot(
                new SlotFiltered(
                        container,
                        VacuumCircuitBlockEntity.SLOT_BATTERY,
                        143,
                        72,
                        EnergyItem::matches));
        addSlot(new SlotUpgrade(container, VacuumCircuitBlockEntity.SLOT_UPGRADE_START, 89, 72));
        addSlot(new SlotUpgrade(container, VacuumCircuitBlockEntity.SLOT_UPGRADE_END, 107, 72));

        addStandardInventorySlots(playerInv, 8, 122);
        addDataSlots(data);
    }

    private static boolean matchesAny(List<CountIngredient> group, ItemStack stack) {
        for (CountIngredient ing : group) if (ing.ingredient().test(stack)) return true;
        return false;
    }

    public int getProgress() {
        return data.getInt("progress");
    }

    public int getProcessTime() {
        return data.getInt("processTime");
    }

    public long getPower() {
        return data.get("power");
    }

    public long getMaxPower() {
        return data.get("maxPower");
    }

    public long getConsumption() {
        return data.get("consumption");
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return quickMove(
                player,
                index,
                true,
                stack -> {
                    int machineEnd = VacuumCircuitBlockEntity.SLOT_COUNT;
                    if (index < machineEnd)
                        return moveItemStackTo(stack, machineEnd, slots.size(), true);
                    if (EnergyItem.matches(stack))
                        return moveItemStackTo(
                                stack,
                                VacuumCircuitBlockEntity.SLOT_BATTERY,
                                VacuumCircuitBlockEntity.SLOT_BATTERY + 1,
                                false);
                    if (MachineUpgradeItem.isUpgrade(stack))
                        return moveItemStackTo(
                                stack,
                                VacuumCircuitBlockEntity.SLOT_UPGRADE_START,
                                VacuumCircuitBlockEntity.SLOT_UPGRADE_END + 1,
                                false);
                    if (matchesAny(VacuumCircuitRecipes.wafer(), stack))
                        return moveItemStackTo(stack, 0, 2, false);
                    if (matchesAny(VacuumCircuitRecipes.pcb(), stack))
                        return moveItemStackTo(stack, 2, 4, false);
                    return false;
                });
    }
}
