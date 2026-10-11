// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.inventory.container;

import com.hbm.api.energymk2.IBatteryItem;
import com.hbm.inventory.container.BlockEntityMenu;
import com.hbm.inventory.slot.SlotFiltered;
import com.hbm.items.ModItems;
import com.hbmspace.blockentity.machine.BlockEntityMachineDriveProcessor;
import com.hbmspace.items.ComponentItemsSpace;
import com.hbmspace.items.ItemVOTVdrive;
import com.hbmspace.items.weapon.MissilePartsSpace;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import static com.hbmspace.blockentity.machine.BlockEntityMachineDriveProcessor.SLOT_COUNT;

public class MenuMachineDriveProcessor extends BlockEntityMenu<BlockEntityMachineDriveProcessor> {

    public MenuMachineDriveProcessor(
            int containerId, Inventory playerInv, BlockEntityMachineDriveProcessor machine) {
        super(RocketryMenusSpace.MACHINE_DRIVE_PROCESSOR.get(), containerId, machine);
        checkContainerSize(machine, SLOT_COUNT);

        addSlot(
                new SlotFiltered(
                        machine,
                        BlockEntityMachineDriveProcessor.SLOT_DRIVE,
                        30,
                        18,
                        stack -> stack.getItem() instanceof ItemVOTVdrive));
        addSlot(
                new SlotFiltered(
                        machine,
                        BlockEntityMachineDriveProcessor.SLOT_CLONE,
                        50,
                        38,
                        stack -> stack.is(MissilePartsSpace.HARD_DRIVE.get())));
        addSlot(
                new SlotFiltered(
                        machine,
                        BlockEntityMachineDriveProcessor.SLOT_CIRCUIT,
                        81,
                        24,
                        stack ->
                                stack.is(ComponentItemsSpace.CIRCUIT_PROCESST1.get())
                                        || stack.is(ComponentItemsSpace.CIRCUIT_PROCESST2.get())
                                        || stack.is(ComponentItemsSpace.CIRCUIT_PROCESST3.get())));
        addSlot(
                new SlotFiltered(
                        machine,
                        BlockEntityMachineDriveProcessor.SLOT_BATTERY,
                        134,
                        72,
                        stack ->
                                IBatteryItem.isBattery(stack)
                                        || stack.is(ModItems.BATTERY_CREATIVE.get())));

        addStandardInventorySlots(playerInv, 8, 125);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return quickMoveUnsorted(player, index, SLOT_COUNT);
    }
}
