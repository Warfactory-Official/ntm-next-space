// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.blockentity.machine;

import com.hbm.api.energymk2.IEnergyHandlerMK2;
import com.hbm.api.energymk2.ItemEnergyTransfer;
import com.hbm.api.fluidmk2.FluidFlushOutputs;
import com.hbm.api.fluidmk2.FluidTankEndpoint;
import com.hbm.blockentity.BlockEntityMachineBase;
import com.hbm.blockentity.PersistentDrop;
import com.hbm.inventory.container.ContainerSync;
import com.hbm.inventory.fluid.FluidStackNTM;
import com.hbm.inventory.fluid.NTMFluids;
import com.hbm.inventory.fluid.tank.FluidTankNTM;
import com.hbm.items.ModDataComponents;
import com.hbm.packet.SyncField;
import com.hbm.packet.SyncUnitSchema;
import com.hbmspace.blocks.IndustryBlockEntitiesSpace;
import com.hbmspace.inventory.container.MenuMachineCryoDistill;
import com.hbmspace.inventory.fluid.FluidsSpace;
import com.hbmspace.inventory.recipes.CryoRecipe;
import com.hbmspace.inventory.recipes.CryoRecipes;
import io.netty.buffer.ByteBuf;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class BlockEntityMachineCryoDistill extends BlockEntityMachineBase
        implements IEnergyHandlerMK2,
                FluidTankEndpoint,
                MenuProvider,
                PersistentDrop,
                SyncUnitSchema {

    public static final int SLOT_BATTERY = 0;
    public static final int SLOT_FLUID_ID = 7;
    public static final int[][] SLOT_UNLOAD = {{1, 2}, {3, 4}, {5, 6}, {8, 9}};
    public static final int SLOT_COUNT = 10;

    public static final long MAX_POWER = 1_000_000L;
    public static final long CONSUMPTION = 20_000L;
    private static final String[] TANK_KEYS = {"input", "o1", "o2", "o3", "o4"};

    @SyncField(units = 1L << 1)
    public final FluidTankNTM[] tanks = new FluidTankNTM[5];

    private final FluidTankNTM[] receiving;
    private final FluidTankNTM[] sending;

    @SyncField(units = 1L << 0)
    @ContainerSync
    public long power;

    private final FluidFlushOutputs flush = new FluidFlushOutputs();

    public BlockEntityMachineCryoDistill(BlockPos pos, BlockState state) {
        super(IndustryBlockEntitiesSpace.CRYO_DISTILL.get(), pos, state, SLOT_COUNT);
        tanks[0] = new FluidTankNTM(FluidsSpace.earthair(), 64_000);
        tanks[1] = new FluidTankNTM(FluidsSpace.nitrogen(), 24_000);
        tanks[2] = new FluidTankNTM(NTMFluids.OXYGEN, 24_000);
        tanks[3] = new FluidTankNTM(FluidsSpace.krypton(), 24_000);
        tanks[4] = new FluidTankNTM(NTMFluids.CARBONDIOXIDE, 24_000);
        receiving = new FluidTankNTM[] {tanks[0]};
        sending = new FluidTankNTM[] {tanks[1], tanks[2], tanks[3], tanks[4]};
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
        boolean changed = tanks[0].setType(SLOT_FLUID_ID, SLOT_FLUID_ID, inventory);
        changed |= distill();
        for (int i = 0; i < SLOT_UNLOAD.length; i++) {
            changed |= tanks[i + 1].unloadTank(SLOT_UNLOAD[i][0], SLOT_UNLOAD[i][1], inventory);
        }
        if (changed || power != prevPower) setChanged();

        flush.provide((ServerLevel) level, this);
        networkPackNT(15);
    }

    private boolean distill() {
        CryoRecipe recipe = CryoRecipes.INSTANCE.getOutput(tanks[0].getTankType());
        if (recipe == null) {
            for (int i = 1; i < tanks.length; i++) tanks[i].setTankType(null);
            return false;
        }

        FluidStackNTM[] out = recipe.outputFluid;
        for (int i = 1; i < tanks.length; i++) tanks[i].setTankType(out[i - 1].type());

        if (power < CONSUMPTION) return false;
        if (tanks[0].getFill() < CryoRecipes.FEED) return false;
        for (int i = 1; i < tanks.length; i++) {
            if (tanks[i].getFill() + out[i - 1].amount() > tanks[i].getMaxFill()) return false;
        }

        tanks[0].setFill(tanks[0].getFill() - CryoRecipes.FEED);
        for (int i = 1; i < tanks.length; i++)
            tanks[i].setFill(tanks[i].getFill() + out[i - 1].amount());
        power -= CONSUMPTION;
        return true;
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("container.cryoDistillator");
    }

    @Override
    public Component getDisplayName() {
        return getName();
    }

    @Override
    public AbstractContainerMenu createMenu(
            int containerId, Inventory playerInventory, Player player) {
        return new MenuMachineCryoDistill(containerId, playerInventory, this);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        input.getLong("power").ifPresent(v -> power = v);
        for (int i = 0; i < tanks.length; i++)
            input.child(TANK_KEYS[i]).ifPresent(tanks[i]::deserialize);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putLong("power", power);
        for (int i = 0; i < tanks.length; i++) tanks[i].serialize(output.child(TANK_KEYS[i]));
    }

    @Override
    public void writePersistent(DataComponentMap.Builder components) {
        for (FluidTankNTM tank : tanks) {
            if (tank.getFill() > 0) {
                components.set(
                        ModDataComponents.TANK_CONTENTS.get(), FluidStackNTM.snapshot(tanks));
                return;
            }
        }
    }

    @Override
    public void readPersistent(DataComponentGetter components) {
        List<FluidStackNTM> contents = components.get(ModDataComponents.TANK_CONTENTS.get());
        if (contents != null) FluidStackNTM.restore(contents, tanks);
    }

    @Override
    public String[] persistentKeys() {
        return TANK_KEYS;
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
