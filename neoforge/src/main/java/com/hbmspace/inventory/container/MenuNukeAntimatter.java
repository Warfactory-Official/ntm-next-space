// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.inventory.container;

import com.hbm.inventory.container.BlockEntityMenu;
import com.hbm.inventory.material.MaterialShapes;
import com.hbm.inventory.slot.SlotFiltered;
import com.hbm.items.MaterialShapeRoster;
import com.hbm.items.ModItems;
import com.hbmspace.blockentity.bomb.BlockEntityNukeAntimatter;
import com.hbmspace.inventory.material.MatsSpace;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import static com.hbmspace.blockentity.bomb.BlockEntityNukeAntimatter.SLOT_CORE;
import static com.hbmspace.blockentity.bomb.BlockEntityNukeAntimatter.SLOT_HAFNIUM_A;
import static com.hbmspace.blockentity.bomb.BlockEntityNukeAntimatter.SLOT_HAFNIUM_B;
import static com.hbmspace.blockentity.bomb.BlockEntityNukeAntimatter.SLOT_LEAD_A;
import static com.hbmspace.blockentity.bomb.BlockEntityNukeAntimatter.SLOT_LEAD_B;

public class MenuNukeAntimatter extends BlockEntityMenu<BlockEntityNukeAntimatter> {

    public static final int SHOWN = 5;
    private static final int PLAYER_DROP = 56;

    public MenuNukeAntimatter(
            int containerId, Inventory playerInv, BlockEntityNukeAntimatter bomb) {
        super(BombMenusSpace.NUKE_ANTIMATTER.get(), containerId, bomb);
        checkContainerSize(bomb, BlockEntityNukeAntimatter.SLOT_COUNT);

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
                                                MaterialShapes.INGOT, MatsSpace.MAT_HAFNIUM))));
        addSlot(
                new SlotFiltered(
                        container(),
                        SLOT_HAFNIUM_B,
                        134,
                        43,
                        stack ->
                                stack.is(
                                        MaterialShapeRoster.require(
                                                MaterialShapes.INGOT, MatsSpace.MAT_HAFNIUM))));

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
