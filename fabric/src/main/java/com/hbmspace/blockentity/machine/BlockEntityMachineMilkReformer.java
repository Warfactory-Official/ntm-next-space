// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.blockentity.machine;

import com.hbm.api.energymk2.IEnergyHandlerMK2;
import com.hbm.api.energymk2.ItemEnergyTransfer;
import com.hbm.api.fluidmk2.FluidFlushOutputs;
import com.hbm.api.fluidmk2.FluidTankEndpoint;
import com.hbm.blockentity.BlockEntityMachineBase;
import com.hbm.inventory.container.ContainerSync;
import com.hbm.inventory.fluid.tank.FluidTankNTM;
import com.hbm.packet.SyncField;
import com.hbm.packet.SyncUnitSchema;
import com.hbmspace.blocks.ModBlockEntitiesSpace;
import com.hbmspace.inventory.container.MenuMachineMilkReformer;
import com.hbmspace.inventory.fluid.FluidsSpace;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class BlockEntityMachineMilkReformer extends BlockEntityMachineBase
        implements IEnergyHandlerMK2, FluidTankEndpoint, MenuProvider, SyncUnitSchema {

    public static final int SLOT_BATTERY = 0;
    public static final int SLOT_INPUT_IN = 1;
    public static final int SLOT_INPUT_OUT = 2;
    public static final int SLOT_OUTPUT_IN_START = 3;
    public static final int SLOT_OUTPUT_OUT_START = 4;
    public static final int SLOT_COUNT = 9;

    public static final long MAX_POWER = 1_000_000L;
    public static final long CONSUMPTION = 10_000L;
    public static final int INPUT = 100;
    public static final int[] OUTPUTS = {50, 35, 15};

    @SyncField(units = 1L << 1)
    public final FluidTankNTM[] tanks = new FluidTankNTM[4];

    private final FluidTankNTM[] receiving;
    private final FluidTankNTM[] sending;

    @SyncField(units = 1L << 0)
    @ContainerSync
    public long power;

    private final FluidFlushOutputs flush = new FluidFlushOutputs();

    public BlockEntityMachineMilkReformer(BlockPos pos, BlockState state) {
        super(ModBlockEntitiesSpace.MILK_REFORMER.get(), pos, state, SLOT_COUNT);
        tanks[0] = new FluidTankNTM(FluidsSpace.milk(), 64_000);
        tanks[1] = new FluidTankNTM(FluidsSpace.emilk(), 32_000);
        tanks[2] = new FluidTankNTM(FluidsSpace.cmilk(), 32_000);
        tanks[3] = new FluidTankNTM(FluidsSpace.cream(), 32_000);
        receiving = new FluidTankNTM[] {tanks[0]};
        sending = new FluidTankNTM[] {tanks[1], tanks[2], tanks[3]};
    }

    @Override
    public long getPower() {
        return power;
    }

    @Override
    public void setPower(long p) {
        power = Math.max(0L, Math.min(p, MAX_POWER));
    }

    @Override
    public long getMaxPower() {
        return MAX_POWER;
    }

    @Override
    public FluidTankNTM[] getReceivingTanks() {
        return receiving;
    }

    @Override
    public FluidTankNTM[] getSendingTanks() {
        return sending;
    }

    @Override
    public FluidTankNTM[] getAllTanks() {
        return tanks;
    }

    @Override
    public void tickServer() {
        long prevPower = power;

        power += ItemEnergyTransfer.extract(this, SLOT_BATTERY, MAX_POWER - power, false);
        boolean changed = tanks[0].loadTank(SLOT_INPUT_IN, SLOT_INPUT_OUT, inventory);
        changed |= refine();
        for (int i = 1; i < tanks.length; i++) {
            changed |=
                    tanks[i].unloadTank(
                            SLOT_OUTPUT_IN_START + (i - 1) * 2,
                            SLOT_OUTPUT_OUT_START + (i - 1) * 2,
                            inventory);
        }
        if (changed || power != prevPower) setChanged();

        flush.provide((ServerLevel) level, this);
        networkPackNT(150);
    }

    private boolean refine() {
        if (power < CONSUMPTION) return false;
        if (tanks[0].getFill() < INPUT) return false;
        for (int i = 0; i < OUTPUTS.length; i++) {
            if (tanks[i + 1].getFill() + OUTPUTS[i] > tanks[i + 1].getMaxFill()) return false;
        }

        power -= CONSUMPTION;
        tanks[0].setFill(tanks[0].getFill() - INPUT);
        for (int i = 0; i < OUTPUTS.length; i++)
            tanks[i + 1].setFill(tanks[i + 1].getFill() + OUTPUTS[i]);
        return true;
    }

    @Override
    public Component getDisplayName() {
        return getName();
    }

    @Override
    public AbstractContainerMenu createMenu(
            int containerId, Inventory playerInventory, Player player) {
        return new MenuMachineMilkReformer(containerId, playerInventory, this);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        input.getLong("power").ifPresent(v -> power = v);
        input.child("input").ifPresent(tanks[0]::deserialize);
        input.child("m1").ifPresent(tanks[1]::deserialize);
        input.child("m2").ifPresent(tanks[2]::deserialize);
        input.child("m3").ifPresent(tanks[3]::deserialize);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putLong("power", power);
        tanks[0].serialize(output.child("input"));
        tanks[1].serialize(output.child("m1"));
        tanks[2].serialize(output.child("m2"));
        tanks[3].serialize(output.child("m3"));
    }

    @Override
    public long syncUnitMask() {
        return 0x3L;
    }

    @Override
    public void writeSyncUnit(int unit, ByteBuf output) {
        switch (unit) {
            case 0 -> output.writeLong(power);
            case 1 -> {
                for (FluidTankNTM tank : tanks) tank.packetSerialize(output);
            }
            default -> throw new IllegalArgumentException();
        }
    }

    @Override
    public void readSyncUnit(int unit, ByteBuf input) {
        switch (unit) {
            case 0 -> power = input.readLong();
            case 1 -> {
                for (FluidTankNTM tank : tanks) tank.packetDeserialize(input);
            }
            default -> throw new IllegalArgumentException();
        }
    }
}
