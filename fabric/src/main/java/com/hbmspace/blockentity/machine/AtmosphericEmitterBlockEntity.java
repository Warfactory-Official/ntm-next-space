/*
 * SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
 * SPDX-FileCopyrightText: 2026 George Paton
 * SPDX-License-Identifier: LGPL-3.0-only
 *
 * Copyright (C) 2026 movblock. All rights reserved.
 * Copyright (C) 2026 George Paton. All rights reserved.
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
import com.hbm.inventory.fluid.tank.FluidTankAccess;
import com.hbm.inventory.fluid.tank.FluidTankNTM;
import com.hbm.inventory.fluid.trait.FluidReleaseType;
import com.hbm.inventory.fluid.trait.FluidTraitSimple.FT_Amat;
import com.hbm.inventory.fluid.trait.PollutingTrait;
import com.hbm.packet.SyncUnitSchema;
import com.hbm.particle.CoolingTowerParticleOptions;
import com.hbm.sound.AudioSystem;
import com.hbm.sound.ModSounds;
import com.hbm.sound.SoundHandle;
import com.hbmspace.blocks.AtmosphereBlockEntitiesSpace;
import com.hbmspace.handler.atmosphere.GasExchange;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class AtmosphericEmitterBlockEntity extends MachineBlockEntity
        implements SoundEmitter,
                FluidTankAccess,
                EnergyHandler,
                FluidTankHandler,
                AudioLoop,
                SyncUnitSchema {

    public static final long MAX_POWER = 250_000L;
    public static final long POWER_PER_MB = 10L;
    private static final float FAN_SPEED = 45F;

    public final FluidTankNTM tank = new FluidTankNTM(50_000);
    private final FluidTankNTM[] tanks = {tank};
    public long power;

    public float fan;
    public float prevFan;

    public AtmosphericEmitterBlockEntity(BlockPos pos, BlockState state) {
        super(AtmosphereBlockEntitiesSpace.ATMOSPHERIC_EMITTER.get(), pos, state, 0);
    }

    @Override
    public void tickServer() {
        networkPackNT(50);

        if (tank.getFill() <= 0) return;

        int toSpill = spill();
        if (power < toSpill * POWER_PER_MB) return;

        if (NTMFluidProperties.hasTrait(tank.getTankType(), FT_Amat.class)) {
            level.explode(
                    null,
                    worldPosition.getX() + 0.5,
                    worldPosition.getY() + 0.5,
                    worldPosition.getZ() + 0.5,
                    10F,
                    true,
                    Level.ExplosionInteraction.BLOCK);
            return;
        }

        tank.setFill(tank.getFill() - toSpill);
        PollutingTrait polluting =
                NTMFluidProperties.getTrait(tank.getTankType(), PollutingTrait.class);
        if (polluting != null)
            polluting.onRelease(level, worldPosition, tank, toSpill, FluidReleaseType.SPILL);
        GasExchange.release((ServerLevel) level, tank.getTankType(), toSpill);
        hbm$syncSet$AtmosphericEmitterBlockEntity$power((long) (power - (toSpill * POWER_PER_MB)));
        setChanged();
    }

    private int spill() {
        return Math.max(tank.getFill() / 2, 1);
    }

    public boolean isRunning() {
        return power >= spill() * POWER_PER_MB;
    }

    @Override
    public void tickClient() {
        prevFan = fan;
        boolean running = isRunning();

        if (running) {
            fan += FAN_SPEED;
            if (fan >= 360F) {
                fan -= 360F;
                prevFan -= 360F;
            }

            if (tank.getFill() > 0
                    && level.getNearestPlayer(
                                    worldPosition.getX(),
                                    worldPosition.getY(),
                                    worldPosition.getZ(),
                                    100.0D,
                                    EntitySelector.ENTITY_STILL_ALIVE)
                            != null) {
                Direction dir = MultiblockCoreBlock.coreFacing(getBlockState());
                Direction rot = dir.getClockWise();
                CoolingTowerParticleOptions plume =
                        new CoolingTowerParticleOptions.Builder()
                                .setLift(0.5F)
                                .setBaseScale(0.375F)
                                .setMaxScale(3F)
                                .setLife(100 + level.getRandom().nextInt(50))
                                .setColor(AirPumpBlockEntity.color(tank.getFluid()))
                                .build();
                level.addParticle(
                        plume,
                        true,
                        false,
                        worldPosition.getX() + 0.5 - dir.getStepX() * 0.5 + rot.getStepX() * 0.5,
                        worldPosition.getY() + 2.5,
                        worldPosition.getZ() + 0.5 - dir.getStepZ() * 0.5 + rot.getStepZ() * 0.5,
                        0,
                        0.2,
                        0);
            }
        }

        audioLoop(running, 0.25F);
    }

    @Override
    public SoundHandle createAudioLoop() {
        return AudioSystem.getLoopedSound(
                ModSounds.MOTOR_LOOP.get(),
                SoundSource.BLOCKS,
                worldPosition.getX(),
                worldPosition.getY(),
                worldPosition.getZ(),
                0.25F,
                10F,
                1.0F,
                20);
    }

    @Override
    public long getPower() {
        return power;
    }

    @Override
    public void setPower(long p) {
        hbm$syncSet$AtmosphericEmitterBlockEntity$power(p);
    }

    @Override
    public long getMaxPower() {
        return MAX_POWER;
    }

    @Override
    public FluidTankNTM[] getReceivingTanks() {
        return tanks;
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
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        input.child("t").ifPresent(tank::deserialize);
        hbm$syncSet$AtmosphericEmitterBlockEntity$power(input.getLongOr("power", 0L));
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putLong("power", power);
        tank.serialize(output.child("t"));
    }

    @Override
    public long syncUnitMask() {
        return super.syncUnitMask() | 0x3L;
    }

    @Override
    public void writeSyncUnit(int unit, ByteBuf output) {
        switch (unit) {
            case 0 -> output.writeLong(power);
            case 1 -> tank.packetSerialize(output);
            default -> super.writeSyncUnit(unit, output);
        }
    }

    @Override
    public void readSyncUnit(int unit, ByteBuf input) {
        switch (unit) {
            case 0 -> hbm$syncSet$AtmosphericEmitterBlockEntity$power(input.readLong());
            case 1 -> tank.packetDeserialize(input);
            default -> super.readSyncUnit(unit, input);
        }
    }

    @Override
    public boolean tickSound() {
        return isRunning();
    }

    public final void hbm$syncSet$AtmosphericEmitterBlockEntity$power(long next) {
        if (this.power != next) {
            this.power = next;
            if ((1L << 0) == 0L) syncChanged(3);
            else syncUnitsChanged(3, 1L << 0);
        }
    }

    static {
        com.hbm.packet.SyncBindings.field(AtmosphericEmitterBlockEntity.class, "tank", 3, 1L << 1);
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

    @Override
    public void setRemoved() {
        super.setRemoved();
        hbm$audioRemove();
    }
}
