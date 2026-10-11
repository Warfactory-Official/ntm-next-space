// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-FileCopyrightText: 2024-2026 George Paton
// SPDX-FileCopyrightText: 2024 JamesH_2
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.blockentity.machine;

import com.hbm.api.energymk2.IEnergyHandlerMK2;
import com.hbm.api.fluidmk2.FluidTankEndpoint;
import com.hbm.blockentity.BlockEntityMachineBase;
import com.hbm.blocks.multiblock.BlockMultiblockCore;
import com.hbm.inventory.fluid.NTMFluidProperties;
import com.hbm.inventory.fluid.NTMFluids;
import com.hbm.inventory.fluid.tank.FluidTankNTM;
import com.hbm.packet.SyncField;
import com.hbm.packet.SyncUnitSchema;
import com.hbm.util.BobMathUtil;
import com.hbmspace.api.tile.IPropulsion;
import com.hbmspace.blocks.StationBlockEntitiesSpace;
import com.hbmspace.dim.CelestialBody;
import com.hbmspace.dim.SolarSystem;
import com.hbmspace.inventory.fluid.trait.FT_Rocket;
import io.netty.buffer.ByteBuf;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class BlockEntityXenonThruster extends BlockEntityMachineBase
        implements IPropulsion, IEnergyHandlerMK2, FluidTankEndpoint, SyncUnitSchema {

    public static final long MAX_POWER = 20_000_000;
    private static final int POWER_COST_MULTIPLIER = 5_000;

    @SyncField(units = 1L << 1)
    public final FluidTankNTM[] tanks = {new FluidTankNTM(NTMFluids.XENON, 4_000)};

    @SyncField(units = 1L << 0)
    public long power;

    @SyncField(units = 1L << 0)
    private boolean isOn;

    @SyncField(units = 1L << 0)
    private int fuelCost;

    public float thrustAmount;

    private boolean hasRegistered;

    public BlockEntityXenonThruster(BlockPos pos, BlockState state) {
        super(StationBlockEntitiesSpace.XENON_THRUSTER.get(), pos, state, 0);
    }

    @Override
    public void tickServer() {
        if (!CelestialBody.inOrbit(level)) return;

        if (!hasRegistered) {
            if (isFacingPrograde()) registerPropulsion();
            hasRegistered = true;
            isOn = false;
        }

        networkPackNT(250);
    }

    @Override
    public void tickClient() {
        if (!CelestialBody.inOrbit(level)) return;

        if (isOn) {
            thrustAmount += 0.01D;
            if (thrustAmount > 1) thrustAmount = 1;
        } else {
            thrustAmount -= 0.01D;
            if (thrustAmount < 0) thrustAmount = 0;
        }
    }

    @Override
    public void setRemoved() {
        super.setRemoved();

        if (hasRegistered) {
            unregisterPropulsion();
            hasRegistered = false;
        }
    }

    public boolean isFacingPrograde() {
        return getBlockState().getValue(BlockMultiblockCore.FACING) == Direction.WEST;
    }

    @Override
    public BlockEntity getBlockEntity() {
        return this;
    }

    @Override
    public boolean canPerformBurn(int shipMass, double deltaV) {
        FT_Rocket trait = NTMFluidProperties.getTrait(tanks[0].getTankType(), FT_Rocket.class);
        int isp = trait != null ? trait.getISP() : 300;

        fuelCost = SolarSystem.getFuelCost(deltaV, shipMass, isp);

        if (power < fuelCost * POWER_COST_MULTIPLIER) return false;

        for (FluidTankNTM tank : tanks) {
            if (tank.getFill() < fuelCost) return false;
        }

        return true;
    }

    @Override
    public void addErrors(List<Component> errors) {
        if (power < fuelCost * POWER_COST_MULTIPLIER) {
            errors.add(
                    Component.translatable(
                                    "overlay.hbmspace.propulsion.power",
                                    BobMathUtil.getShortNumber(fuelCost * POWER_COST_MULTIPLIER))
                            .withStyle(ChatFormatting.RED));
        }

        for (FluidTankNTM tank : tanks) {
            if (tank.getFill() < fuelCost) {
                errors.add(
                        Component.translatable("overlay.hbmspace.propulsion.fuel", fuelCost)
                                .withStyle(ChatFormatting.RED));
            }
        }
    }

    @Override
    public float getThrust() {
        return 1_400_000;
    }

    @Override
    public int startBurn() {
        isOn = true;
        power -= fuelCost * POWER_COST_MULTIPLIER;
        for (FluidTankNTM tank : tanks) {
            tank.setFill(tank.getFill() - fuelCost);
        }
        return 200;
    }

    @Override
    public int endBurn() {
        isOn = false;
        return 200;
    }

    @Override
    public FluidTankNTM[] getAllTanks() {
        return tanks;
    }

    @Override
    public FluidTankNTM[] getReceivingTanks() {
        return tanks;
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
        return MAX_POWER;
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        isOn = input.getBooleanOr("on", false);
        power = input.getLongOr("power", 0L);
        input.child("t0").ifPresent(tanks[0]::deserialize);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putBoolean("on", isOn);
        output.putLong("power", power);
        tanks[0].serialize(output.child("t0"));
    }

    @Override
    public long syncUnitMask() {
        return super.syncUnitMask() | 0x3L;
    }

    @Override
    public void writeSyncUnit(int unit, ByteBuf output) {
        switch (unit) {
            case 0 -> {
                output.writeBoolean(isOn);
                output.writeLong(power);
                output.writeInt(fuelCost);
            }
            case 1 -> {
                for (FluidTankNTM tank : tanks) tank.packetSerialize(output);
            }
            default -> super.writeSyncUnit(unit, output);
        }
    }

    @Override
    public void readSyncUnit(int unit, ByteBuf input) {
        switch (unit) {
            case 0 -> {
                isOn = input.readBoolean();
                power = input.readLong();
                fuelCost = input.readInt();
            }
            case 1 -> {
                for (FluidTankNTM tank : tanks) tank.packetDeserialize(input);
            }
            default -> super.readSyncUnit(unit, input);
        }
    }
}
