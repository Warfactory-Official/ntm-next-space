// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-FileCopyrightText: 2026 George Paton
// SPDX-FileCopyrightText: 2025-2026 JamesH_2
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.blockentity.machine;

import com.hbm.blockentity.AudioLoop;
import com.hbm.blockentity.machine.albion.BlockEntityCooledBase;
import com.hbm.blockentity.machine.fusion.IFusionPowerReceiver;
import com.hbm.blocks.multiblock.BlockMultiblockCore;
import com.hbm.packet.SyncField;
import com.hbm.sound.AudioSystem;
import com.hbm.sound.AudioWrapper;
import com.hbm.util.BobMathUtil;
import com.hbmspace.api.tile.IPropulsion;
import com.hbmspace.blocks.StationBlockEntitiesSpace;
import com.hbmspace.dim.CelestialBody;
import com.hbmspace.dim.SolarSystem;
import com.hbmspace.sound.ModSoundsSpace;
import io.netty.buffer.ByteBuf;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class BlockEntityMachineHTRNeo extends BlockEntityCooledBase
        implements IPropulsion, IFusionPowerReceiver, AudioLoop {

    public static Consumer<BlockEntityMachineHTRNeo> CLIENT_EFFECTS = be -> {};

    public static final long MAX_POWER = 200_000_000L;
    private static final long PLASMA_UNIT = 1L << 3;

    public long plasmaEnergy;

    @SyncField(units = PLASMA_UNIT)
    public long plasmaEnergySync;

    public float rotor;
    public float prevRotor;
    public float rotorSpeed;

    @SyncField(units = PLASMA_UNIT)
    public float soundtime;

    private boolean hasRegistered;

    @SyncField(units = PLASMA_UNIT)
    public boolean isOn;

    @SyncField(units = PLASMA_UNIT)
    public int fuelCost;

    public float thrustAmount;

    @SyncField(units = PLASMA_UNIT)
    public float plasmaR;

    @SyncField(units = PLASMA_UNIT)
    public float plasmaG;

    @SyncField(units = PLASMA_UNIT)
    public float plasmaB;

    public BlockEntityMachineHTRNeo(BlockPos pos, BlockState state) {
        super(StationBlockEntitiesSpace.HTRF4_NEO.get(), pos, state, 0);
    }

    @Override
    public void tickServer() {
        if (!hasRegistered && CelestialBody.inOrbit(level)) {
            if (isFacingPrograde()) registerPropulsion();
            hasRegistered = true;
            isOn = false;
        }

        flush.provide((ServerLevel) level, this);

        plasmaEnergySync = plasmaEnergy;

        tickCooling();

        if (isOn) {
            soundtime++;

            if (soundtime == 1) {
                level.playSound(
                        null,
                        worldPosition,
                        ModSoundsSpace.MISC_HTRF_START.get(),
                        SoundSource.BLOCKS,
                        1.5F,
                        1F);
            } else if (soundtime > 30) {
                soundtime = 30;
            }
        } else {
            soundtime--;

            if (soundtime == 29) {
                level.playSound(
                        null,
                        worldPosition,
                        ModSoundsSpace.MISC_HTR_STOP.get(),
                        SoundSource.BLOCKS,
                        2.0F,
                        1F);
            } else if (soundtime <= 0) {
                soundtime = 0;
            }
        }

        networkPackNT(200);
        plasmaEnergy = 0;
    }

    @Override
    public void tickClient() {
        if (power >= MAX_POWER || isOn) rotorSpeed += 0.125F;
        else rotorSpeed -= 0.125F;

        rotorSpeed = Mth.clamp(rotorSpeed, 0F, 15F);

        prevRotor = rotor;
        rotor += rotorSpeed;

        if (rotor >= 360F) {
            rotor -= 360F;
            prevRotor -= 360F;
        }

        if (isOn) {
            thrustAmount += 0.01D;
            if (thrustAmount > 1) thrustAmount = 1;
        } else {
            thrustAmount -= 0.01D;
            if (thrustAmount < 0) thrustAmount = 0;
        }

        CLIENT_EFFECTS.accept(this);
    }

    @Override
    public AudioWrapper createAudioLoop() {
        return AudioSystem.getLoopedSound(
                ModSoundsSpace.MISC_HTR_LOOP.get(),
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

    @Override
    public boolean receivesFusionPower() {
        return true;
    }

    @Override
    public void receiveFusionPower(
            long fusionPower, double neutronPower, float r, float g, float b) {
        plasmaEnergy = fusionPower;
        plasmaR = r;
        plasmaG = g;
        plasmaB = b;
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
        fuelCost = SolarSystem.getFuelCost(deltaV, shipMass, 250);

        if (plasmaEnergySync < fuelCost) return false;
        if (power < MAX_POWER) return false;
        return isCool();
    }

    @Override
    public void addErrors(List<Component> errors) {
        if (plasmaEnergySync < fuelCost) {
            errors.add(
                    Component.translatable(
                                    "overlay.hbmspace.htrf4neo.plasma",
                                    BobMathUtil.getShortNumber(fuelCost))
                            .withStyle(ChatFormatting.RED));
        }

        if (power < MAX_POWER) {
            errors.add(
                    Component.translatable("overlay.hbmspace.htrf4neo.power")
                            .withStyle(ChatFormatting.RED));
        }

        if (!isCool()) {
            errors.add(
                    Component.translatable("overlay.hbmspace.htrf4neo.coolant")
                            .withStyle(ChatFormatting.RED));
        }
    }

    @Override
    public float getThrust() {
        return 1_600_000_000.0F;
    }

    @Override
    public int startBurn() {
        isOn = true;
        power = 0;

        return 180;
    }

    @Override
    public int endBurn() {
        isOn = false;
        return 180;
    }

    @Override
    public long getMaxPower() {
        return MAX_POWER;
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        plasmaEnergy = input.getLongOr("plasma", 0L);
        isOn = input.getBooleanOr("on", false);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putLong("plasma", plasmaEnergy);
        output.putBoolean("on", isOn);
    }

    @Override
    public long syncUnitMask() {
        return super.syncUnitMask() | PLASMA_UNIT;
    }

    @Override
    public void writeSyncUnit(int unit, ByteBuf output) {
        if (unit != 3) {
            super.writeSyncUnit(unit, output);
            return;
        }
        output.writeLong(plasmaEnergySync);
        output.writeBoolean(isOn);
        output.writeInt(fuelCost);
        output.writeFloat(soundtime);
        output.writeFloat(plasmaR);
        output.writeFloat(plasmaG);
        output.writeFloat(plasmaB);
    }

    @Override
    public void readSyncUnit(int unit, ByteBuf input) {
        if (unit != 3) {
            super.readSyncUnit(unit, input);
            return;
        }
        plasmaEnergy = plasmaEnergySync = input.readLong();
        isOn = input.readBoolean();
        fuelCost = input.readInt();
        soundtime = input.readFloat();
        plasmaR = input.readFloat();
        plasmaG = input.readFloat();
        plasmaB = input.readFloat();
    }
}
