// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-FileCopyrightText: 2024-2025 George Paton
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.blockentity.machine;

import com.hbm.api.energymk2.IEnergyHandlerMK2;
import com.hbm.api.fluidmk2.FluidFlushOutputs;
import com.hbm.api.fluidmk2.FluidTankEndpoint;
import com.hbm.blockentity.BlockEntityMachineBase;
import com.hbm.inventory.fluid.tank.FluidTankNTM;
import com.hbm.packet.SyncField;
import com.hbm.packet.SyncUnitSchema;
import com.hbmspace.blocks.AtmosphereBlockEntitiesSpace;
import com.hbmspace.dim.CelestialBody;
import com.hbmspace.dim.trait.CBT_Atmosphere;
import com.hbmspace.handler.atmosphere.ChunkAtmosphereManager;
import com.hbmspace.handler.atmosphere.GasExchange;
import com.hbmspace.inventory.fluid.FluidsSpace;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

public class BlockEntityAtmosphericCompressor extends BlockEntityMachineBase
        implements IEnergyHandlerMK2, FluidTankEndpoint, SyncUnitSchema {

    public static final long MAX_POWER = 1_000_000L;
    public static final int RATE = 200;
    private static final float MAX_SPEED = 30F;

    @SyncField(units = 1L << 1)
    public final FluidTankNTM tank = new FluidTankNTM(FluidsSpace.earthair(), 50_000);

    private final FluidTankNTM[] tanks = {tank};

    @SyncField(units = 1L << 0)
    public long power;

    public float rot;
    public float prevRot;
    private float rotSpeed;

    private final FluidFlushOutputs flush = new FluidFlushOutputs();

    public BlockEntityAtmosphericCompressor(BlockPos pos, BlockState state) {
        super(AtmosphereBlockEntitiesSpace.ATMOSPHERIC_COMPRESSOR.get(), pos, state, 0);
    }

    @Override
    public void tickServer() {
        ServerLevel server = (ServerLevel) level;
        Fluid typeBefore = tank.getTankType();
        int fillBefore = tank.getFill();

        CBT_Atmosphere atmosphere = CelestialBody.inOrbit(server) ? null : openSky(server);
        if (atmosphere != null && atmosphere.getPressure() > 0.0D) {
            if (!atmosphere.hasFluid(tank.getTankType()))
                tank.setTankType(atmosphere.getMainFluid());
        } else {
            tank.setTankType(null);
        }

        if (hasPower()
                && tank.getTankType() != null
                && tank.getFill() + RATE <= tank.getMaxFill()) {
            tank.setFill(tank.getFill() + RATE);
            power -= MAX_POWER / 50;
            GasExchange.capture(server, tank.getTankType(), RATE);
        }

        if (tank.getTankType() != typeBefore || tank.getFill() != fillBefore) setChanged();
        flush.provide(server, this);
        networkPackNT(50);
    }

    private @Nullable CBT_Atmosphere openSky(ServerLevel server) {
        return ChunkAtmosphereManager.proxy.hasAtmosphere(server, worldPosition)
                ? null
                : CelestialBody.getTrait(server, CBT_Atmosphere.class);
    }

    @Override
    public void tickClient() {
        if (hasPower()) {
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

    public boolean hasPower() {
        return power >= MAX_POWER / 50;
    }

    public void cycleGas() {
        CBT_Atmosphere atmosphere = openSky((ServerLevel) level);
        if (atmosphere == null) return;

        atmosphere = atmosphere.clone();
        atmosphere.sortDescending();

        Fluid current = tank.getTankType();
        for (int i = 0; i < atmosphere.fluids.size(); i++) {
            if (atmosphere.fluids.get(i).fluid == current) {
                int target = i + 1;
                if (target >= atmosphere.fluids.size()) target = 0;
                if (atmosphere.fluids.get(target).pressure <= 0.0001) target = 0;

                tank.setTankType(atmosphere.fluids.get(target).fluid);
                setChanged();
                break;
            }
        }
    }

    public boolean switchGas(Fluid target) {
        if (target == tank.getTankType()) return false;

        CBT_Atmosphere atmosphere = openSky((ServerLevel) level);
        if (atmosphere == null) return false;

        for (CBT_Atmosphere.FluidEntry entry : atmosphere.fluids) {
            if (entry.fluid == target) {
                tank.setTankType(target);
                setChanged();
                return true;
            }
        }
        return false;
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
        input.child("water").ifPresent(tank::deserialize);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putLong("power", power);
        tank.serialize(output.child("water"));
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
