// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.inventory.container;

import com.hbm.inventory.container.BlockEntityMenu;
import com.hbmspace.blockentity.machine.BlockEntityOrbitalStationComputer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class MenuOrbitalStationComputer extends BlockEntityMenu<BlockEntityOrbitalStationComputer> {

    public MenuOrbitalStationComputer(
            int containerId, Inventory playerInv, BlockEntityOrbitalStationComputer be) {
        super(StationMenusSpace.ORBITAL_STATION_COMPUTER.get(), containerId, be);
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }
}
