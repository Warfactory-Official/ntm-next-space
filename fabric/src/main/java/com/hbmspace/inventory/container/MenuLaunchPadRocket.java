// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.inventory.container;

import com.hbm.api.energymk2.IBatteryItem;
import com.hbm.inventory.container.BlockEntityMenu;
import com.hbm.inventory.fluid.tank.FluidTankNTM;
import com.hbm.inventory.slot.SlotFiltered;
import com.hbm.items.ModItems;
import com.hbmspace.blockentity.bomb.BlockEntityLaunchPadRocket;
import com.hbmspace.inventory.SlotRocket;
import com.hbmspace.items.ItemVOTVdrive;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import static com.hbmspace.blockentity.bomb.BlockEntityLaunchPadRocket.SLOT_COUNT;

public class MenuLaunchPadRocket extends BlockEntityMenu<BlockEntityLaunchPadRocket> {

    public MenuLaunchPadRocket(
            int containerId, Inventory playerInv, BlockEntityLaunchPadRocket pad) {
        super(RocketryMenusSpace.LAUNCH_PAD_ROCKET.get(), containerId, pad);
        checkContainerSize(pad, SLOT_COUNT);

        addSlot(new SlotRocket(pad, BlockEntityLaunchPadRocket.SLOT_ROCKET, 37, 21));
        addSlot(
                new SlotFiltered(
                        pad,
                        BlockEntityLaunchPadRocket.SLOT_DRIVE,
                        37,
                        39,
                        stack -> stack.getItem() instanceof ItemVOTVdrive));
        addSlot(
                new SlotFiltered(
                        pad,
                        BlockEntityLaunchPadRocket.SLOT_BATTERY,
                        167,
                        90,
                        stack ->
                                IBatteryItem.isBattery(stack)
                                        || stack.is(ModItems.BATTERY_CREATIVE.get())));
        addSlot(
                new SlotFiltered(
                        pad,
                        BlockEntityLaunchPadRocket.SLOT_FUEL_IN,
                        77,
                        21,
                        stack ->
                                stack.is(ModItems.ROCKET_FUEL.get())
                                        || FluidTankNTM.isFluidContainer(stack)));
        addSlot(
                new SlotFiltered(
                        pad, BlockEntityLaunchPadRocket.SLOT_FUEL_OUT, 77, 39, SlotFiltered.NONE));

        addStandardInventorySlots(playerInv, 14, 154);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return quickMoveUnsorted(player, index, SLOT_COUNT);
    }
}
