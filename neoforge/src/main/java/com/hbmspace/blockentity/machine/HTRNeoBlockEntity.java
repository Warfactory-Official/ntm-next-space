/*
 * SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
 * SPDX-FileCopyrightText: 2026 George Paton
 * SPDX-FileCopyrightText: 2025-2026 JamesH_2
 * SPDX-License-Identifier: LGPL-3.0-only
 *
 * Copyright (C) 2026 movblock. All rights reserved.
 * Copyright (C) 2026 George Paton. All rights reserved.
 * Copyright (C) 2025-2026 JamesH_2. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License version 3 only,
 * as published by the Free Software Foundation.
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this code. If not, see <https://www.gnu.org/licenses/>.
 *
 * Under GPLv3 §7(c) and §7(e), these additional terms apply to LGPL-covered
 * material authored by movblock:
 *
 * - the license grants no rights to the name "NTM NEXT: Space";
 * - a modified version must not be named NTM NEXT: Space or presented as
 *   the official release, and must be marked as different from it.
 *
 * Accurately identifying a work as derived from NTM NEXT: Space does not
 * violate these additional terms, provided it is clearly distinguished from
 * the official release and does not imply endorsement. Ports are encouraged
 * to acknowledge NTM NEXT: Space as their upstream project and provide a
 * link to it.
 */

package com.hbmspace.blockentity.machine;

import com.hbm.blockentity.AudioLoop;
import com.hbm.blockentity.SoundEmitter;
import com.hbm.blockentity.machine.albion.CooledAcceleratorBlockEntity;
import com.hbm.blockentity.machine.fusion.PlasmaReceiver;
import com.hbm.blocks.multiblock.MultiblockCoreBlock;
import com.hbm.sound.AudioSystem;
import com.hbm.sound.SoundHandle;
import com.hbm.sound.WorldSounds;
import com.hbm.util.NumberFormatter;
import com.hbmspace.api.tile.PropulsionHandler;
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

public class HTRNeoBlockEntity extends CooledAcceleratorBlockEntity
        implements SoundEmitter, PropulsionHandler, PlasmaReceiver, AudioLoop {

    public static Consumer<HTRNeoBlockEntity> CLIENT_EFFECTS = be -> {};

    public static final long MAX_POWER = 200_000_000L;
    private static final long PLASMA_UNIT = 1L << 3;

    public long plasmaEnergy;
    public long plasmaEnergySync;

    public float rotor;
    public float prevRotor;
    public float rotorSpeed;
    public float soundtime;

    private boolean attached;
    public boolean isOn;
    public int fuelCost;
    public float thrustAmount;

    public float plasmaR;
    public float plasmaG;
    public float plasmaB;

    public HTRNeoBlockEntity(BlockPos pos, BlockState state) {
        super(StationBlockEntitiesSpace.HTRF4_NEO.get(), pos, state, 0);
    }

    @Override
    public void onGraphLoad(ServerLevel server) {
        if (!CelestialBody.inOrbit(server)) return;
        hbm$syncSet$HTRNeoBlockEntity$isOn(false);
        attached = attachPropulsion(attached);
    }

    @Override
    public void setBlockState(BlockState state) {
        super.setBlockState(state);
        if (level instanceof ServerLevel) attached = attachPropulsion(attached);
    }

    @Override
    public void tickServer() {
        flush.provide((ServerLevel) level, this);

        hbm$syncSet$HTRNeoBlockEntity$plasmaEnergySync(plasmaEnergy);

        tickCooling();

        if (isOn) {
            hbm$syncSet$HTRNeoBlockEntity$soundtime((float) (soundtime + 1));

            if (soundtime == 1) {
                WorldSounds.machine(
                        level,
                        null,
                        worldPosition,
                        ModSoundsSpace.MISC_HTRF_START.get(),
                        SoundSource.BLOCKS,
                        1.5F,
                        1F);
            } else if (soundtime > 30) {
                hbm$syncSet$HTRNeoBlockEntity$soundtime(30);
            }
        } else {
            hbm$syncSet$HTRNeoBlockEntity$soundtime((float) (soundtime - 1));

            if (soundtime == 29) {
                WorldSounds.machine(
                        level,
                        null,
                        worldPosition,
                        ModSoundsSpace.MISC_HTR_STOP.get(),
                        SoundSource.BLOCKS,
                        2.0F,
                        1F);
            } else if (soundtime <= 0) {
                hbm$syncSet$HTRNeoBlockEntity$soundtime(0);
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
    public SoundHandle createAudioLoop() {
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
        attached = attachPropulsion(attached);

        com$hbm$blockentity$AudioLoop$setRemoved();
    }

    @Override
    public boolean isPlasmaConsumer() {
        return true;
    }

    @Override
    public void receivePlasma(long fusionPower, double neutronPower, float r, float g, float b) {
        plasmaEnergy = fusionPower;
        hbm$syncSet$HTRNeoBlockEntity$plasmaR(r);
        hbm$syncSet$HTRNeoBlockEntity$plasmaG(g);
        hbm$syncSet$HTRNeoBlockEntity$plasmaB(b);
    }

    @Override
    public boolean isFacingPrograde() {
        return getBlockState().getValue(MultiblockCoreBlock.FACING) == Direction.SOUTH;
    }

    @Override
    public BlockEntity getBlockEntity() {
        return this;
    }

    @Override
    public boolean canPerformBurn(int shipMass, double deltaV) {
        hbm$syncSet$HTRNeoBlockEntity$fuelCost(SolarSystem.getFuelCost(deltaV, shipMass, 250));

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
                                    NumberFormatter.formatCompact(fuelCost))
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
        hbm$syncSet$HTRNeoBlockEntity$isOn(true);
        hbm$syncSet$CooledAcceleratorBlockEntity$power(0);

        return 180;
    }

    @Override
    public int endBurn() {
        hbm$syncSet$HTRNeoBlockEntity$isOn(false);
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
        hbm$syncSet$HTRNeoBlockEntity$isOn(input.getBooleanOr("on", false));
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
        hbm$syncSet$HTRNeoBlockEntity$plasmaEnergySync(input.readLong());
        plasmaEnergy = plasmaEnergySync;
        hbm$syncSet$HTRNeoBlockEntity$isOn(input.readBoolean());
        hbm$syncSet$HTRNeoBlockEntity$fuelCost(input.readInt());
        hbm$syncSet$HTRNeoBlockEntity$soundtime(input.readFloat());
        hbm$syncSet$HTRNeoBlockEntity$plasmaR(input.readFloat());
        hbm$syncSet$HTRNeoBlockEntity$plasmaG(input.readFloat());
        hbm$syncSet$HTRNeoBlockEntity$plasmaB(input.readFloat());
    }

    @Override
    public boolean tickSound() {
        return isOn && soundtime > 28;
    }

    public final void hbm$syncSet$HTRNeoBlockEntity$plasmaEnergySync(long next) {
        if (this.plasmaEnergySync != next) {
            this.plasmaEnergySync = next;
            if ((PLASMA_UNIT) == 0L) syncChanged(3);
            else syncUnitsChanged(3, PLASMA_UNIT);
        }
    }

    public final void hbm$syncSet$HTRNeoBlockEntity$soundtime(float next) {
        if (Float.floatToRawIntBits(this.soundtime) != Float.floatToRawIntBits(next)) {
            this.soundtime = next;
            if ((PLASMA_UNIT) == 0L) syncChanged(3);
            else syncUnitsChanged(3, PLASMA_UNIT);
        }
    }

    public final void hbm$syncSet$HTRNeoBlockEntity$isOn(boolean next) {
        if (this.isOn != next) {
            this.isOn = next;
            if ((PLASMA_UNIT) == 0L) syncChanged(3);
            else syncUnitsChanged(3, PLASMA_UNIT);
        }
    }

    public final void hbm$syncSet$HTRNeoBlockEntity$fuelCost(int next) {
        if (this.fuelCost != next) {
            this.fuelCost = next;
            if ((PLASMA_UNIT) == 0L) syncChanged(3);
            else syncUnitsChanged(3, PLASMA_UNIT);
        }
    }

    public final void hbm$syncSet$HTRNeoBlockEntity$plasmaR(float next) {
        if (Float.floatToRawIntBits(this.plasmaR) != Float.floatToRawIntBits(next)) {
            this.plasmaR = next;
            if ((PLASMA_UNIT) == 0L) syncChanged(3);
            else syncUnitsChanged(3, PLASMA_UNIT);
        }
    }

    public final void hbm$syncSet$HTRNeoBlockEntity$plasmaG(float next) {
        if (Float.floatToRawIntBits(this.plasmaG) != Float.floatToRawIntBits(next)) {
            this.plasmaG = next;
            if ((PLASMA_UNIT) == 0L) syncChanged(3);
            else syncUnitsChanged(3, PLASMA_UNIT);
        }
    }

    public final void hbm$syncSet$HTRNeoBlockEntity$plasmaB(float next) {
        if (Float.floatToRawIntBits(this.plasmaB) != Float.floatToRawIntBits(next)) {
            this.plasmaB = next;
            if ((PLASMA_UNIT) == 0L) syncChanged(3);
            else syncUnitsChanged(3, PLASMA_UNIT);
        }
    }

    private boolean hbm$soundActive;

    public boolean soundStarted(boolean active) {
        boolean started = active && !hbm$soundActive;
        hbm$soundActive = active;
        return started;
    }

    private SoundHandle hbm$audio;

    public void audioLoop(boolean running, float volume) {
        hbm$audio = AudioLoop.loop(this, hbm$audio, running, volume, 0F, false);
    }

    public void audioLoop(boolean running, float volume, float pitch) {
        hbm$audio = AudioLoop.loop(this, hbm$audio, running, volume, pitch, true);
    }

    private void hbm$audioRemove() {
        audioLoop(false, 0F);
    }

    private void com$hbm$blockentity$AudioLoop$setRemoved() {
        hbm$audioRemove();
    }
}
