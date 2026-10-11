// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-FileCopyrightText: 2024-2025 George Paton
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.blockentity.machine;

import com.hbm.api.energymk2.IEnergyHandlerMK2;
import com.hbm.api.fluidmk2.FluidFlushOutputs;
import com.hbm.api.fluidmk2.FluidTankEndpoint;
import com.hbm.blockentity.BlockEntityMachineBase;
import com.hbm.inventory.fluid.NTMFluids;
import com.hbm.inventory.fluid.tank.FluidTankNTM;
import com.hbm.packet.SyncField;
import com.hbm.packet.SyncUnitSchema;
import com.hbm.util.TickPhase;
import com.hbmspace.blocks.AtmosphereBlockEntitiesSpace;
import com.hbmspace.handler.atmosphere.AtmosphereBlob;
import com.hbmspace.handler.atmosphere.ChunkAtmosphereManager;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

public class BlockEntityAirScrubber extends BlockEntityMachineBase
        implements IEnergyHandlerMK2, FluidTankEndpoint, SyncUnitSchema {

    public static final long MAX_POWER = 10_000L;
    public static final long MIN_POWER = 200L;
    public static final long POWER_PER_MB = 10L;
    private static final float MAX_SPEED = 30F;

    @SyncField(units = 1L << 1)
    public final FluidTankNTM tank = new FluidTankNTM(NTMFluids.CARBONDIOXIDE, 16_000);

    private final FluidTankNTM[] tanks = {tank};

    @SyncField(units = 1L << 0)
    public long power;

    public float rot;
    public float prevRot;
    private float rotSpeed;

    private @Nullable BlockEntityAirPump pump;
    private final FluidFlushOutputs flush = new FluidFlushOutputs();

    public BlockEntityAirScrubber(BlockPos pos, BlockState state) {
        super(AtmosphereBlockEntitiesSpace.AIR_SCRUBBER.get(), pos, state, 0);
    }

    @Override
    public void tickServer() {
        if (canOperate()
                && TickPhase.every(this, 5)
                && (pump == null || pump.getFluidPressure() == 0 || !pump.registerScrubber(this))) {
            pump = findPump((ServerLevel) level);
        }

        flush.provide((ServerLevel) level, this);
        networkPackNT(20);
    }

    private @Nullable BlockEntityAirPump findPump(ServerLevel level) {
        for (AtmosphereBlob blob : ChunkAtmosphereManager.proxy.getBlobs(level, worldPosition)) {
            if (level.getBlockEntity(blob.getRootPosition()) instanceof BlockEntityAirPump candidate
                    && candidate.registerScrubber(this)) {
                return candidate;
            }
        }
        return null;
    }

    @Override
    public void tickClient() {
        if (canOperate()) {
            rotSpeed = Math.min(rotSpeed + 0.2F, MAX_SPEED);
        } else {
            rotSpeed = Math.max(rotSpeed - 0.1F, 0F);
        }

        prevRot = rot;
        rot += rotSpeed;

        if (rot >= 360F) {
            rot -= 360F;
            prevRot -= 360F;
        }
    }

    public boolean canOperate() {
        return power > MIN_POWER;
    }

    public int scrub(int amount) {
        if (!canOperate()) return 0;
        int add = Math.min(tank.getMaxFill() - tank.getFill(), amount);
        tank.setFill(tank.getFill() + add);
        power -= add * POWER_PER_MB;
        if (add > 0) setChanged();
        return add;
    }

    @Override
    public long getPower() {
        return power;
    }

    @Override
    public void setPower(long p) {
        power = p;
    }

    @Override
    public long getMaxPower() {
        return MAX_POWER;
    }

    @Override
    public FluidTankNTM[] getSendingTanks() {
        return tanks;
    }

    @Override
    public FluidTankNTM[] getAllTanks() {
        return tanks;
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        power = input.getLongOr("power", 0L);
        input.child("t").ifPresent(tank::deserialize);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putLong("power", power);
        tank.serialize(output.child("t"));
    }

    @Override
    public long syncUnitMask() {
        return 0x3L;
    }

    @Override
    public void writeSyncUnit(int unit, ByteBuf output) {
        switch (unit) {
            case 0 -> output.writeLong(power);
            case 1 -> tank.packetSerialize(output);
            default -> throw new IllegalArgumentException();
        }
    }

    @Override
    public void readSyncUnit(int unit, ByteBuf input) {
        switch (unit) {
            case 0 -> power = input.readLong();
            case 1 -> tank.packetDeserialize(input);
            default -> throw new IllegalArgumentException();
        }
    }
}
