// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.inventory.container;

import com.hbm.api.energymk2.IBatteryItem;
import com.hbm.inventory.container.BlockEntityMenu;
import com.hbm.inventory.container.SyncedData;
import com.hbm.inventory.slot.SlotFiltered;
import com.hbmspace.blockentity.machine.BlockEntityMachineMilkReformer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class MenuMachineMilkReformer extends BlockEntityMenu<BlockEntityMachineMilkReformer> {

    private static final int[][] TANK_SLOTS = {{45, 88}, {95, 88}, {122, 88}, {149, 88}};

    private final SyncedData data;

    public MenuMachineMilkReformer(
            int containerId, Inventory playerInv, BlockEntityMachineMilkReformer be) {
        this(
                containerId,
                playerInv,
                be,
                playerInv.player.level().isClientSide()
                        ? SyncedData.client(BlockEntityMachineMilkReformer.class)
                        : SyncedData.of(be),
                be);
    }

    private MenuMachineMilkReformer(
            int containerId,
            Inventory playerInv,
            Container container,
            SyncedData data,
            BlockEntityMachineMilkReformer be) {
        super(ModMenusSpace.MACHINE_MILK_REFORMER.get(), containerId, be, container);
        checkContainerSize(container, BlockEntityMachineMilkReformer.SLOT_COUNT);
        this.data = data;

        addSlot(
                new SlotFiltered(
                        container,
                        BlockEntityMachineMilkReformer.SLOT_BATTERY,
                        79,
                        8,
                        IBatteryItem::isBattery));
        for (int i = 0; i < TANK_SLOTS.length; i++) {
            int x = TANK_SLOTS[i][0], y = TANK_SLOTS[i][1];
            addSlot(
                    new Slot(
                            container, BlockEntityMachineMilkReformer.SLOT_INPUT_IN + i * 2, x, y));
            addSlot(
                    new SlotFiltered(
                            container,
                            BlockEntityMachineMilkReformer.SLOT_INPUT_OUT + i * 2,
                            x,
                            y + 18,
                            SlotFiltered.NONE));
        }

        addStandardInventorySlots(playerInv, 8, 156);
        addDataSlots(data);
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
                    int machineEnd = BlockEntityMachineMilkReformer.SLOT_COUNT;
                    if (index < machineEnd)
                        return moveItemStackTo(stack, machineEnd, slots.size(), true);
                    if (IBatteryItem.isBattery(stack))
                        return moveItemStackTo(
                                stack,
                                BlockEntityMachineMilkReformer.SLOT_BATTERY,
                                BlockEntityMachineMilkReformer.SLOT_BATTERY + 1,
                                false);
                    for (int slot = BlockEntityMachineMilkReformer.SLOT_INPUT_IN;
                            slot < machineEnd;
                            slot += 2) {
                        if (moveItemStackTo(stack, slot, slot + 1, false)) return true;
                    }
                    return false;
                });
    }
}
