// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-FileCopyrightText: 2024-2025 George Paton
// SPDX-FileCopyrightText: 2024 JamesH_2
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.blockentity.machine;

import com.hbm.api.fluidmk2.FluidFlushOutputs;
import com.hbm.api.fluidmk2.FluidTankEndpoint;
import com.hbm.blockentity.BlockEntityMachineBase;
import com.hbm.inventory.fluid.NTMFluids;
import com.hbm.inventory.fluid.tank.FluidTankNTM;
import com.hbm.packet.SyncField;
import com.hbm.packet.SyncUnitSchema;
import com.hbmspace.blocks.StationBlockEntitiesSpace;
import com.hbmspace.dim.CelestialBody;
import com.hbmspace.inventory.fluid.FluidsSpace;
import io.netty.buffer.ByteBuf;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class BlockEntityMachineGasDock extends BlockEntityMachineBase
        implements FluidTankEndpoint, SyncUnitSchema {

    public static Consumer<BlockEntityMachineGasDock> CLIENT_EFFECTS = be -> {};

    private static final long ROCKET_UNIT = 1L << 0;
    private static final long TANKS_UNIT = 1L << 1;

    @SyncField(units = TANKS_UNIT)
    public final FluidTankNTM[] tanks = {
        new FluidTankNTM(FluidsSpace.joolgas(), 64_000),
        new FluidTankNTM(NTMFluids.HYDROGEN, 32_000),
        new FluidTankNTM(NTMFluids.OXYGEN, 32_000)
    };

    private final FluidTankNTM[] sending = {tanks[0]};
    private final FluidTankNTM[] receiving = {tanks[1], tanks[2]};
    private final FluidFlushOutputs flush = new FluidFlushOutputs();

    @SyncField(units = ROCKET_UNIT)
    public boolean hasRocket = true;

    public int launchTicks = 0;

    public BlockEntityMachineGasDock(BlockPos pos, BlockState state) {
        super(StationBlockEntitiesSpace.GAS_DOCK.get(), pos, state, 0);
    }

    @Override
    public void tickServer() {
        flush.provide((ServerLevel) level, this);

        CelestialBody planet = CelestialBody.getPlanet(level);

        stepLaunch();

        if (planet.gas != null) {
            tanks[0].setTankType(planet.gas);

            if (hasFuel() && launchTicks <= -20) {
                hasRocket = false;
                collectGas();
            } else if (launchTicks >= 100) {
                hasRocket = true;
            }
        }

        networkPackNT(150);
    }

    @Override
    public void tickClient() {
        stepLaunch();
        CLIENT_EFFECTS.accept(this);
    }

    private void stepLaunch() {
        launchTicks = Mth.clamp(launchTicks + (hasRocket ? -1 : 1), hasRocket ? -20 : 0, 100);
    }

    private void collectGas() {
        if (tanks[1].getFill() < 500) return;
        if (tanks[2].getFill() < 500) return;
        if (tanks[0].getFill() + 8000 > tanks[0].getMaxFill()) return;

        tanks[1].setFill(tanks[1].getFill() - 500);
        tanks[2].setFill(tanks[2].getFill() - 500);
        tanks[0].setFill(tanks[0].getFill() + 8000);
    }

    private boolean hasFuel() {
        return tanks[1].getFill() >= 500 && tanks[2].getFill() >= 500;
    }

    @Override
    public FluidTankNTM[] getAllTanks() {
        return tanks;
    }

    @Override
    public FluidTankNTM[] getSendingTanks() {
        return sending;
    }

    @Override
    public FluidTankNTM[] getReceivingTanks() {
        return receiving;
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        hasRocket = input.getBooleanOr("hasRocker", true);
        input.child("gas").ifPresent(tanks[0]::deserialize);
        input.child("f1").ifPresent(tanks[1]::deserialize);
        input.child("f2").ifPresent(tanks[2]::deserialize);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putBoolean("hasRocker", hasRocket);
        tanks[0].serialize(output.child("gas"));
        tanks[1].serialize(output.child("f1"));
        tanks[2].serialize(output.child("f2"));
    }

    @Override
    public long syncUnitMask() {
        return super.syncUnitMask() | ROCKET_UNIT | TANKS_UNIT;
    }

    @Override
    public void writeSyncUnit(int unit, ByteBuf output) {
        switch (unit) {
            case 0 -> output.writeBoolean(hasRocket);
            case 1 -> {
                for (FluidTankNTM tank : tanks) tank.packetSerialize(output);
            }
            default -> super.writeSyncUnit(unit, output);
        }
    }

    @Override
    public void readSyncUnit(int unit, ByteBuf input) {
        switch (unit) {
            case 0 -> hasRocket = input.readBoolean();
            case 1 -> {
                for (FluidTankNTM tank : tanks) tank.packetDeserialize(input);
            }
            default -> super.readSyncUnit(unit, input);
        }
    }
}
