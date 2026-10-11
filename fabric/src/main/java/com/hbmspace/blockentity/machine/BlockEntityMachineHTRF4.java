// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-FileCopyrightText: 2025-2026 George Paton
// SPDX-FileCopyrightText: 2024 StarlarkXVII
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.blockentity.machine;

import com.hbm.api.energymk2.IEnergyHandlerMK2;
import com.hbm.api.fluidmk2.FluidTankEndpoint;
import com.hbm.blockentity.AudioLoop;
import com.hbm.blockentity.BlockEntityMachineBase;
import com.hbm.blocks.multiblock.BlockMultiblockCore;
import com.hbm.inventory.fluid.NTMFluidProperties;
import com.hbm.inventory.fluid.NTMFluids;
import com.hbm.inventory.fluid.tank.FluidTankNTM;
import com.hbm.packet.SyncField;
import com.hbm.packet.SyncUnitSchema;
import com.hbm.sound.AudioSystem;
import com.hbm.sound.AudioWrapper;
import com.hbm.util.BobMathUtil;
import com.hbmspace.api.tile.IPropulsion;
import com.hbmspace.blocks.StationBlockEntitiesSpace;
import com.hbmspace.dim.CelestialBody;
import com.hbmspace.dim.SolarSystem;
import com.hbmspace.inventory.fluid.trait.FT_Rocket;
import com.hbmspace.sound.ModSoundsSpace;
import io.netty.buffer.ByteBuf;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class BlockEntityMachineHTRF4 extends BlockEntityMachineBase
        implements IPropulsion, IEnergyHandlerMK2, FluidTankEndpoint, AudioLoop, SyncUnitSchema {

    public static Consumer<BlockEntityMachineHTRF4> CLIENT_EFFECTS = be -> {};

    public static final long MAX_POWER = 1_000_000_000;
    private static final int POWER_COST_MULTIPLIER = 250_000;

    @SyncField(units = 1L << 1)
    public final FluidTankNTM[] tanks = {new FluidTankNTM(NTMFluids.PLASMA_DT, 64000)};

    @SyncField(units = 1L << 0)
    public long power;

    @SyncField(units = 1L << 0)
    public boolean isOn;

    @SyncField(units = 1L << 0)
    public float soundtime;

    @SyncField(units = 1L << 0)
    private int fuelCost;

    private boolean hasRegistered;

    public BlockEntityMachineHTRF4(BlockPos pos, BlockState state) {
        super(StationBlockEntitiesSpace.HTRF4.get(), pos, state, 0);
    }

    @Override
    public void tickServer() {
        if (!CelestialBody.inOrbit(level)) return;

        if (!hasRegistered) {
            if (isFacingPrograde()) registerPropulsion();
            hasRegistered = true;
            isOn = false;
        }

        if (isOn) {
            soundtime++;

            if (soundtime == 1) {
                level.playSound(
                        null,
                        worldPosition,
                        ModSoundsSpace.MISC_LPW_START.get(),
                        SoundSource.BLOCKS,
                        1.5F,
                        1F);
            } else if (soundtime > 20) {
                soundtime = 20;
            }
        } else {
            soundtime--;

            if (soundtime == 19) {
                level.playSound(
                        null,
                        worldPosition,
                        ModSoundsSpace.MISC_LPW_STOP.get(),
                        SoundSource.BLOCKS,
                        2.0F,
                        1F);
            } else if (soundtime <= 0) {
                soundtime = 0;
            }
        }

        networkPackNT(250);
    }

    @Override
    public void tickClient() {
        if (!CelestialBody.inOrbit(level)) return;
        CLIENT_EFFECTS.accept(this);
    }

    public String contrailType() {
        return tanks[0].getTankType() == NTMFluids.PLASMA_BF
                ? "missileContrailbf"
                : "missileContrailf";
    }

    @Override
    public AudioWrapper createAudioLoop() {
        return AudioSystem.getLoopedSound(
                ModSoundsSpace.MISC_LPW_LOOP.get(),
                SoundSource.BLOCKS,
                worldPosition.getX(),
                worldPosition.getY(),
                worldPosition.getZ(),
                0.25F,
                27.5F,
                1.0F,
                20);
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
        return getBlockState().getValue(BlockMultiblockCore.FACING) == Direction.SOUTH;
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
        return 1_600_000_000.0F;
    }

    @Override
    public int startBurn() {
        isOn = true;
        power -= fuelCost * POWER_COST_MULTIPLIER;
        for (FluidTankNTM tank : tanks) {
            tank.setFill(tank.getFill() - fuelCost);
        }
        return 180;
    }

    @Override
    public int endBurn() {
        isOn = false;
        return 180;
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
                output.writeFloat(soundtime);
                output.writeInt(fuelCost);
                output.writeLong(power);
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
                soundtime = input.readFloat();
                fuelCost = input.readInt();
                power = input.readLong();
            }
            case 1 -> {
                for (FluidTankNTM tank : tanks) tank.packetDeserialize(input);
            }
            default -> super.readSyncUnit(unit, input);
        }
    }
}
