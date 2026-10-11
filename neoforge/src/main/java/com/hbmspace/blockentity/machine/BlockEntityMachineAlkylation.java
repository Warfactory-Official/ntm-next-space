// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.blockentity.machine;

import com.hbm.api.energymk2.IEnergyHandlerMK2;
import com.hbm.api.fluidmk2.FluidFlushOutputs;
import com.hbm.api.fluidmk2.FluidTankEndpoint;
import com.hbm.blockentity.BlockEntityMachineBase;
import com.hbm.blockentity.PersistentDrop;
import com.hbm.inventory.fluid.FluidStackNTM;
import com.hbm.inventory.fluid.NTMFluids;
import com.hbm.inventory.fluid.tank.FluidTankNTM;
import com.hbm.items.ModDataComponents;
import com.hbm.packet.SyncField;
import com.hbm.packet.SyncUnitSchema;
import com.hbm.util.TickPhase;
import com.hbmspace.blocks.IndustryBlockEntitiesSpace;
import com.hbmspace.inventory.fluid.FluidsSpace;
import com.hbmspace.inventory.recipes.AlkylationRecipe;
import com.hbmspace.inventory.recipes.AlkylationRecipes;
import io.netty.buffer.ByteBuf;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

public class BlockEntityMachineAlkylation extends BlockEntityMachineBase
        implements IEnergyHandlerMK2, FluidTankEndpoint, PersistentDrop, SyncUnitSchema {

    public static final long MAX_POWER = 1_000_000L;
    public static final long CONSUMPTION = 4_000L;
    private static final String[] TANK_KEYS = {"t0", "t1", "t2", "t3"};

    @SyncField(units = 1L << 1)
    public final FluidTankNTM[] tanks = new FluidTankNTM[4];

    private final FluidTankNTM[] receiving;
    private final FluidTankNTM[] sending;

    @SyncField(units = 1L << 0)
    public long power;

    private final FluidFlushOutputs flush = new FluidFlushOutputs();

    public BlockEntityMachineAlkylation(BlockPos pos, BlockState state) {
        super(IndustryBlockEntitiesSpace.ALKYLATION.get(), pos, state, 0);
        tanks[0] = new FluidTankNTM(FluidsSpace.chloromethane(), 8_000);
        tanks[1] = new FluidTankNTM(null, 4_000);
        tanks[2] = new FluidTankNTM(NTMFluids.UNSATURATEDS, 8_000);
        tanks[3] = new FluidTankNTM(NTMFluids.CHLORINE, 8_000);
        receiving = new FluidTankNTM[] {tanks[0], tanks[1]};
        sending = new FluidTankNTM[] {tanks[2], tanks[3]};
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
        if (TickPhase.every(this, 2) && alkylate()) setChanged();
        if (TickPhase.every(this, 10)) flush.provide((ServerLevel) level, this);
        networkPackNT(25);
    }

    private boolean alkylate() {
        AlkylationRecipe recipe = AlkylationRecipes.INSTANCE.getOutput(tanks[0].getTankType());
        if (recipe == null) {
            tanks[2].setTankType(null);
            tanks[3].setTankType(null);
            return false;
        }

        @Nullable FluidStackNTM acid = recipe.inputFluid.length > 1 ? recipe.inputFluid[1] : null;
        FluidStackNTM first = recipe.outputFluid[0];
        FluidStackNTM second = recipe.outputFluid[1];
        tanks[1].setTankType(acid == null ? null : acid.type());
        tanks[2].setTankType(first.type());
        tanks[3].setTankType(second.type());

        long acidFill = acid == null ? 0L : acid.amount();
        if (power < CONSUMPTION) return false;
        if (tanks[0].getFill() < AlkylationRecipes.FEED) return false;
        if (tanks[1].getFill() < acidFill) return false;
        if (tanks[2].getFill() + first.amount() > tanks[2].getMaxFill()) return false;
        if (tanks[3].getFill() + second.amount() > tanks[3].getMaxFill()) return false;

        tanks[0].setFill(tanks[0].getFill() - AlkylationRecipes.FEED);
        tanks[1].setFill(tanks[1].getFill() - acidFill);
        tanks[2].setFill(tanks[2].getFill() + first.amount());
        tanks[3].setFill(tanks[3].getFill() + second.amount());
        power -= CONSUMPTION;
        return true;
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        input.getLong("power").ifPresent(v -> power = v);
        for (int i = 0; i < tanks.length; i++) {
            FluidTankNTM tank = tanks[i];
            input.child(TANK_KEYS[i]).ifPresent(tank::deserialize);
        }
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
