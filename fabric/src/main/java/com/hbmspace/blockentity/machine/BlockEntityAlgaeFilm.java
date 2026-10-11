// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-FileCopyrightText: 2024 George Paton
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.blockentity.machine;

import com.hbm.api.fluidmk2.FluidFlushOutputs;
import com.hbm.api.fluidmk2.FluidTankEndpoint;
import com.hbm.blockentity.BlockEntityMachineBase;
import com.hbm.inventory.fluid.NTMFluids;
import com.hbm.inventory.fluid.tank.FluidTankNTM;
import com.hbm.packet.SyncField;
import com.hbm.packet.SyncUnitSchema;
import com.hbmspace.blocks.TerrainBlockEntitiesSpace;
import com.hbmspace.dim.SpaceDimensions;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class BlockEntityAlgaeFilm extends BlockEntityMachineBase
        implements FluidTankEndpoint, SyncUnitSchema {

    private static final long OPERATE_UNIT = 1L << 0;
    private static final long TANKS_UNIT = 1L << 1;

    @SyncField(units = OPERATE_UNIT)
    public boolean canOperate;

    @SyncField(units = TANKS_UNIT)
    public final FluidTankNTM[] tanks = {
        new FluidTankNTM(NTMFluids.CARBONDIOXIDE, 8_000), new FluidTankNTM(NTMFluids.OXYGEN, 8_000)
    };

    private final FluidTankNTM[] receiving = {tanks[0]};
    private final FluidTankNTM[] sending = {tanks[1]};
    private final FluidFlushOutputs flush = new FluidFlushOutputs();

    public BlockEntityAlgaeFilm(BlockPos pos, BlockState state) {
        super(TerrainBlockEntitiesSpace.ALGAE_FILM.get(), pos, state, 0);
    }

    @Override
    public void tickServer() {
        canOperate = level.dimension() == SpaceDimensions.ORBIT;

        if (canOperate
                && level.getRandom().nextBoolean()
                && tanks[0].getFill() > 0
                && tanks[1].getFill() < tanks[1].getMaxFill()) {
            tanks[0].setFill(tanks[0].getFill() - 1);
            tanks[1].setFill(tanks[1].getFill() + 1);
            setChanged();
        }

        flush.provide((ServerLevel) level, this);
        networkPackNT(20);
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
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        for (int i = 0; i < tanks.length; i++)
            input.child("t" + i).ifPresent(tanks[i]::deserialize);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        for (int i = 0; i < tanks.length; i++) tanks[i].serialize(output.child("t" + i));
    }

    @Override
    public long syncUnitMask() {
        return OPERATE_UNIT | TANKS_UNIT;
    }

    @Override
    public void writeSyncUnit(int unit, ByteBuf output) {
        switch (unit) {
            case 0 -> output.writeBoolean(canOperate);
            case 1 -> {
                for (FluidTankNTM tank : tanks) tank.packetSerialize(output);
            }
            default -> throw new IllegalArgumentException();
        }
    }

    @Override
    public void readSyncUnit(int unit, ByteBuf input) {
        switch (unit) {
            case 0 -> canOperate = input.readBoolean();
            case 1 -> {
                for (FluidTankNTM tank : tanks) tank.packetDeserialize(input);
            }
            default -> throw new IllegalArgumentException();
        }
    }
}
