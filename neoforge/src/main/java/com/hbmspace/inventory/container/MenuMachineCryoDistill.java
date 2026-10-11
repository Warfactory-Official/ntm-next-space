// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.inventory.container;

import com.hbm.api.energymk2.IBatteryItem;
import com.hbm.inventory.container.BlockEntityMenu;
import com.hbm.inventory.container.SyncedData;
import com.hbm.inventory.slot.SlotFiltered;
import com.hbm.items.machine.FluidIdentifierItem;
import com.hbmspace.blockentity.machine.BlockEntityMachineCryoDistill;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class MenuMachineCryoDistill extends BlockEntityMenu<BlockEntityMachineCryoDistill> {

    private static final int[] UNLOAD_X = {80, 98, 116, 134};

    private final SyncedData data;

    public MenuMachineCryoDistill(
            int containerId, Inventory playerInv, BlockEntityMachineCryoDistill be) {
        this(
                containerId,
                playerInv,
                be,
                playerInv.player.level().isClientSide()
                        ? SyncedData.client(BlockEntityMachineCryoDistill.class)
                        : SyncedData.of(be),
                be);
    }

    private MenuMachineCryoDistill(
            int containerId,
            Inventory playerInv,
            Container container,
            SyncedData data,
            BlockEntityMachineCryoDistill be) {
        super(IndustryMenusSpace.MACHINE_CRYO_DISTILL.get(), containerId, be, container);
        checkContainerSize(container, BlockEntityMachineCryoDistill.SLOT_COUNT);
        this.data = data;

        addSlot(
                new SlotFiltered(
                        container,
                        BlockEntityMachineCryoDistill.SLOT_BATTERY,
                        26,
                        90,
                        IBatteryItem::isBattery));
        for (int i = 0; i < 3; i++) unload(container, i);
        addSlot(
                new SlotFiltered(
                        container,
                        BlockEntityMachineCryoDistill.SLOT_FLUID_ID,
                        26,
                        108,
                        stack -> stack.getItem() instanceof FluidIdentifierItem));
        unload(container, 3);

        addStandardInventorySlots(playerInv, 8, 156);
        addDataSlots(data);
    }

    private void unload(Container container, int tank) {
        int[] slots = BlockEntityMachineCryoDistill.SLOT_UNLOAD[tank];
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
                    int machineEnd = BlockEntityMachineCryoDistill.SLOT_COUNT;
                    if (index < machineEnd)
                        return moveItemStackTo(stack, machineEnd, slots.size(), true);
                    if (IBatteryItem.isBattery(stack))
                        return moveItemStackTo(
                                stack,
                                BlockEntityMachineCryoDistill.SLOT_BATTERY,
                                BlockEntityMachineCryoDistill.SLOT_BATTERY + 1,
                                false);
                    if (stack.getItem() instanceof FluidIdentifierItem)
                        return moveItemStackTo(
                                stack,
                                BlockEntityMachineCryoDistill.SLOT_FLUID_ID,
                                BlockEntityMachineCryoDistill.SLOT_FLUID_ID + 1,
                                false);
                    for (int[] pair : BlockEntityMachineCryoDistill.SLOT_UNLOAD) {
                        if (moveItemStackTo(stack, pair[0], pair[0] + 1, false)) return true;
                    }
                    return false;
                });
    }
}
