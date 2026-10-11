// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.blockentity.machine;

import com.hbm.api.control.IControlReceiver;
import com.hbm.api.energymk2.IEnergyHandlerMK2;
import com.hbm.blockentity.BlockEntityMachineBase;
import com.hbm.inventory.IGUIProvider;
import com.hbm.items.ISatChip;
import com.hbm.saveddata.SatelliteSavedData;
import com.hbm.saveddata.satellites.Satellite;
import com.hbmspace.blocks.WarBlocksSpace;
import com.hbmspace.dim.CelestialBody;
import com.hbmspace.inventory.container.MenuMachineWarController;
import com.hbmspace.items.ItemVOTVdrive;
import com.hbmspace.saveddata.satellites.SatelliteWar;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

public class BlockEntityMachineWarController extends BlockEntityMachineBase
        implements IEnergyHandlerMK2, IControlReceiver, IGUIProvider {

    public static final int SLOT_DRIVE = 1;
    public static final int SLOT_CHIP = 2;

    public BlockEntityMachineWarController(BlockPos pos, BlockState state) {
        super(WarBlocksSpace.WAR_CONTROLLER_BE.get(), pos, state, 4);
    }

    @Override
    public void tickServer() {
        int id = ISatChip.getFreqS(getItem(SLOT_CHIP));
        ItemStack drive = getItem(SLOT_DRIVE);
        if (!(drive.getItem() instanceof ItemVOTVdrive)) return;

        @Nullable Satellite sat =
                SatelliteSavedData.at(
                                (ServerLevel) level, worldPosition.getX(), worldPosition.getZ())
                        .getSatFromFreq(id);
        @Nullable CelestialBody body = ItemVOTVdrive.getDestination(drive).body().getBody();
        if (body != null && sat instanceof SatelliteWar war) war.setTarget(body);
    }

    @Override
    public void receiveControl(CompoundTag data) {
        if (!data.contains("xcoord") || !data.contains("zcoord")) return;

        ItemStack drive = getItem(SLOT_DRIVE);
        if (!(drive.getItem() instanceof ItemVOTVdrive)) return;

        ItemVOTVdrive.setCoordinates(drive, data.getIntOr("xcoord", 0), data.getIntOr("zcoord", 0));
        setChanged();
    }

    @Override
    public boolean hasPermission(Player player) {
        return stillValid(player);
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
        return new MenuMachineWarController(containerId, playerInventory, this);
    }
}
