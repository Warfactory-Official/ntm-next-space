/*
 * SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright (C) 2026 movblock. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * You may not use this file under the Apache License except in
 * compliance with that License. You may obtain a copy at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Apache License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License for the specific language governing permissions
 * and limitations under the License.
 */

package com.hbmspace.blockentity.machine;

import com.hbm.api.energymk2.EnergyHandler;
import com.hbm.blockentity.MachineBlockEntity;
import com.hbm.items.SatelliteFrequencyProvider;
import com.hbm.saveddata.SatelliteSavedData;
import com.hbm.saveddata.satellites.Satellite;
import com.hbmspace.blocks.WarBlocksSpace;
import com.hbmspace.dim.CelestialBody;
import com.hbmspace.inventory.container.WarControllerMenu;
import com.hbmspace.items.VOTVdriveItem;
import com.hbmspace.saveddata.satellites.SatelliteWar;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

public class WarControllerBlockEntity extends MachineBlockEntity
        implements EnergyHandler, MenuProvider {

    public static final int SLOT_DRIVE = 1;
    public static final int SLOT_CHIP = 2;

    public WarControllerBlockEntity(BlockPos pos, BlockState state) {
        super(WarBlocksSpace.WAR_CONTROLLER_BE.get(), pos, state, 4);
    }

    @Override
    public void tickServer() {
        int id = SatelliteFrequencyProvider.frequency(getItem(SLOT_CHIP));
        ItemStack drive = getItem(SLOT_DRIVE);
        if (!(drive.getItem() instanceof VOTVdriveItem)) return;

        @Nullable Satellite sat =
                SatelliteSavedData.at(
                                (ServerLevel) level, worldPosition.getX(), worldPosition.getZ())
                        .getSatFromFreq(id);
        @Nullable CelestialBody body = VOTVdriveItem.getDestination(drive).body().getBody();
        if (body != null && sat instanceof SatelliteWar war) war.setTarget(body);
    }

    public void setTarget(int x, int z) {
        ItemStack drive = getItem(SLOT_DRIVE);
        if (!(drive.getItem() instanceof VOTVdriveItem)) return;
        VOTVdriveItem.setCoordinates(drive, x, z);
        setChanged();
    }

    @Override
    public long getPower() {
        return 0;
    }

    @Override
    public void setPower(long power) {}

    @Override
    public long getMaxPower() {
        return 0;
    }

    @Override
    public Component getDisplayName() {
        return getName();
    }

    @Override
    public AbstractContainerMenu createMenu(
            int containerId, Inventory playerInventory, Player player) {
        return new WarControllerMenu(containerId, playerInventory, this);
    }
}
