// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.inventory.container;

import com.hbm.api.energymk2.IBatteryItem;
import com.hbm.inventory.container.BlockEntityMenu;
import com.hbm.inventory.container.SyncedData;
import com.hbm.inventory.slot.SlotFiltered;
import com.hbmspace.blockentity.machine.BlockEntityHydroponic;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class MenuHydroponic extends BlockEntityMenu<BlockEntityHydroponic> {

    private final SyncedData data;

    public MenuHydroponic(int containerId, Inventory playerInv, BlockEntityHydroponic be) {
        this(
                containerId,
                playerInv,
                be,
                playerInv.player.level().isClientSide()
                        ? SyncedData.client(BlockEntityHydroponic.class)
                        : SyncedData.of(be),
                be);
    }

    private MenuHydroponic(
            int containerId,
            Inventory playerInv,
            Container container,
            SyncedData data,
            BlockEntityHydroponic be) {
        super(IndustryMenusSpace.HYDROBAY.get(), containerId, be, container);
        checkContainerSize(container, BlockEntityHydroponic.SLOT_COUNT);
        this.data = data;

        addSlot(
                new SlotFiltered(
                        container,
                        BlockEntityHydroponic.SLOT_PLANT,
                        67,
                        18,
                        BlockEntityHydroponic::isPlantable));
        addSlot(
                new SlotFiltered(
                        container,
                        BlockEntityHydroponic.SLOT_FERTILIZER,
                        67,
                        54,
                        stack -> BlockEntityHydroponic.fertilizerStrength(stack) > 0));
        addSlot(
                new SlotFiltered(
                        container,
                        BlockEntityHydroponic.SLOT_BATTERY,
                        147,
                        54,
                        IBatteryItem::isBattery));
        for (int i = 0; i < 3; i++) {
            addSlot(
                    new SlotFiltered(
                            container,
                            BlockEntityHydroponic.SLOT_OUTPUT_START + i,
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
        return quickMoveFilteredUnsorted(player, index, BlockEntityHydroponic.SLOT_COUNT);
    }
}
