// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-FileCopyrightText: 2024-2025 George Paton
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.blockentity.machine;

import com.hbm.api.energymk2.IEnergyHandlerMK2;
import com.hbm.blockentity.BlockEntityMachineBase;
import com.hbm.blocks.multiblock.BlockMultiblockCore;
import com.hbm.handler.threading.TargetPoint;
import com.hbm.packet.PacketWire;
import com.hbm.packet.SyncField;
import com.hbm.packet.SyncUnitSchema;
import com.hbm.packet.toclient.EffectNTPayload;
import com.hbm.particle.HbmEffectNT;
import com.hbm.particle.helper.ParticleCreators;
import com.hbmspace.blockentity.IDysonConverter;
import com.hbmspace.blocks.SkyBlockEntitiesSpace;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;

public class BlockEntityDysonConverterHE extends BlockEntityMachineBase
        implements IDysonConverter, IEnergyHandlerMK2, SyncUnitSchema {

    public long power;

    @SyncField(units = 1L)
    public boolean isConverting;

    private int cooldown;

    public BlockEntityDysonConverterHE(BlockPos pos, BlockState state) {
        super(SkyBlockEntitiesSpace.DYSON_CONVERTER_HE.get(), pos, state, 0);
    }

    @Override
    public void tickServer() {
        ServerLevel level = (ServerLevel) this.level;
        Direction dir = BlockMultiblockCore.coreFacing(getBlockState()).getOpposite();
        Direction rot = dir.getClockWise();

        isConverting = power > 0;

        if (isConverting && level.getGameTime() % 2 == 0) {
            boolean tau = level.getGameTime() % 10 == 0;
            spark(level, tau, dir, 4, 2.25, rot);
            spark(level, tau, dir, 4, 2.25, rot.getOpposite());
            spark(level, tau, dir, 3, 2.75, rot);
            spark(level, tau, dir, 3, 2.75, rot.getOpposite());

            cooldown++;
            if (cooldown > 10) {
                power = 0;
                cooldown = 0;
            }
        }

        networkPackNT(250);
    }

    private void spark(
            ServerLevel level, boolean tau, Direction dir, int forward, double up, Direction side) {
        double x = worldPosition.getX() + 0.5 + dir.getStepX() * forward + side.getStepX();
        double y = worldPosition.getY() + up;
        double z = worldPosition.getZ() + 0.5 + dir.getStepZ() * forward + side.getStepZ();
        if (tau) {
            ParticleCreators.sparks(level, x, y, z, 1, false, 25);
        } else {
            PacketWire.sendToAllAround(
                    new EffectNTPayload(HbmEffectNT.Hadron, x, y, z),
                    new TargetPoint(
                            level,
                            worldPosition.getX(),
                            worldPosition.getY(),
                            worldPosition.getZ(),
                            25));
        }
    }

    @Override
    public boolean provideEnergy(BlockPos pos, long energy) {
        Direction facing = BlockMultiblockCore.coreFacing(getBlockState());
        if (!pos.equals(worldPosition.relative(facing, 4).above())) return false;

        power = energy;
        cooldown = 0;
        return true;
    }

    @Override
    public long getPower() {
        return power;
    }

    @Override
    public void setPower(long power) {
        this.power = power;
    }

    @Override
    public long getMaxPower() {
        return power;
    }

    @Override
    public long syncUnitMask() {
        return 1L;
    }

    @Override
    public void writeSyncUnit(int unit, ByteBuf output) {
        if (unit != 0) throw new IllegalArgumentException();
        output.writeBoolean(isConverting);
    }

    @Override
    public void readSyncUnit(int unit, ByteBuf input) {
        if (unit != 0) throw new IllegalArgumentException();
        isConverting = input.readBoolean();
    }
}
