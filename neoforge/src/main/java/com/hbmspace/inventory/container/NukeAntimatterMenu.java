/*
 * SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
 * SPDX-FileCopyrightText: 2023 DUODEC
 * SPDX-FileCopyrightText: 2023 JamesH_2
 * SPDX-License-Identifier: LGPL-3.0-only
 *
 * Copyright (C) 2026 movblock. All rights reserved.
 * Copyright (C) 2023 DUODEC. All rights reserved.
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

import com.hbm.inventory.container.BlockEntityMenu;
import com.hbm.inventory.material.MaterialShape;
import com.hbm.inventory.slot.SlotFiltered;
import com.hbm.items.MaterialShapeRoster;
import com.hbm.items.ModItems;
import com.hbmspace.blockentity.bomb.NukeAntimatterBlockEntity;
import com.hbmspace.inventory.material.MatsSpace;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import static com.hbmspace.blockentity.bomb.NukeAntimatterBlockEntity.SLOT_CORE;
import static com.hbmspace.blockentity.bomb.NukeAntimatterBlockEntity.SLOT_HAFNIUM_A;
import static com.hbmspace.blockentity.bomb.NukeAntimatterBlockEntity.SLOT_HAFNIUM_B;
import static com.hbmspace.blockentity.bomb.NukeAntimatterBlockEntity.SLOT_LEAD_A;
import static com.hbmspace.blockentity.bomb.NukeAntimatterBlockEntity.SLOT_LEAD_B;

public class NukeAntimatterMenu extends BlockEntityMenu<NukeAntimatterBlockEntity> {

    public static final int SHOWN = 5;
    private static final int PLAYER_DROP = 56;

    public NukeAntimatterMenu(
            int containerId, Inventory playerInv, NukeAntimatterBlockEntity bomb) {
        super(BombMenusSpace.NUKE_ANTIMATTER.get(), containerId, bomb);
        checkContainerSize(bomb, NukeAntimatterBlockEntity.SLOT_COUNT);

        addSlot(
                new SlotFiltered(
                        container(),
                        SLOT_LEAD_A,
                        26,
                        15,
                        stack -> stack.is(ModItems.PARTICLE_LEAD.get())));
        addSlot(
                new SlotFiltered(
                        container(),
                        SLOT_LEAD_B,
                        134,
                        14,
                        stack -> stack.is(ModItems.PARTICLE_LEAD.get())));
        addSlot(
                new SlotFiltered(
                        container(),
                        SLOT_CORE,
                        81,
                        15,
                        stack -> stack.is(ModItems.PELLET_ANTIMATTER.get())));
        addSlot(
                new SlotFiltered(
                        container(),
                        SLOT_HAFNIUM_A,
                        26,
                        44,
                        stack ->
                                stack.is(
                                        MaterialShapeRoster.require(
                                                MaterialShape.INGOT, MatsSpace.MAT_HAFNIUM))));
        addSlot(
                new SlotFiltered(
                        container(),
                        SLOT_HAFNIUM_B,
                        134,
                        43,
                        stack ->
                                stack.is(
                                        MaterialShapeRoster.require(
                                                MaterialShape.INGOT, MatsSpace.MAT_HAFNIUM))));

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(
                        new Slot(
                                playerInv,
                                col + row * 9 + 9,
                                8 + col * 18,
                                84 + row * 18 + PLAYER_DROP));
            }
        }
        for (int col = 0; col < 9; col++)
            addSlot(new Slot(playerInv, col, 8 + col * 18, 142 + PLAYER_DROP));
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return quickMove(
                player,
                index,
                false,
                stack -> index < SHOWN && moveItemStackTo(stack, SHOWN, slots.size(), true));
    }
}
