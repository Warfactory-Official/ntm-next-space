/*
 * SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
 * SPDX-FileCopyrightText: 2024-2025 George Paton
 * SPDX-License-Identifier: LGPL-3.0-only
 *
 * Copyright (C) 2026 movblock. All rights reserved.
 * Copyright (C) 2024-2025 George Paton. All rights reserved.
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
import com.hbm.items.weapon.CustomMissilePartItem.PartType;
import com.hbmspace.blockentity.machine.RocketAssemblyBlockEntity;
import com.hbmspace.handler.RocketStruct;
import com.hbmspace.inventory.SlotRocket.SlotCapsule;
import com.hbmspace.inventory.SlotRocket.SlotDrive;
import com.hbmspace.inventory.SlotRocket.SlotRocketPart;
import com.hbmspace.inventory.SlotRocket;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import static com.hbmspace.blockentity.machine.RocketAssemblyBlockEntity.SLOT_COUNT;
import static com.hbmspace.blockentity.machine.RocketAssemblyBlockEntity.SLOT_RESULT;

public class RocketAssemblyMenu extends BlockEntityMenu<RocketAssemblyBlockEntity> {

    private int layer;

    public RocketAssemblyMenu(int containerId, Inventory playerInv, RocketAssemblyBlockEntity be) {
        super(RocketryMenusSpace.MACHINE_ROCKET_ASSEMBLY.get(), containerId, be);
        checkContainerSize(be, SLOT_COUNT);

        addSlot(new SlotCapsule(be, RocketAssemblyBlockEntity.SLOT_CAPSULE, 18, 13));
        for (int i = 0; i < RocketStruct.MAX_STAGES; i++) {
            addSlot(
                    new SlotRocketPart(
                            be,
                            RocketAssemblyBlockEntity.fuselage(i),
                            18,
                            44,
                            i,
                            this::layer,
                            PartType.FUSELAGE));
            addSlot(
                    new SlotRocketPart(
                            be,
                            RocketAssemblyBlockEntity.fins(i),
                            18,
                            62,
                            i,
                            this::layer,
                            PartType.FINS));
            addSlot(
                    new SlotRocketPart(
                            be,
                            RocketAssemblyBlockEntity.thruster(i),
                            18,
                            80,
                            i,
                            this::layer,
                            PartType.THRUSTER));
        }
        addSlot(new SlotRocket(be, SLOT_RESULT, 42, 91));
        for (int i = 0; i < RocketStruct.MAX_STAGES; i++) {
            addSlot(
                    new SlotDrive(
                            be, RocketAssemblyBlockEntity.driveFrom(i), 161, 54, i, this::layer));
            addSlot(
                    new SlotDrive(
                            be, RocketAssemblyBlockEntity.driveTo(i), 170, 87, i, this::layer));
        }

        addStandardInventorySlots(playerInv, 8, 142);
    }

    public int layer() {
        return layer;
    }

    public void setLayer(int layer) {
        this.layer = layer;
        blockEntity().currentStage = layer;
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (id < 0 || id >= RocketStruct.MAX_STAGES) return false;
        setLayer(id);
        return true;
    }

    @Override
    public void clicked(int slotId, int button, ContainerInput input, Player player) {
        if (slotId >= SLOT_COUNT && input == ContainerInput.QUICK_MOVE) return;

        if (slotId > SLOT_RESULT && slotId < SLOT_COUNT) {
            Slot slot = slots.get(slotId);
            ItemStack held = getCarried();
            if (!held.isEmpty() && !slot.mayPlace(held)) return;
            slot.set(held.isEmpty() ? ItemStack.EMPTY : held.copyWithCount(1));
            slot.setChanged();
            return;
        }

        super.clicked(slotId, button, input, player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return quickMove(
                player,
                index,
                false,
                stack ->
                        index < SLOT_COUNT
                                && moveItemStackTo(stack, SLOT_COUNT, slots.size(), true));
    }
}
