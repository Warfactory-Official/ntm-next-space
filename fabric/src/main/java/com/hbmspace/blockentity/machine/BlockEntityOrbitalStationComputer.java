// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-FileCopyrightText: 2024 George Paton
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.blockentity.machine;

import com.hbm.api.control.IControlReceiver;
import com.hbm.blockentity.BlockEntityMachineBase;
import com.hbm.packet.SyncField;
import com.hbm.packet.SyncUnitSchema;
import com.hbmspace.blocks.StationBlockEntitiesSpace;
import com.hbmspace.dim.CelestialBody;
import com.hbmspace.dim.orbit.OrbitalStation.StationState;
import com.hbmspace.dim.orbit.OrbitalStation;
import com.hbmspace.inventory.container.MenuOrbitalStationComputer;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public class BlockEntityOrbitalStationComputer extends BlockEntityMachineBase
        implements MenuProvider, IControlReceiver, SyncUnitSchema {

    @SyncField(units = 1L << 0)
    public boolean hasDrive;

    public BlockEntityOrbitalStationComputer(BlockPos pos, BlockState state) {
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
        hasDrive = !inventory.get(0).isEmpty();
        networkPackNT(50);
    }

    @Override
    public boolean hasPermission(Player player) {
        return true;
    }

    @Override
    public void receiveControl(CompoundTag data) {
        OrbitalStation station =
                OrbitalStation.getStationFromPosition(worldPosition.getX(), worldPosition.getZ());
        data.getString("name").ifPresent(name -> station.name = name);
        data.getBoolean("gravity")
                .ifPresent(gravity -> station.gravityMultiplier = gravity ? 1 : 0);
    }

    @Override
    public Component getDisplayName() {
        return getName();
    }

    @Override
    public AbstractContainerMenu createMenu(
            int containerId, Inventory playerInventory, Player player) {
        return new MenuOrbitalStationComputer(containerId, playerInventory, this);
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
        if (unit == 0) hasDrive = input.readBoolean();
        else super.readSyncUnit(unit, input);
    }
}
