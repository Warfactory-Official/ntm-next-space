/*
 * SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
 * SPDX-FileCopyrightText: 2025-2026 George Paton
 * SPDX-FileCopyrightText: 2024 StarlarkXVII
 * SPDX-License-Identifier: LGPL-3.0-only
 *
 * Copyright (C) 2026 movblock. All rights reserved.
 * Copyright (C) 2025-2026 George Paton. All rights reserved.
 * Copyright (C) 2024 StarlarkXVII. All rights reserved.
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

import com.hbm.api.energymk2.EnergyHandler;
import com.hbm.api.fluidmk2.FluidTankHandler;
import com.hbm.blockentity.AudioLoop;
import com.hbm.blockentity.MachineBlockEntity;
import com.hbm.blockentity.SoundEmitter;
import com.hbm.blocks.multiblock.MultiblockCoreBlock;
import com.hbm.inventory.fluid.NTMFluidProperties;
import com.hbm.inventory.fluid.NTMFluids;
import com.hbm.inventory.fluid.tank.FluidTankAccess;
import com.hbm.inventory.fluid.tank.FluidTankNTM;
import com.hbm.packet.SyncUnitSchema;
import com.hbm.particle.RocketFlameColor;
import com.hbm.sound.AudioSystem;
import com.hbm.sound.SoundHandle;
import com.hbm.sound.WorldSounds;
import com.hbm.util.NumberFormatter;
import com.hbmspace.api.tile.PropulsionHandler;
import com.hbmspace.blocks.StationBlockEntitiesSpace;
import com.hbmspace.dim.CelestialBody;
import com.hbmspace.dim.SolarSystem;
import com.hbmspace.inventory.fluid.trait.FT_Rocket;
import com.hbmspace.particle.RocketFlamesSpace;
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
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class HTRF4BlockEntity extends MachineBlockEntity
        implements SoundEmitter,
                FluidTankAccess,
                PropulsionHandler,
                EnergyHandler,
                FluidTankHandler,
                AudioLoop,
                SyncUnitSchema {

    public static Consumer<HTRF4BlockEntity> CLIENT_EFFECTS = be -> {};

    public static final long MAX_POWER = 1_000_000_000;
    private static final int POWER_COST_MULTIPLIER = 250_000;

    public final FluidTankNTM[] tanks = {new FluidTankNTM(NTMFluids.PLASMA_DT, 64000)};

    public long power;
    public boolean isOn;
    public float soundtime;
    private int fuelCost;

    private boolean attached;

    public HTRF4BlockEntity(BlockPos pos, BlockState state) {
        super(StationBlockEntitiesSpace.HTRF4.get(), pos, state, 0);
    }

    @Override
    public void onGraphLoad(ServerLevel server) {
        if (!CelestialBody.inOrbit(server)) return;
        hbm$syncSet$HTRF4BlockEntity$isOn(false);
        attached = attachPropulsion(attached);
    }

    @Override
    public void setBlockState(BlockState state) {
        super.setBlockState(state);
        if (level instanceof ServerLevel) attached = attachPropulsion(attached);
    }

    @Override
    public void tickServer() {
        if (!CelestialBody.inOrbit(level)) return;

        if (isOn) {
            hbm$syncSet$HTRF4BlockEntity$soundtime((float) (soundtime + 1));

            if (soundtime == 1) {
                WorldSounds.machine(
                        level,
                        null,
                        worldPosition,
                        ModSoundsSpace.MISC_LPW_START.get(),
                        SoundSource.BLOCKS,
                        1.5F,
                        1F);
            } else if (soundtime > 20) {
                hbm$syncSet$HTRF4BlockEntity$soundtime(20);
            }
        } else {
            hbm$syncSet$HTRF4BlockEntity$soundtime((float) (soundtime - 1));

            if (soundtime == 19) {
                WorldSounds.machine(
                        level,
                        null,
                        worldPosition,
                        ModSoundsSpace.MISC_LPW_STOP.get(),
                        SoundSource.BLOCKS,
                        2.0F,
                        1F);
            } else if (soundtime <= 0) {
                hbm$syncSet$HTRF4BlockEntity$soundtime(0);
            }
        }

        networkPackNT(250);
    }

    @Override
    public void tickClient() {
        if (!CelestialBody.inOrbit(level)) return;
        CLIENT_EFFECTS.accept(this);
    }

    public RocketFlameColor contrailType() {
        return tanks[0].getTankType() == NTMFluids.PLASMA_BF
                ? RocketFlamesSpace.BALEFIRE
                : RocketFlamesSpace.FUSION;
    }

    @Override
    public SoundHandle createAudioLoop() {
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
        attached = attachPropulsion(attached);

        com$hbm$blockentity$AudioLoop$setRemoved();
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
        FT_Rocket trait = NTMFluidProperties.getTrait(tanks[0].getTankType(), FT_Rocket.class);
        int isp = trait != null ? trait.getISP() : 300;

        hbm$syncSet$HTRF4BlockEntity$fuelCost(SolarSystem.getFuelCost(deltaV, shipMass, isp));

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
                                    NumberFormatter.formatCompact(fuelCost * POWER_COST_MULTIPLIER))
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
        hbm$syncSet$HTRF4BlockEntity$isOn(true);
        hbm$syncSet$HTRF4BlockEntity$power((long) (power - (fuelCost * POWER_COST_MULTIPLIER)));
        for (FluidTankNTM tank : tanks) {
            tank.setFill(tank.getFill() - fuelCost);
        }
        return 180;
    }

    @Override
    public int endBurn() {
        hbm$syncSet$HTRF4BlockEntity$isOn(false);
        return 180;
    }

    @Override
    public int tankCount() {
        return tanks.length;
    }

    @Override
    public FluidTankNTM tank(int index) {
        return tanks[index];
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
        this.hbm$syncSet$HTRF4BlockEntity$power(power);
    }

    @Override
    public long getMaxPower() {
        return MAX_POWER;
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        hbm$syncSet$HTRF4BlockEntity$isOn(input.getBooleanOr("on", false));
        hbm$syncSet$HTRF4BlockEntity$power(input.getLongOr("power", 0L));
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
                hbm$syncSet$HTRF4BlockEntity$isOn(input.readBoolean());
                hbm$syncSet$HTRF4BlockEntity$soundtime(input.readFloat());
                hbm$syncSet$HTRF4BlockEntity$fuelCost(input.readInt());
                hbm$syncSet$HTRF4BlockEntity$power(input.readLong());
            }
            case 1 -> {
                for (FluidTankNTM tank : tanks) tank.packetDeserialize(input);
            }
            default -> super.readSyncUnit(unit, input);
        }
    }

    @Override
    public boolean tickSound() {
        return CelestialBody.inOrbit(level) && isOn && soundtime > 18;
    }

    public final FluidTankNTM hbm$syncArraySet$HTRF4BlockEntity$tanks(
            FluidTankNTM[] array, int index, FluidTankNTM next) {
        if (array[index] != next) {
            if (array == this.tanks && syncBound())
                com.hbm.packet.SyncBindings.unbind(this, array[index]);
            array[index] = next;
            if (array == this.tanks) {
                syncArrayChanged(array, index, 3, 1L << 1);
            }
        }
        return next;
    }

    public final void hbm$syncSet$HTRF4BlockEntity$power(long next) {
        if (this.power != next) {
            this.power = next;
            if ((1L << 0) == 0L) syncChanged(3);
            else syncUnitsChanged(3, 1L << 0);
        }
    }

    public final void hbm$syncSet$HTRF4BlockEntity$isOn(boolean next) {
        if (this.isOn != next) {
            this.isOn = next;
            if ((1L << 0) == 0L) syncChanged(3);
            else syncUnitsChanged(3, 1L << 0);
        }
    }

    public final void hbm$syncSet$HTRF4BlockEntity$soundtime(float next) {
        if (Float.floatToRawIntBits(this.soundtime) != Float.floatToRawIntBits(next)) {
            this.soundtime = next;
            if ((1L << 0) == 0L) syncChanged(3);
            else syncUnitsChanged(3, 1L << 0);
        }
    }

    public final void hbm$syncSet$HTRF4BlockEntity$fuelCost(int next) {
        if (this.fuelCost != next) {
            this.fuelCost = next;
            if ((1L << 0) == 0L) syncChanged(3);
            else syncUnitsChanged(3, 1L << 0);
        }
    }

    static {
        com.hbm.packet.SyncBindings.field(HTRF4BlockEntity.class, "tanks", 3, 1L << 1);
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
