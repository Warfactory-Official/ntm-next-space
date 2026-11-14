/*
 * SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
 * SPDX-FileCopyrightText: 2024 George Paton
 * SPDX-License-Identifier: LGPL-3.0-only
 *
 * Copyright (C) 2026 movblock. All rights reserved.
 * Copyright (C) 2024 George Paton. All rights reserved.
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

package com.hbmspace.blockentity.machine;

import com.hbm.blockentity.MachineBlockEntity;
import com.hbm.packet.SyncUnitSchema;
import com.hbmspace.blocks.StationBlockEntitiesSpace;
import com.hbmspace.dim.CelestialBody;
import com.hbmspace.dim.orbit.OrbitalStation.StationState;
import com.hbmspace.dim.orbit.OrbitalStation;
import com.hbmspace.inventory.container.OrbitalStationComputerMenu;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public class OrbitalStationComputerBlockEntity extends MachineBlockEntity
        implements MenuProvider, SyncUnitSchema {
    public boolean hasDrive;

    public OrbitalStationComputerBlockEntity(BlockPos pos, BlockState state) {
        super(StationBlockEntitiesSpace.ORBITAL_STATION_COMPUTER.get(), pos, state, 1);
    }

    public boolean travelTo(CelestialBody body, ItemStack drive) {
        OrbitalStation station =
                OrbitalStation.getStationFromPosition(worldPosition.getX(), worldPosition.getZ());

        if (station.orbiting == body) return false;

        station.travelTo(body);
        setItem(0, drive);

        return true;
    }

    public boolean isTravelling() {
        return OrbitalStation.getStationFromPosition(worldPosition.getX(), worldPosition.getZ())
                        .state
                != StationState.ORBIT;
    }

    @Override
    public void tickServer() {
        hbm$syncSet$OrbitalStationComputerBlockEntity$hasDrive(!inventory.get(0).isEmpty());
        networkPackNT(50);
    }

    public void setName(String name) {
        OrbitalStation.getStationFromPosition(worldPosition.getX(), worldPosition.getZ()).name =
                name;
    }

    public void setGravity(boolean enabled) {
        OrbitalStation.getStationFromPosition(worldPosition.getX(), worldPosition.getZ())
                        .gravityMultiplier =
                enabled ? 1 : 0;
    }

    @Override
    public Component getDisplayName() {
        return getName();
    }

    @Override
    public AbstractContainerMenu createMenu(
            int containerId, Inventory playerInventory, Player player) {
        return new OrbitalStationComputerMenu(containerId, playerInventory, this);
    }

    @Override
    public long syncUnitMask() {
        return super.syncUnitMask() | 0x1L;
    }

    @Override
    public void writeSyncUnit(int unit, ByteBuf output) {
        if (unit == 0) output.writeBoolean(hasDrive);
        else super.writeSyncUnit(unit, output);
    }

    @Override
    public void readSyncUnit(int unit, ByteBuf input) {
        if (unit == 0) hbm$syncSet$OrbitalStationComputerBlockEntity$hasDrive(input.readBoolean());
        else super.readSyncUnit(unit, input);
    }

    public final void hbm$syncSet$OrbitalStationComputerBlockEntity$hasDrive(boolean next) {
        if (this.hasDrive != next) {
            this.hasDrive = next;
            if ((1L << 0) == 0L) syncChanged(3);
            else syncUnitsChanged(3, 1L << 0);
        }
    }
}
